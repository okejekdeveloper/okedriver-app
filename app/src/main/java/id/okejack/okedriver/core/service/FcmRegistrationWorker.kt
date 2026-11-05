package id.okejack.okedriver.core.service

import android.content.Context
import androidx.work.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmRegistrationDependencies {
    fun authRepository(): AuthRepository
    fun sessionDataStore(): SessionDataStore
}

class FcmRegistrationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(applicationContext, FcmRegistrationDependencies::class.java)
        val token = dependencies.sessionDataStore().token.first().orEmpty()
        if (token.isBlank() || fingerprint(token) != inputData.getString("session_fingerprint")) return Result.success()
        return when (val response = dependencies.authRepository().registerFcmToken()) {
            is ApiResponse.Success -> Result.success()
            is ApiResponse.Error -> if (response.code == 401) Result.success() else Result.retry()
            ApiResponse.Loading -> Result.retry()
        }
    }

    companion object {
        private const val NAME = "driver_fcm_registration"
        private fun fingerprint(token: String): String = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray()).joinToString("") { "%02x".format(it) }

        fun enqueue(context: Context, sessionToken: String) {
            if (sessionToken.isBlank()) { cancel(context); return }
            val request = OneTimeWorkRequestBuilder<FcmRegistrationWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf("session_fingerprint" to fingerprint(sessionToken)))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context) { WorkManager.getInstance(context).cancelUniqueWork(NAME) }
    }
}
