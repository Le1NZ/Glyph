package ru.glyph.utils.clock

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

actual fun currentTimeDuration(): Duration {
    return System.currentTimeMillis()
        .milliseconds
        .inWholeSeconds
        .seconds
}