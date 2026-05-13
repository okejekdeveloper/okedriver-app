package id.okejack.okedriver.core.common

import android.content.Context
import android.os.Build
import id.okejack.okedriver.BuildConfig

object PackageHelper {
    fun versionName(context: Context): String {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            buildString {
                append("v")
                append(packageInfo.versionName ?: BuildConfig.VERSION_NAME)
                if (BuildConfig.DEBUG) append("-dev")
            }
        }.getOrElse {
            "v${BuildConfig.VERSION_NAME}${if (BuildConfig.DEBUG) "-dev" else ""}"
        }
    }

    fun versionCode(context: Context): Long {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        }.getOrElse {
            BuildConfig.VERSION_CODE.toLong()
        }
    }
}
