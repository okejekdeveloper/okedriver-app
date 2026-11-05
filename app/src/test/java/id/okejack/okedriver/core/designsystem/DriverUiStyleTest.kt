package id.okejack.okedriver.core.designsystem

import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverUiStyleTest {
    @Test fun storedChoicesRemainDistinct() {
        assertEquals(DriverUiStyle.SIMPLE, DriverUiStyle.fromStored("simple"))
        assertEquals(DriverUiStyle.DYNAMIC, DriverUiStyle.fromStored("dynamic"))
    }
    @Test fun newAndUnknownPreferencesUseDynamicAppearance() {
        assertEquals(DriverUiStyle.DYNAMIC, DriverUiStyle.fromStored(null))
        assertEquals(DriverUiStyle.DYNAMIC, DriverUiStyle.fromStored(""))
        assertEquals(DriverUiStyle.DYNAMIC, DriverUiStyle.fromStored("unknown-future-choice"))
    }
}
