package app.hued.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hued.ui.components.PillButton
import app.hued.ui.theme.LocalHuedTextMuted

@Composable
fun NewFoldersDialog(
    folders: List<FolderUiState>,
    onToggle: (path: String, include: Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                "new folders found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Column {
                Text(
                    "include these in your color history?",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalHuedTextMuted.current,
                )
                Spacer(modifier = Modifier.height(16.dp))
                folders.forEach { folder ->
                    FolderRow(
                        folder = folder,
                        onToggle = { onToggle(folder.path, it) },
                    )
                }
            }
        },
        confirmButton = {
            PillButton(
                text = "process",
                onClick = onConfirm,
            )
        },
        dismissButton = {
            PillButton(
                text = "skip",
                onClick = onDismiss,
                color = LocalHuedTextMuted.current,
            )
        },
    )
}

@Composable
private fun FolderRow(folder: FolderUiState, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!folder.isIncluded) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (folder.isIncluded) "\u2713" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.width(16.dp),
            )
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (folder.isIncluded) MaterialTheme.colorScheme.onBackground
                    else LocalHuedTextMuted.current.copy(alpha = 0.35f),
            )
        }
        Text(
            text = "${folder.photoCount}",
            style = MaterialTheme.typography.bodySmall,
            color = LocalHuedTextMuted.current.copy(alpha = if (folder.isIncluded) 0.5f else 0.2f),
        )
    }
}
