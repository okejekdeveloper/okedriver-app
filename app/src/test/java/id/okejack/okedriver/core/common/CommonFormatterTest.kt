package id.okejack.okedriver.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommonFormatterTest {
    @Test
    fun rupiah_formatsWholeNumberWithCurrencyPrefix() {
        val formatted = CurrencyFormatter.rupiah(12345)

        assertTrue(formatted.startsWith("Rp"))
        assertTrue(formatted.contains("12.345"))
    }

    @Test
    fun dateFormatter_usesRequestedPattern() {
        val formatted = DateTimeFormatter.formatDate(0L, "yyyy-MM-dd")

        assertEquals("1970-01-01", formatted)
    }
}
