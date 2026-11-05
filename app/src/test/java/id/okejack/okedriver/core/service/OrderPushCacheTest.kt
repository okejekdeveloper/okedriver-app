package id.okejack.okedriver.core.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderPushCacheTest {
    @Test fun returnsThePushedJsonWhileTheOfferIsFresh() {
        val cache = OrderPushCache()
        cache.put(10, """{"id":10}""", nowMs = 1_000)
        assertEquals("""{"id":10}""", cache.get(10, nowMs = 1_000 + OrderPushCache.MAX_AGE_MS))
    }

    @Test fun forgetsAnOfferOnceItIsTooOldToBeOpen() {
        val cache = OrderPushCache()
        cache.put(10, """{"id":10}""", nowMs = 1_000)
        assertNull(cache.get(10, nowMs = 1_000 + OrderPushCache.MAX_AGE_MS + 1))
    }

    @Test fun neverReturnsAnotherOrdersPayload() {
        val cache = OrderPushCache()
        cache.put(10, """{"id":10}""", nowMs = 0)
        assertNull(cache.get(11, nowMs = 0))
    }

    @Test fun ignoresPushesWithoutAUsableIdOrBody() {
        val cache = OrderPushCache()
        cache.put(0, """{"id":0}""", nowMs = 0)
        cache.put(10, "  ", nowMs = 0)
        assertNull(cache.get(0, nowMs = 0))
        assertNull(cache.get(10, nowMs = 0))
    }

    @Test fun keepsOnlyTheMostRecentOffers() {
        val cache = OrderPushCache()
        (1L..6L).forEach { cache.put(it, """{"id":$it}""", nowMs = 0) }
        assertNull(cache.get(1, nowMs = 0))
        assertEquals("""{"id":6}""", cache.get(6, nowMs = 0))
    }
}
