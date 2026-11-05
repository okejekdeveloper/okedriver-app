package id.okejack.okedriver.feature.order.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverOrderTest {

    @Test
    fun currentOrderCancelReasons_matchLegacyReasonCodes() {
        val codes = DriverOrder.currentOrderCancelReasons.map { it.code }

        assertEquals(4, codes.size)
        assertTrue(DriverOrder.CANCEL_REASON_NO_CONTACT in codes)
        assertTrue(DriverOrder.CANCEL_REASON_SHOP_CLOSED in codes)
        assertTrue(DriverOrder.CANCEL_REASON_CUSTOMER_CANCEL in codes)
        assertTrue(DriverOrder.CANCEL_REASON_LOCATION_UNREACHABLE in codes)
    }
}
