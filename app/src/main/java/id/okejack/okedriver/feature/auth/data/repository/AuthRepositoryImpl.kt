package id.okejack.okedriver.feature.auth.data.repository

import id.okejack.okedriver.core.network.model.ServerErrors

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.api.FcmApiService
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.core.service.FirebaseTokenProvider
import id.okejack.okedriver.feature.auth.data.remote.AuthApiService
import id.okejack.okedriver.feature.auth.data.remote.dto.FcmTokenRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.GoogleLoginRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.LoginRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.toJsonObject
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.okejack.okedriver.core.service.LocationHelper
import kotlinx.coroutines.CancellationException
import id.okejack.okedriver.core.service.FcmRegistrationWorker

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val fcmApi: FcmApiService,
    private val firebaseTokenProvider: FirebaseTokenProvider,
    private val sessionDataStore: SessionDataStore,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResponse<AuthDriver> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.success && response.data.sessionId.isNotBlank() && response.data.driver.id > 0) {
                val driver = response.data.driver
                val token = response.data.sessionId
                sessionDataStore.saveSession(
                    token = token,
                    driverId = driver.id.toInt(),
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    driverEmail = driver.email,
                    driverPhoto = driver.imageUrl
                )
                cacheDriverProfile(driver)
                FcmRegistrationWorker.enqueue(context, token)
                ApiResponse.Success(driver.toDomain(token))
            } else {
                ApiResponse.Error(400, ServerErrors.message(response.error, response.message), errorCode = response.error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResponse.Error(500, e.message ?: "Koneksi gagal")
        }
    }

    override suspend fun loginWithGoogle(email: String, idToken: String): ApiResponse<AuthDriver> {
        return try {
            val response = api.loginWithGoogle(GoogleLoginRequest(email, idToken))
            if (response.success && response.data.sessionId.isNotBlank() && response.data.driver.id > 0) {
                val driver = response.data.driver
                val token = response.data.sessionId
                sessionDataStore.saveSession(
                    token = token,
                    driverId = driver.id.toInt(),
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    driverEmail = driver.email,
                    driverPhoto = driver.imageUrl
                )
                cacheDriverProfile(driver)
                FcmRegistrationWorker.enqueue(context, token)
                ApiResponse.Success(driver.toDomain(token))
            } else {
                ApiResponse.Error(400, ServerErrors.message(response.error, response.message), errorCode = response.error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResponse.Error(500, e.message ?: "Koneksi gagal")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = sessionDataStore.token.firstOrNull()
        return !token.isNullOrBlank()
    }

    override suspend fun registerFcmToken(token: String?): ApiResponse<Unit> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        val driverId = sessionDataStore.driverId.firstOrNull() ?: 0
        if (apiToken.isBlank() || driverId == 0) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        return when (val result = safeApiCall {
            val fcmToken = token?.takeIf(String::isNotBlank) ?: firebaseTokenProvider.getToken()
            sessionDataStore.saveFcmToken(fcmToken)
            val request = FcmTokenRequest(
                token = fcmToken,
                driverId = driverId,
                session = apiToken
            )
            fcmApi.registerFcmToken(request.toJsonObject(), apiToken)
        }) {
            is ApiResponse.Success -> {
                if (result.data.isSuccessful) {
                    ApiResponse.Success(Unit)
                } else {
                    ApiResponse.Error(400, result.data.message.ifBlank { "Gagal register FCM" })
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun logout() {
        FcmRegistrationWorker.cancel(context)
        LocationHelper.stopOnlineDriverService(context)
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        val driverId = sessionDataStore.driverId.firstOrNull() ?: 0
        val fcmToken = sessionDataStore.fcmToken.firstOrNull().orEmpty()
        if (apiToken.isNotBlank() && driverId != 0 && fcmToken.isNotBlank()) {
            val request = FcmTokenRequest(
                token = fcmToken,
                driverId = driverId,
                session = apiToken
            )
            safeApiCall {
                fcmApi.unregisterFcmToken(request.toJsonObject(), apiToken)
            }
        }
        sessionDataStore.clearSession()
    }

    private suspend fun cacheDriverProfile(driver: id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto) {
        sessionDataStore.saveDriverProfile(
            driverId = driver.id.toInt(),
            driverName = driver.name,
            driverPhone = driver.phone,
            driverEmail = driver.email,
            driverPhoto = driver.imageUrl.ifBlank { driver.image },
            balance = driver.balance.toPlainString(),
            withdrawableBalance = driver.withdrawableBalance.toPlainString(),
            online = driver.online
        )
    }
}

private fun id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto.toDomain(token: String) =
    AuthDriver(
        id = id,
        name = name,
        email = email,
        phone = phone,
        imageUrl = imageUrl.ifBlank { image },
        vehicleType = vehicleType,
        vehiclePlate = vehiclePlate,
        cityId = cityId,
        driverType = driverType,
        balance = balance,
        withdrawableBalance = withdrawableBalance,
        online = online,
        token = token
    )
