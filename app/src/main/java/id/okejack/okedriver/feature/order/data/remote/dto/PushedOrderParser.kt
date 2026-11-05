package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.Gson
import com.google.gson.JsonParseException
import id.okejack.okedriver.feature.order.domain.model.DriverOrder

/** Reads the `order` JSON embedded in a "job_received" push; it has the same shape as `data.order` of the API. */
object PushedOrderParser {
    private val gson = Gson()

    sealed interface Result {
        data class Parsed(val order: DriverOrder) : Result
        /** [reason] names the problem (never the payload, which holds customer details) so it is safe to log. */
        data class Failed(val reason: String) : Result
    }

    /**
     * [pushedOrderId] is the push's own `order_id`. The embedded JSON is only used for its details, so a missing
     * `id` inside it falls back to that value; a conflicting `id` is rejected rather than showing the wrong order.
     */
    fun parse(json: String, pushedOrderId: Long): Result {
        if (json.isBlank()) return Result.Failed("payload is empty")
        val dto = try {
            gson.fromJson(json, OrderDto::class.java)
        } catch (e: JsonParseException) {
            return Result.Failed("payload is not a readable order (${e.message?.take(120)})")
        } ?: return Result.Failed("payload is null")
        val embeddedId = dto.id?.takeIf { it > 0 }
        if (embeddedId != null && embeddedId != pushedOrderId) {
            return Result.Failed("payload is for order $embeddedId, push was for $pushedOrderId")
        }
        if (pushedOrderId <= 0) return Result.Failed("push has no usable order id")
        // An id alone is not an offer: without any place on it the driver would be shown a blank order.
        if (dto.originAddress.isNullOrBlank() && dto.destinationAddress.isNullOrBlank() &&
            dto.originLatLng.isNullOrBlank() && dto.destinationLatLng.isNullOrBlank()
        ) return Result.Failed("payload has no pickup or destination")
        return Result.Parsed(dto.copy(id = pushedOrderId).toDomain())
    }
}
