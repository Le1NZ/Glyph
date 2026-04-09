package ru.glyph.navigation.ui.composable

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val DURATION_MILLIS = 300
private const val FADE_DURATION_MILLIS = 200

internal fun pushAnimationSpec(): ContentTransform = ContentTransform(
    targetContentEnter = slideInHorizontally(
        animationSpec = tween(durationMillis = DURATION_MILLIS),
        initialOffsetX = { it },
    ),
    initialContentExit = fadeOut(animationSpec = tween(durationMillis = FADE_DURATION_MILLIS)),
    targetContentZIndex = 1f,
)

internal fun popAnimationSpec(): ContentTransform = ContentTransform(
    targetContentEnter = fadeIn(
        animationSpec = tween(
            durationMillis = FADE_DURATION_MILLIS,
            delayMillis = DURATION_MILLIS - FADE_DURATION_MILLIS,
        ),
    ),
    initialContentExit = slideOutHorizontally(
        animationSpec = tween(durationMillis = DURATION_MILLIS),
        targetOffsetX = { it },
    ),
    targetContentZIndex = -1f,
)
