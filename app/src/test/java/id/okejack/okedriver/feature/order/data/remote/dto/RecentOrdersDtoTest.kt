package id.okejack.okedriver.feature.order.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentOrdersDtoTest {

    @Test
    fun response_isSuccessful_acceptsLegacySuccessOrBaseStatus() {
        assertTrue(RecentOrdersResponseDto(success = true).isSuccessful)
        assertTrue(RecentOrdersResponseDto(status = true).isSuccessful)
        assertFalse(RecentOrdersResponseDto().isSuccessful)
    }

    @Test
    fun toCoordinateList_ignoresInvalidAndZeroCoordinates() {
        val coordinates = RecentOrdersResponseDto(
            success = true,
            data = RecentOrdersDataDto(
                coordinates = listOf(
                    "-6.1,106.7",
                    "0.0,0.0",
                    "invalid",
                    "-6.2, 106.8"
                )
            )
        ).toCoordinateList()

        assertEquals(2, coordinates.size)
        assertEquals(-6.1, coordinates[0].latitude, 0.0)
        assertEquals(106.7, coordinates[0].longitude, 0.0)
        assertEquals(-6.2, coordinates[1].latitude, 0.0)
        assertEquals(106.8, coordinates[1].longitude, 0.0)
    }
}
