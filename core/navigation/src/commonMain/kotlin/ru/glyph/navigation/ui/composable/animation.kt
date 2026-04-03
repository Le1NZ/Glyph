package ru.glyph.navigation.ui.composable

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val CHANGE_SCREEN_DURATION_MILLIS = 350

internal fun <T> changeScreenAnimationSpec() = tween<T>(
    durationMillis = CHANGE_SCREEN_DURATION_MILLIS,
)

internal fun animationSpec(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInHorizontally(changeScreenAnimationSpec()),
        initialContentExit = slideOutHorizontally(changeScreenAnimationSpec()),
    )
}
