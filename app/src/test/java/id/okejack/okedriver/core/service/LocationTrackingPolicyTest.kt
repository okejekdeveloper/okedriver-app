package id.okejack.okedriver.core.service

import org.junit.Assert.*
import org.junit.Test

class LocationTrackingPolicyTest {
    @Test fun locationPermissionAndOnlineStatusDoNotReplaceConsent() {
        assertFalse(LocationTrackingPolicy.canAccess(0, true))
        assertFalse(LocationTrackingPolicy.canAccess(2, true))
        assertFalse(LocationTrackingPolicy.canTrack(0, true, true, true, "session"))
        assertTrue(LocationTrackingPolicy.canAccess(1, true))
    }

    @Test fun trackingRequiresEveryAuthorizationCondition() {
        for (version in 0..2) for (permission in listOf(false, true)) for (requested in listOf(false, true))
            for (online in listOf(false, true)) for (session in listOf("", " ", "session")) {
                assertEquals(version == 1 && permission && requested && online && session.isNotBlank(),
                    LocationTrackingPolicy.canTrack(version, permission, requested, online, session))
            }
    }
}
