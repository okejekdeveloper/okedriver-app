package id.okejack.okedriver.feature.auth.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.data.remote.AuthApiService
import id.okejack.okedriver.feature.auth.data.remote.dto.GoogleLoginRequest
import id.okejack.okedriver.feature.auth.data.remote.dto.LoginRequest
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResponse<AuthDriver> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.success) {
                val driver = response.data.driver
                val token = response.data.sessionId
                sessionDataStore.saveSession(
                    token = token,
                    driverId = driver.id.toInt(),
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    driverPhoto = driver.imageUrl
                )
                ApiResponse.Success(driver.toDomain(token))
            } else {
                ApiResponse.Error(400, response.error.ifBlank { response.message })
            }
        } catch (e: Exception) {
            ApiResponse.Error(500, e.message ?: "Koneksi gagal")
        }
    }

    override suspend fun loginWithGoogle(email: String, idToken: String): ApiResponse<AuthDriver> {
        return try {
            val response = api.loginWithGoogle(GoogleLoginRequest(email, idToken))
            if (response.success) {
                val driver = response.data.driver
                val token = response.data.sessionId
                sessionDataStore.saveSession(
                    token = token,
                    driverId = driver.id.toInt(),
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    driverPhoto = driver.imageUrl
                )
                ApiResponse.Success(driver.toDomain(token))
            } else {
                ApiResponse.Error(400, response.error.ifBlank { response.message })
            }
        } catch (e: Exception) {
            ApiResponse.Error(500, e.message ?: "Koneksi gagal")
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = sessionDataStore.token.firstOrNull()
        return !token.isNullOrBlank()
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
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
