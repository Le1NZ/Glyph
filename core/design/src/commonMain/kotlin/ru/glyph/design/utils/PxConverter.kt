package ru.glyph.design.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
fun Dp.roundToPx(): Int {
    return with(LocalDensity.current) { this@roundToPx.roundToPx() }
}

@Composable
fun Float.pxToDp(): Dp {
    return with(LocalDensity.current) { this@pxToDp.toDp() }
}

@Composable
fun Int.pxToDp(): Dp {
    return with(LocalDensity.current) { this@pxToDp.toDp() }
}
