package id.okejack.okedriver.core.service

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import id.okejack.okedriver.R

/** Legacy monochrome status icon and the purple legacy app logo in the notification body. */
internal fun NotificationCompat.Builder.withDriverBrand(context: Context): NotificationCompat.Builder =
    setSmallIcon(R.drawable.ic_stat_onesignal_default)
        .setColor(ContextCompat.getColor(context, R.color.oke_brand_purple))
        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.oke_driver_mark))
