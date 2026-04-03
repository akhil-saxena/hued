package app.hued.processing

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import javax.inject.Inject

data class ImageReference(
    val uri: Uri,
    val timestamp: Long,
    val folderPath: String,
)

class GalleryScanner @Inject constructor(
    private val context: Context,
) {

    fun scanGallery(
        excludedFolders: List<String> = emptyList(),
        sinceTimestamp: Long = 0,
    ): List<ImageReference> {
        val images = mutableListOf<ImageReference>()
        val contentResolver: ContentResolver = context.contentResolver

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
        )

        val selection = if (sinceTimestamp > 0) {
            "${MediaStore.Images.Media.DATE_ADDED} > ?"
        } else null

        val selectionArgs = if (sinceTimestamp > 0) {
            arrayOf((sinceTimestamp / 1000).toString())
        } else null

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateTaken = cursor.getLong(dateTakenColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                val filePath = cursor.getString(dataColumn) ?: ""

                val timestamp = if (dateTaken > 0) dateTaken else dateAdded
                val folderPath = filePath.substringBeforeLast("/")

                if (excludedFolders.any { folderPath.contains(it, ignoreCase = true) }) continue

                val contentUri = Uri.withAppendedPath(collection, id.toString())
                images.add(ImageReference(contentUri, timestamp, folderPath))
            }
        }

        return images
    }
}
