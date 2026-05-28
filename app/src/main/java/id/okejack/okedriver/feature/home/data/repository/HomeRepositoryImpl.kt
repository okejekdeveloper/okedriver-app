package id.okejack.okedriver.feature.home.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto
import id.okejack.okedriver.feature.home.data.remote.HomeApiService
import id.okejack.okedriver.feature.home.data.remote.dto.HomeDriverResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.toHomeDriver
import id.okejack.okedriver.feature.home.data.remote.dto.toHomeSupportInfo
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApiService,
    private val sessionDataStore: SessionDataStore
) : HomeRepository {

    override suspend fun getDriverInfo(): ApiResponse<HomeDriver> =
        callWithSession { apiToken -> api.driverInfo(apiToken) }

    override suspend fun toggleOnlineStatus(): ApiResponse<HomeDriver> =
        callWithSession { apiToken -> api.toggleStatus(apiToken) }

    override suspend fun getSupportInfo(): ApiResponse<HomeSupportInfo> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        val motdResult = safeApiCall { api.motd(apiToken) }
        val queueResult = safeApiCall { api.queueNumber(apiToken) }
        val currentOrderResult = safeApiCall { api.currentOrder(apiToken) }
        val motd = (motdResult as? ApiResponse.Success)?.data
        val queue = (queueResult as? ApiResponse.Success)?.data
        val currentOrder = (currentOrderResult as? ApiResponse.Success)?.data

        if (motd == null && queue == null && currentOrder == null) {
            return (motdResult as? ApiResponse.Error)
                ?: (queueResult as? ApiResponse.Error)
                ?: (currentOrderResult as? ApiResponse.Error)
                ?: ApiResponse.Error(400, "Gagal memuat info dashboard")
        }

        return ApiResponse.Success(toHomeSupportInfo(motd, queue, currentOrder))
    }

    private suspend fun callWithSession(
        request: suspend (String) -> HomeDriverResponseDto
    ): ApiResponse<HomeDriver> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        return when (val result = safeApiCall { request(apiToken) }) {
            is ApiResponse.Success -> mapResponse(result.data, apiToken)
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun mapResponse(
        response: HomeDriverResponseDto,
        currentToken: String
    ): ApiResponse<HomeDriver> {
        if (!response.isSuccessful) {
            return ApiResponse.Error(400, response.errorMessage)
        }

        val driverDto = response.data.driver
        val sessionId = response.data.sessionId
        val effectiveDriver = driverDto.copy(
            online = response.data.isOnline ?: driverDto.online
        )
        cacheDriverProfile(effectiveDriver, sessionId.ifBlank { currentToken })

        return ApiResponse.Success(effectiveDriver.toHomeDriver())
    }

    private suspend fun cacheDriverProfile(driver: DriverDto, token: String) {
        if (token.isNotBlank()) {
            sessionDataStore.saveSession(
                token = token,
                driverId = driver.id.toInt(),
                driverName = driver.name,
                driverPhone = driver.phone,
                driverEmail = driver.email,
                driverPhoto = driver.imageUrl.ifBlank { driver.image }
            )
        }
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
