package id.okejack.okedriver.core.service

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import id.okejack.okedriver.R
import org.junit.Assert.*
import org.junit.Test

class DriverNotificationBrandTest {
    @Test fun notificationUsesLegacySilhouetteAndPurpleBrandResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notification = NotificationCompat.Builder(context, "brand-test")
            .withDriverBrand(context).setContentTitle("Oke Driver").build()
        assertEquals(R.drawable.ic_stat_onesignal_default, notification.smallIcon.resId)
        assertEquals(ContextCompat.getColor(context, R.color.oke_brand_purple), notification.color)
        assertNotNull(notification.getLargeIcon())
    }
}
