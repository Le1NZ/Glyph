package ru.glyph.model

enum class FolderColor {
    BLUE,
    PURPLE,
    GREEN,
    ORANGE,
    RED,
    TEAL;

    companion object {

        fun fromKey(key: String): FolderColor =
            entries.firstOrNull { it.name == key } ?: BLUE

        /**
         * Cyclic pick by 0-based ordinal (creation order).
         */
        fun byIndex(index: Int): FolderColor =
            entries[((index % entries.size) + entries.size) % entries.size]
    }
}
