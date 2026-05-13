package id.okejack.okedriver.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatter {
    private val locale = Locale.forLanguageTag("id-ID")

    fun formatDate(timestampMillis: Long, pattern: String = "dd MMM yyyy"): String =
        SimpleDateFormat(pattern, locale).format(Date(timestampMillis))

    fun formatDateTime(timestampMillis: Long, pattern: String = "dd MMM yyyy HH:mm"): String =
        SimpleDateFormat(pattern, locale).format(Date(timestampMillis))
}
