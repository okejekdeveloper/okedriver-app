package id.okejack.okedriver.core.network.api

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.BuildConfig
import id.okejack.okedriver.core.common.PolylineDecoder
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.core.service.DriverLatLng
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface DirectionsApiService {
    @GET("json")
    suspend fun directions(@Query("origin") origin: String, @Query("destination") destination: String,
        @Query("key") key: String): DirectionsResponse
}
data class DirectionsResponse(val status: String? = null, val routes: List<DirectionsRoute>? = null)
data class DirectionsRoute(@SerializedName("overview_polyline") val polyline: DirectionsPolyline? = null)
data class DirectionsPolyline(val points: String? = null)

@Singleton
class DirectionsClient @Inject constructor() {
    // A dedicated client prevents the driver's bearer token from being sent to Google.
    private val api = Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/directions/")
        .client(OkHttpClient.Builder().callTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(DirectionsApiService::class.java)

    suspend fun route(origin: DriverLatLng, destination: DriverLatLng): List<DriverLatLng> {
        if (BuildConfig.DIRECTIONS_API_KEY.isBlank() || origin == destination) return emptyList()
        val result = safeApiCall { api.directions(origin.asApiValue(), destination.asApiValue(), BuildConfig.DIRECTIONS_API_KEY) }
        return if (result is ApiResponse.Success && result.data.status == "OK")
            PolylineDecoder.decode(result.data.routes?.firstOrNull()?.polyline?.points.orEmpty()) else emptyList()
    }
}
