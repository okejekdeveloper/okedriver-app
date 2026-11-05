package id.okejack.okedriver.core.common

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrImageSaver {
    suspend fun save(context: Context, url: String, name: String): Uri = withContext(Dispatchers.IO) {
        require(url.isNotBlank()) { "QRIS belum tersedia" }
        val image = context.imageLoader.execute(ImageRequest.Builder(context).data(url).allowHardware(false).build())
        val bitmap = (image as? SuccessResult)?.drawable?.toBitmap() ?: error("Gagal mengunduh QRIS")
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name.replace(Regex("[^a-zA-Z0-9._-]"), "_"))
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OkeDriver")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("Gagal membuat file QRIS")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Gagal menyimpan QRIS" }
            } ?: error("Gagal menulis QRIS")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }
}
