package id.okejack.okedriver.feature.order.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderDistanceTest {
    @Test fun aShortTripIsShownInKilometresNotMetres() {
        assertEquals("2 km", formatOrderDistance(2))
    }

    @Test fun longerTripsKeepTheKilometreUnit() {
        assertEquals("12 km", formatOrderDistance(12))
        assertEquals("1 km", formatOrderDistance(1))
    }

    @Test fun missingDistanceIsReportedAsUnavailableNotAsZero() {
        assertEquals("Jarak belum tersedia", formatOrderDistance(0))
    }

    @Test fun aNegativeDistanceIsTreatedAsUnavailable() {
        assertEquals("Jarak belum tersedia", formatOrderDistance(-1))
    }
}
