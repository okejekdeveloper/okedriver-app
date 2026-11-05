package id.okejack.okedriver.core.network.model

import org.junit.Assert.*
import org.junit.Test

class ServerErrorsTest {
    @Test fun machineCodesAreNotDisplayedAsUserMessages() {
        for (code in listOf("user_suspended", "payment_failed", "session_not_valid", "pending_withdraw_request", "max_shopping_edit")) {
            assertNotEquals(code, ServerErrors.message(code))
            assertTrue(ServerErrors.message(code).isNotBlank())
        }
        assertEquals("Gagal memuat data", ServerErrors.message("UNKNOWN_CODE", "", "Gagal memuat data"))
    }

    @Test fun serverExplanationsAndCaseInsensitiveAttendanceCodesRemainReadable() {
        assertEquals("Tunggu 2 menit lagi", ServerErrors.message("order_progress_delayed", "Tunggu 2 menit lagi"))
        assertEquals(ServerErrors.message("location_not_allowed"), ServerErrors.message("LOCATION_NOT_ALLOWED"))
        assertEquals("Bank sedang dalam pemeliharaan", ServerErrors.message("unknown", "Bank sedang dalam pemeliharaan"))
    }
}
