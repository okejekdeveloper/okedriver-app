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
import id.okejack.okedriver.feature.home.domain.model.AttendanceInfo
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

        val driverDto = driverResponse?.takeIf { it.isSuccessful }?.data?.driver
        val queueEnabled = driverDto?.enableDriverQueue ?: false
        val attendanceEnabled = driverDto?.enableDriverAttendance ?: false

        val queueResult = if (queueEnabled) safeApiCall { api.queueNumber(apiToken) } else null
        val absenceResult = if (queueEnabled || attendanceEnabled) {
            safeApiCall { api.checkAbsenceStatus(apiToken) }
        } else {
            null
        }
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
            toHomeSupportInfo(motd, queue, currentOrder, init, absence, queueEnabled, attendanceEnabled)
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

    override suspend fun checkAttendance(): ApiResponse<AttendanceInfo> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.checkAbsenceStatus(apiToken) }) {
            is ApiResponse.Success -> {
                if (!result.data.isSuccessful) {
                    return ApiResponse.Error(400, attendanceErrorMessage(result.data.error, result.data.message))
                }
                val queue = safeApiCall { api.queueNumber(apiToken) }
                ApiResponse.Success(
                    attendanceInfo(
                        hasAttended = result.data.data.driverStatus,
                        queue = (queue as? ApiResponse.Success)?.data
                    )
                )
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun submitAttendance(): ApiResponse<AttendanceInfo> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.submitStatus(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful || dto.error == "ATTENDANCE_ALREADY_RECORDED") {
                    ApiResponse.Success(
                        AttendanceInfo(
                            hasAttended = true,
                            queueNumber = dto.data.queueNumber,
                            queueTerminal = dto.data.queueTerminal,
                            queueTerminalTotal = dto.data.queueTerminalTotal,
                            queueMessage = dto.data.queueMessage
                        )
                    )
                } else {
                    ApiResponse.Error(400, attendanceErrorMessage(dto.error, dto.message))
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private fun attendanceInfo(
        hasAttended: Boolean,
        queue: HomeQueueResponseDto?
    ): AttendanceInfo =
        AttendanceInfo(
            hasAttended = hasAttended,
            queueNumber = queue?.takeIf { it.isSuccessful }?.data?.queueNumber,
            queueTerminal = queue?.takeIf { it.isSuccessful }?.data?.queueTerminal,
            queueTerminalTotal = queue?.takeIf { it.isSuccessful }?.data?.queueTerminalTotal,
            queueMessage = queue?.takeIf { it.isSuccessful }?.data?.queueMessage.orEmpty()
        )

    private fun attendanceErrorMessage(error: String, message: String): String {
        return when (error) {
            "ATTENDANCE_ALREADY_RECORDED" -> "Anda sudah melakukan absensi hari ini"
            "LOCATION_NOT_ALLOWED" -> message.ifBlank { "Lokasi Anda tidak berada di area absensi" }
            "INSUFFICIENT_BALANCE" -> "Saldo oke driver anda tidak mencukupi, silahkan melakukan top up."
            "OUTSIDE_WORKING_HOURS" -> "Absensi hanya dapat dilakukan pada jam kerja yang ditentukan"
            "LOCATION_NOT_AVAILABLE" -> "Lokasi GPS tidak tersedia. Pastikan GPS aktif dan coba lagi."
            "SESSION_NOT_VALID" -> "Sesi tidak valid. Silakan login ulang."
            "INVALID_LOCATION_FORMAT", "INVALID_COORDINATES" ->
                "Format lokasi GPS tidak valid. Silakan restart aplikasi."
            else -> message.ifBlank { "Gagal menyimpan absensi. Silakan coba lagi." }
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
            return ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
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
