package id.okejack.okedriver.feature.wallet.domain.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object TopUpExpiry {
    fun parseMillis(value: String): Long? {
        if (value.isBlank()) return null
        for (pattern in listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")) {
            val parsed = runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                }.parse(value)?.time
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }
}
