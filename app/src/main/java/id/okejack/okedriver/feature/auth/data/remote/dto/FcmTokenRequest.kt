package id.okejack.okedriver.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    val token: String,
    @SerializedName("driver_id") val driverId: Int,
    val session: String
)
