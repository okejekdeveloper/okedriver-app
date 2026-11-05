package id.okejack.okedriver.core.network.model

import com.google.gson.annotations.SerializedName

data class AppInitDataDto(@SerializedName(value = "initSettings", alternate = ["init_settings"]) val initSettings: AppInitSettingsDto? = null)

data class AppInitSettingsDto(
    @SerializedName("current_app_version_code") val currentVersionCode: Int = 0,
    @SerializedName("current_app_version") val currentVersionName: String = "",
    @SerializedName("min_app_version_code") val minimumVersionCode: Int = 0,
    @SerializedName("payment_bidding_range") val paymentBiddingRange: List<Int>? = null,
    @SerializedName("distance_bidding_range") val distanceBiddingRange: List<Int>? = null
) {
    fun requiresUpdate(installedCode: Int): Boolean = minimumVersionCode > 0 && installedCode < minimumVersionCode
    fun hasUpdate(installedCode: Int): Boolean = requiresUpdate(installedCode) || installedCode < currentVersionCode
}
