package ru.glyph.utils.clock

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

actual fun currentTimeDuration(): Duration {
    return NSDate().timeIntervalSince1970.toLong().seconds
}