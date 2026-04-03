package app.hued.data.model

sealed interface DelightMoment {
    val text: String
    val periodKey: String

    data class Monochrome(
        override val text: String,
        override val periodKey: String,
        val hueName: String,
    ) : DelightMoment

    data class Harmony(
        override val text: String,
        override val periodKey: String,
    ) : DelightMoment

    data class Birthday(
        override val text: String,
        override val periodKey: String,
    ) : DelightMoment

    data class NewYear(
        override val text: String,
        override val periodKey: String,
        val year: Int,
    ) : DelightMoment
}
