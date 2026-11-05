package id.okejack.okedriver.core.network.retrofit

import id.okejack.okedriver.core.datastore.SessionDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.JsonParser

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking(Dispatchers.IO) { sessionDataStore.token.firstOrNull() }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept", "application/json")
        }.build()

        val response = chain.proceed(request)
        val legacySessionExpired = if (response.header("Content-Type").orEmpty().contains("json")) {
            runCatching {
                JsonParser.parseString(response.peekBody(16_384).string()).asJsonObject
                    .get("error")?.asString == "session_not_valid"
            }.getOrDefault(false)
        } else false
        if ((response.code == 401 || legacySessionExpired) && !token.isNullOrBlank()) {
            runBlocking(Dispatchers.IO) { sessionDataStore.expireSession(token) }
        }
        return response
    }
}
