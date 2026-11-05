package id.okejack.okedriver.core.common

import org.junit.Assert.*
import org.junit.Test

class PolylineDecoderTest {
    @Test fun decodesKnownGooglePolyline() {
        val points = PolylineDecoder.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        assertEquals(3, points.size)
        assertEquals(38.5, points[0].latitude, 0.00001)
        assertEquals(-120.2, points[0].longitude, 0.00001)
        assertEquals(43.252, points[2].latitude, 0.00001)
        assertEquals(-126.453, points[2].longitude, 0.00001)
    }

    @Test fun incompleteOrUnboundedInputCannotCrashMap() {
        for (encoded in listOf("", "?", "_p~iF~ps|U_ulL", "~".repeat(100), "<html>error</html>", "?".repeat(200_001))) {
            assertTrue(PolylineDecoder.decode(encoded).isEmpty())
        }
    }
}
