package app.hued.data.model

sealed interface ProcessingState {
    data class InitialProcessing(
        val totalFound: Int,
        val totalProcessed: Int,
    ) : ProcessingState

    data object Ready : ProcessingState

    data object Updating : ProcessingState

    data class UpdatingHistory(
        val totalFound: Int,
        val totalProcessed: Int,
    ) : ProcessingState
}
