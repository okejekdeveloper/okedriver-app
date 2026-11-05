package id.okejack.okedriver.core.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SlideConfirmTest {
    @Test fun confirmsOnlyAfterThumbTravelsMostOfTheTrack() {
        assertFalse(SlideConfirm.isConfirmed(offset = 0f, maxDrag = 100f))
        assertFalse(SlideConfirm.isConfirmed(offset = 84f, maxDrag = 100f))
        assertTrue(SlideConfirm.isConfirmed(offset = 85f, maxDrag = 100f))
        assertTrue(SlideConfirm.isConfirmed(offset = 100f, maxDrag = 100f))
    }

    @Test fun zeroWidthTrackNeverConfirms() {
        // Track width is 0 until the first layout pass; a tap or stray drag must not trigger the action.
        assertFalse(SlideConfirm.isConfirmed(offset = 0f, maxDrag = 0f))
        assertFalse(SlideConfirm.isConfirmed(offset = 50f, maxDrag = 0f))
    }
}
