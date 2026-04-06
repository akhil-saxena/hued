package app.hued.processing

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ImageReference(
    val uri: Uri,
    val timestamp: Long,
    val folderPath: String,
)

data class FolderInfo(
    val path: String,
    val imageCount: Int,
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
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        val selection = if (sinceTimestamp > 0) {
            "${MediaStore.Images.Media.DATE_ADDED} > ?"
        } else null

        val selectionArgs = if (sinceTimestamp > 0) {
            arrayOf((sinceTimestamp / 1000).toString())
        } else null

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val cursor = contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
        if (cursor == null) {
            Log.w("GalleryScanner", "Gallery query returned null — permission may have been revoked")
            return emptyList()
        }
        cursor.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateTaken = cursor.getLong(dateTakenColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                val filePath = cursor.getString(dataColumn) ?: ""
                val displayName = cursor.getString(displayNameColumn) ?: ""

                val timestamp = if (dateTaken > 0) dateTaken
                    else parseTimestampFromFilename(displayName) ?: dateAdded
                val folderPath = filePath.substringBeforeLast("/")

                if (excludedFolders.any { folderPath.contains(it, ignoreCase = true) }) continue

                val contentUri = Uri.withAppendedPath(collection, id.toString())
                images.add(ImageReference(contentUri, timestamp, folderPath))
            }
        }

        return images
    }

    /**
     * Queries MediaStore for all unique folder paths and their image counts.
     * Used on launch to detect new folders the user hasn't seen yet.
     */
    fun discoverAllFolders(): List<FolderInfo> {
        val folderCounts = mutableMapOf<String, Int>()
        val contentResolver = context.contentResolver

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(collection, projection, null, null, null)
        cursor?.use {
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val filePath = it.getString(dataColumn) ?: continue
                val folder = filePath.substringBeforeLast("/")
                folderCounts[folder] = (folderCounts[folder] ?: 0) + 1
            }
        }

        return folderCounts.map { (path, count) -> FolderInfo(path, count) }
    }

    private val filenameTimestampPattern = Regex("""(\d{4})(\d{2})(\d{2})_(\d{2})(\d{2})(\d{2})""")

    private fun parseTimestampFromFilename(filename: String): Long? {
        val match = filenameTimestampPattern.find(filename) ?: return null
        return try {
            val (y, mo, d, h, mi, s) = match.destructured
            val dt = LocalDateTime.of(y.toInt(), mo.toInt(), d.toInt(), h.toInt(), mi.toInt(), s.toInt())
            dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
