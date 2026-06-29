package app.hued

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.hued.ui.HuedApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Default edge-to-edge uses auto system-bar styles that adapt icon colors to light/dark.
        enableEdgeToEdge()
        cleanShareCache()
        val openWeekly = intent?.getBooleanExtra("open_weekly", false) ?: false
        setContent {
            HuedApp(openWeekly = openWeekly)
        }
    }

    private fun cleanShareCache() {
        val cacheDir = java.io.File(cacheDir, "share_cards")
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }
}
