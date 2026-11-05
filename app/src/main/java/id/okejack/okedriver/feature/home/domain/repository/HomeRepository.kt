package id.okejack.okedriver.feature.home.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.home.domain.model.AttendanceInfo
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo

interface HomeRepository {
    suspend fun getDriverInfo(): ApiResponse<HomeDriver>
    suspend fun toggleOnlineStatus(): ApiResponse<HomeDriver>
    suspend fun getSupportInfo(): ApiResponse<HomeSupportInfo>
    suspend fun registerQueue(latLng: String, terminal: String): ApiResponse<HomeSupportInfo>
    suspend fun checkAttendance(): ApiResponse<AttendanceInfo>
    suspend fun submitAttendance(): ApiResponse<AttendanceInfo>
}
