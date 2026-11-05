package id.okejack.okedriver.feature.wallet.domain.model

import org.junit.Assert.*
import org.junit.Test

class TopUpExpiryTest {
    @Test fun serverTimestampUsesJakartaTimezone() {
        assertEquals(TopUpExpiry.parseMillis("2026-10-06T05:00:00Z"), TopUpExpiry.parseMillis("2026-10-06 12:00:00"))
    }
    @Test fun missingOrInvalidExpiryDoesNotInventDeadline() {
        assertNull(TopUpExpiry.parseMillis(""))
        assertNull(TopUpExpiry.parseMillis("invalid"))
        assertNull(TopUpExpiry.parseMillis("2026-02-31 12:00:00"))
    }
}
