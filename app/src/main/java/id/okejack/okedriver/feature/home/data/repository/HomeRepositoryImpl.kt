package id.okejack.okedriver.feature.home.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto
import id.okejack.okedriver.feature.home.data.remote.HomeApiService
import id.okejack.okedriver.feature.home.data.remote.dto.HomeQueueResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.HomeDriverResponseDto
import id.okejack.okedriver.feature.home.data.remote.dto.toHomeDriver
import id.okejack.okedriver.feature.home.data.remote.dto.toHomeSupportInfo
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import com.google.gson.JsonObject
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
        val driverResult = safeApiCall { api.driverInfo(apiToken) }
        val currentOrderResult = safeApiCall { api.currentOrder(apiToken) }
        val initResult = safeApiCall { api.init() }
        val motd = (motdResult as? ApiResponse.Success)?.data
        val driverResponse = (driverResult as? ApiResponse.Success)?.data
        val currentOrder = (currentOrderResult as? ApiResponse.Success)?.data
        val init = (initResult as? ApiResponse.Success)?.data

        val queueEnabled = driverResponse
            ?.takeIf { it.isSuccessful }
            ?.data?.driver?.enableDriverQueue
            ?: false

        val queueResult = if (queueEnabled) safeApiCall { api.queueNumber(apiToken) } else null
        val absenceResult = if (queueEnabled) safeApiCall { api.checkAbsenceStatus(apiToken) } else null
        val queue = (queueResult as? ApiResponse.Success)?.data
        val absence = (absenceResult as? ApiResponse.Success)?.data

        if (motd == null && driverResponse == null && currentOrder == null && init == null) {
            return (motdResult as? ApiResponse.Error)
                ?: (driverResult as? ApiResponse.Error)
                ?: (currentOrderResult as? ApiResponse.Error)
                ?: (initResult as? ApiResponse.Error)
                ?: ApiResponse.Error(400, "Gagal memuat info dashboard")
        }

        return ApiResponse.Success(
            toHomeSupportInfo(motd, queue, currentOrder, init, absence, queueEnabled)
        )
    }

    override suspend fun registerQueue(
        latLng: String,
        terminal: String
    ): ApiResponse<HomeSupportInfo> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) {
            return ApiResponse.Error(401, "Session tidak valid")
        }

        val body = JsonObject().apply {
            addProperty("latlng", latLng)
            addProperty("terminal", terminal)
        }

        return when (val result = safeApiCall { api.registerQueue(body, apiToken) }) {
            is ApiResponse.Success -> mapQueueResponse(result.data)
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun checkAttendance(): ApiResponse<Boolean> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.checkAbsenceStatus(apiToken) }) {
            is ApiResponse.Success -> {
                val attended = result.data.isSuccessful && result.data.data.driverStatus
                ApiResponse.Success(attended)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun submitAttendance(): ApiResponse<Unit> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.submitStatus(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful) ApiResponse.Success(Unit)
                else ApiResponse.Error(400, dto.errorMessage)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private fun mapQueueResponse(response: HomeQueueResponseDto): ApiResponse<HomeSupportInfo> {
        if (!response.isSuccessful) {
            return ApiResponse.Error(
                400,
                response.error.ifBlank { response.message.ifBlank { "Gagal masuk antrean" } }
            )
        }
        return ApiResponse.Success(toHomeSupportInfo(null, response, null, null, queueEnabled = true))
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
