package id.okejack.okedriver.feature.order.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushedOrderParserTest {
    private fun parsed(json: String, pushedId: Long = 3580961) =
        PushedOrderParser.parse(json, pushedId) as PushedOrderParser.Result.Parsed

    private fun failed(json: String, pushedId: Long = 3580961) =
        PushedOrderParser.parse(json, pushedId) as PushedOrderParser.Result.Failed

    @Test fun readsTheSameShapeTheApiReturnsForAnOrder() {
        val order = parsed(
            """{"id":3580961,"status":0,"type":0,"origin_address":"A","destination_address":"B","fee":12000,"distance":3200}"""
        ).order
        assertEquals(3580961L, order.id)
        assertEquals("A", order.originAddress)
        assertEquals(3200, order.distance)
    }

    @Test fun anOrderWithoutAnIdTakesTheIdFromThePush() {
        assertEquals(3580961L, parsed("""{"status":0,"origin_address":"A"}""").order.id)
    }

    @Test fun anOrderForADifferentIdIsRejectedRatherThanShownAsTheWrongOrder() {
        assertTrue(failed("""{"id":999,"status":0,"origin_address":"A"}""").reason.contains("999"))
    }

    @Test fun numbersAndIdsSentAsStringsStillRead() {
        val order = parsed("""{"id":"3580961","status":"0","distance":"3200","fee":"12000","origin_address":"A"}""").order
        assertEquals(3580961L, order.id)
        assertEquals(3200, order.distance)
    }

    @Test fun unreadableOrEmptyPayloadsAreReportedNotThrown() {
        listOf("", "   ", "not json", "null", """{"user":"should be an object"}""", "[1,2]", "{}").forEach { json ->
            assertTrue("'$json' must be rejected", PushedOrderParser.parse(json, 3580961) is PushedOrderParser.Result.Failed)
        }
    }

    @Test fun anIdWithoutAnyPlaceIsNotAnOffer() {
        assertTrue(failed("""{"id":3580961,"status":0,"fee":12000}""").reason.contains("pickup or destination"))
    }

    @Test fun failureReasonNeverContainsThePayload() {
        assertFalse(failed("""{"user":{"phone":"0811111111"}} trailing""").reason.contains("0811111111"))
    }

    @Test fun aPushWithoutAUsableIdIsRejected() {
        assertTrue(PushedOrderParser.parse("""{"status":0,"origin_address":"A"}""", 0) is PushedOrderParser.Result.Failed)
    }
}
