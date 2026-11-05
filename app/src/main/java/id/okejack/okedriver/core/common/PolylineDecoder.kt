package id.okejack.okedriver.core.common

import id.okejack.okedriver.core.service.DriverLatLng

object PolylineDecoder {
    fun decode(encoded: String): List<DriverLatLng> = runCatching {
        if (encoded.length > 200_000) return emptyList()
        var index = 0
        var latitude = 0L
        var longitude = 0L
        val result = mutableListOf<DriverLatLng>()
        fun component(): Long {
            var value = 0L
            var shift = 0
            var byte: Int
            do {
                require(index < encoded.length && shift <= 30)
                byte = encoded[index++].code - 63
                require(byte in 0..63)
                value = value or ((byte and 31).toLong() shl shift)
                shift += 5
            } while (byte >= 32)
            return if (value and 1L != 0L) (value shr 1).inv() else value shr 1
        }
        while (index < encoded.length) {
            latitude += component()
            longitude += component()
            val point = DriverLatLng(latitude / 1e5, longitude / 1e5)
            require(point.latitude in -90.0..90.0 && point.longitude in -180.0..180.0)
            result += point
        }
        result
    }.getOrDefault(emptyList())
}
