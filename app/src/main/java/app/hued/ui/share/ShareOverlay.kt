package app.hued.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.hued.data.model.TimePeriod
import app.hued.ui.components.PillButton
import app.hued.ui.main.PeriodPaletteUi
import app.hued.ui.theme.HuedCanvasResting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ShareOverlay(
    palette: PeriodPaletteUi,
    period: TimePeriod,
    isCurrent: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var stage by remember { mutableIntStateOf(0) }

    LaunchedEffect(palette) {
        bitmap = withContext(Dispatchers.Default) {
            ShareCardRenderer.renderBitmap(context, palette, period, isCurrent)
        }
        delay(50)
        stage = 1
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 0.85f else 0f,
        animationSpec = tween(300),
        label = "scrim",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0.85f,
        animationSpec = tween(350),
        label = "cardScale",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(300),
        label = "cardAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim layer — blocks touches from reaching screen below
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* consume */ },
        )
        // Content layer — card + pills
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .scale(cardScale)
                    .alpha(cardAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val currentBitmap = bitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "Share card",
                        modifier = Modifier
                            .width(260.dp)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HuedCanvasResting.copy(alpha = 0.3f)),
                    )
                }

                Spacer(Modifier.height(28.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton(
                        text = "save",
                        onClick = { bitmap?.let { saveBitmapToGallery(context, it) } },
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    PillButton(
                        text = "share",
                        onClick = { bitmap?.let { shareBitmap(context, it) } },
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    PillButton(
                        text = "close",
                        onClick = onDismiss,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val file = writeBitmapToCache(context, bitmap)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    try {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "hued_palette_${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/hued")
            put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
        )
        if (uri == null) {
            android.widget.Toast.makeText(context, "Could not save image", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }
        values.clear()
        values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
        android.widget.Toast.makeText(context, "Saved to Pictures/hued", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        android.util.Log.e("ShareOverlay", "Failed to save to gallery", e)
        android.widget.Toast.makeText(context, "Could not save image", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun writeBitmapToCache(context: Context, bitmap: Bitmap): File {
    val cachePath = File(context.cacheDir, "share_cards")
    cachePath.mkdirs()
    val file = File(cachePath, "hued_palette.png")
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
    }
    return file
}
