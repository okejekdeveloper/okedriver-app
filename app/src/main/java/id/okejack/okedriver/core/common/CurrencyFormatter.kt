package id.okejack.okedriver.core.common

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale.forLanguageTag("id-ID")

    fun rupiah(value: Number): String =
        NumberFormat.getCurrencyInstance(locale)
            .apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }
            .format(value)
            .replace("Rp", "Rp ")

    fun rupiah(value: BigDecimal): String = rupiah(value.toLong())
}
