package id.okejack.okedriver.feature.auth.data.remote.dto

import com.google.gson.JsonObject

fun FcmTokenRequest.toJsonObject(): JsonObject =
    JsonObject().apply {
        addProperty("token", token)
        addProperty("driver_id", driverId)
        addProperty("session", session)
    }
