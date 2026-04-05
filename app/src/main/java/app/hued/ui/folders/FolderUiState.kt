package app.hued.ui.folders

data class FolderUiState(
    val path: String,
    val displayName: String,
    val photoCount: Int,
    val isIncluded: Boolean,
)
