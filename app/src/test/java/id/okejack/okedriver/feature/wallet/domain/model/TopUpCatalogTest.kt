package id.okejack.okedriver.feature.wallet.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

class TopUpCatalogTest {
    private fun at(time: String) = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").apply {
        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    }.parse("2026-10-28 $time")!!.time

    @Test fun bankTransfersRespectJakartaOpeningAndClosingTimes() {
        for (id in listOf(7, 5, 6, 9)) {
            assertFalse(TopUpCatalog.isAvailable(id, at("04:59:59")))
            assertTrue(TopUpCatalog.isAvailable(id, at("05:00:00")))
            assertTrue(TopUpCatalog.isAvailable(id, at("20:59:59")))
            assertFalse(TopUpCatalog.isAvailable(id, at("21:00:00")))
        }
    }

    @Test fun qrisAndVirtualAccountsRemainAvailableAtNight() {
        for (id in listOf(16, 22, 23, 24, 25)) {
            assertTrue(TopUpCatalog.isAvailable(id, at("02:00:00")))
            assertTrue(TopUpCatalog.isAvailable(id, at("23:00:00")))
        }
        assertFalse(TopUpCatalog.isAvailable(999))
        assertEquals(9, TopUpCatalog.methods.map { it.id }.distinct().size)
    }
}
