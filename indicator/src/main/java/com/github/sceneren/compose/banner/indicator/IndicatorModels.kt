package com.github.sceneren.compose.banner.indicator

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

enum class IndicatorStyle {
    Circle,
    Dash,
    RoundRect,
}

enum class IndicatorSlideMode {
    Normal,
    Smooth,
    Worm,
    Color,
    Scale,
}

@Immutable
data class IndicatorProgress(
    val currentPage: Int,
    val targetPage: Int,
    val fraction: Float,
) {
    fun selectionFraction(page: Int): Float = when {
        currentPage == targetPage && page == currentPage -> 1f
        page == currentPage -> 1f - fraction
        page == targetPage -> fraction
        else -> 0f
    }
}

object IndicatorDefaults {
    val UnselectedColor = androidx.compose.ui.graphics.Color(0x66FFFFFF)
    val SelectedColor = androidx.compose.ui.graphics.Color.White
    val Spacing = 6.dp

    fun indicatorSize(style: IndicatorStyle): DpSize = when (style) {
        IndicatorStyle.Circle -> DpSize(8.dp, 8.dp)
        IndicatorStyle.Dash -> DpSize(16.dp, 4.dp)
        IndicatorStyle.RoundRect -> DpSize(12.dp, 6.dp)
    }

    fun selectedIndicatorSize(style: IndicatorStyle): DpSize = when (style) {
        IndicatorStyle.Circle -> DpSize(8.dp, 8.dp)
        IndicatorStyle.Dash -> DpSize(24.dp, 4.dp)
        IndicatorStyle.RoundRect -> DpSize(24.dp, 6.dp)
    }
}

internal fun resolveIndicatorProgress(
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
): IndicatorProgress {
    if (pageCount <= 0) return IndicatorProgress(0, 0, 0f)
    val current = Math.floorMod(currentPage, pageCount)
    val offset = currentPageOffsetFraction.coerceIn(-1f, 1f)
    if (offset == 0f || pageCount == 1) return IndicatorProgress(current, current, 0f)
    val direction = if (offset > 0f) 1 else -1
    return IndicatorProgress(
        currentPage = current,
        targetPage = Math.floorMod(current + direction, pageCount),
        fraction = offset.absoluteValue,
    )
}

internal fun logicalCanvasSize(
    pageCount: Int,
    cellSize: Size,
    spacing: Float,
    horizontal: Boolean,
): Size {
    val count = pageCount.coerceAtLeast(0)
    if (count == 0) return Size.Zero
    val mainAxis = if (horizontal) cellSize.width else cellSize.height
    val crossAxis = if (horizontal) cellSize.height else cellSize.width
    val total = mainAxis * count + spacing * (count - 1)
    return if (horizontal) Size(total, crossAxis) else Size(crossAxis, total)
}
