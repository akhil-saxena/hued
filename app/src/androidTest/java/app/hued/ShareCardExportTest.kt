package app.hued

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.hued.ui.share.ShareCardExporter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareCardExportTest {

    @Test
    fun exportShareCards() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val count = ShareCardExporter.exportAll(context)
        assert(count > 0) { "No cards exported" }
        // PNGs are in /data/data/app.hued/cache/share_exports/
        // Pull via: adb shell run-as app.hued cat cache/share_exports/01_this_week.png > file.png
    }
}
