package com.github.sceneren.compose.banner.indicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.toSize
import kotlin.math.max
import kotlin.math.min

/**
 * Canvas-based indicator supporting the three styles and five slide modes from
 * BannerViewPager's indicator library.
 */
@Composable
fun BannerIndicator(
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    modifier: Modifier = Modifier,
    style: IndicatorStyle = IndicatorStyle.Circle,
    slideMode: IndicatorSlideMode = IndicatorSlideMode.Smooth,
    indicatorColor: Color = IndicatorDefaults.UnselectedColor,
    selectedIndicatorColor: Color = IndicatorDefaults.SelectedColor,
    indicatorSize: DpSize = IndicatorDefaults.indicatorSize(style),
    selectedIndicatorSize: DpSize = IndicatorDefaults.selectedIndicatorSize(style),
    spacing: Dp = IndicatorDefaults.Spacing,
    orientation: Orientation = Orientation.Horizontal,
    scaleFactor: Float = 1.35f,
    onPageSelected: ((Int) -> Unit)? = null,
) {
    if (pageCount <= 0) return
    require(scaleFactor >= 1f) { "scaleFactor must be at least 1f" }

    val density = LocalDensity.current
    val horizontal = orientation == Orientation.Horizontal
    val normalPx = with(density) { indicatorSize.toSize() }
    val selectedPx = with(density) { selectedIndicatorSize.toSize() }
    val spacingPx = with(density) { spacing.toPx() }
    val cellSize = Size(
        width = max(normalPx.width, selectedPx.width * if (slideMode == IndicatorSlideMode.Scale) scaleFactor else 1f),
        height = max(normalPx.height, selectedPx.height * if (slideMode == IndicatorSlideMode.Scale) scaleFactor else 1f),
    )
    val canvasPx = logicalCanvasSize(pageCount, cellSize, spacingPx, horizontal)
    val canvasSize = with(density) {
        DpSize(canvasPx.width.toDp(), canvasPx.height.toDp())
    }
    val progress = resolveIndicatorProgress(pageCount, currentPage, currentPageOffsetFraction)
    val step = if (horizontal) cellSize.width + spacingPx else cellSize.height + spacingPx

    val clickModifier = if (onPageSelected == null) {
        Modifier
    } else {
        Modifier.pointerInput(pageCount, step, horizontal, onPageSelected) {
            detectTapGestures { position ->
                val mainAxis = if (horizontal) position.x else position.y
                val index = (mainAxis / step).toInt().coerceIn(0, pageCount - 1)
                onPageSelected(index)
            }
        }
    }

    Canvas(
        modifier = modifier
            .then(clickModifier)
            .semantics {
                stateDescription = "${progress.currentPage + 1} / $pageCount"
            }
            .size(canvasSize),
    ) {
        fun centerFor(page: Int): Offset = if (horizontal) {
            Offset(cellSize.width / 2f + page * step, size.height / 2f)
        } else {
            Offset(size.width / 2f, cellSize.height / 2f + page * step)
        }

        when (slideMode) {
            IndicatorSlideMode.Color -> {
                repeat(pageCount) { page ->
                    val selectedFraction = progress.selectionFraction(page)
                    drawIndicator(
                        style = style,
                        color = lerp(indicatorColor, selectedIndicatorColor, selectedFraction),
                        center = centerFor(page),
                        indicatorSize = normalPx,
                    )
                }
            }

            IndicatorSlideMode.Scale -> {
                repeat(pageCount) { page ->
                    val selectedFraction = progress.selectionFraction(page)
                    val scale = 1f + (scaleFactor - 1f) * selectedFraction
                    drawIndicator(
                        style = style,
                        color = lerp(indicatorColor, selectedIndicatorColor, selectedFraction),
                        center = centerFor(page),
                        indicatorSize = Size(normalPx.width * scale, normalPx.height * scale),
                    )
                }
            }

            else -> {
                repeat(pageCount) { page ->
                    drawIndicator(style, indicatorColor, centerFor(page), normalPx)
                }
                when (slideMode) {
                    IndicatorSlideMode.Normal -> drawIndicator(
                        style,
                        selectedIndicatorColor,
                        centerFor(progress.currentPage),
                        selectedPx,
                    )

                    IndicatorSlideMode.Smooth -> drawIndicator(
                        style,
                        selectedIndicatorColor,
                        lerpOffset(
                            centerFor(progress.currentPage),
                            centerFor(progress.targetPage),
                            progress.fraction,
                        ),
                        selectedPx,
                    )

                    IndicatorSlideMode.Worm -> drawWorm(
                        style = style,
                        color = selectedIndicatorColor,
                        from = centerFor(progress.currentPage),
                        to = centerFor(progress.targetPage),
                        fraction = progress.fraction,
                        indicatorSize = selectedPx,
                        horizontal = horizontal,
                    )

                    else -> Unit
                }
            }
        }
    }
}

private fun DrawScope.drawWorm(
    style: IndicatorStyle,
    color: Color,
    from: Offset,
    to: Offset,
    fraction: Float,
    indicatorSize: Size,
    horizontal: Boolean,
) {
    if (from == to) {
        drawIndicator(style, color, from, indicatorSize)
        return
    }
    val phase = (fraction * 2f).coerceIn(0f, 2f)
    if (horizontal) {
        val fromStart = from.x - indicatorSize.width / 2f
        val fromEnd = from.x + indicatorSize.width / 2f
        val toStart = to.x - indicatorSize.width / 2f
        val toEnd = to.x + indicatorSize.width / 2f
        val left: Float
        val right: Float
        if (to.x >= from.x) {
            left = if (phase <= 1f) fromStart else lerpFloat(fromStart, toStart, phase - 1f)
            right = if (phase <= 1f) lerpFloat(fromEnd, toEnd, phase) else toEnd
        } else {
            left = if (phase <= 1f) lerpFloat(fromStart, toStart, phase) else toStart
            right = if (phase <= 1f) fromEnd else lerpFloat(fromEnd, toEnd, phase - 1f)
        }
        drawIndicator(style, color, Offset((left + right) / 2f, from.y), Size(right - left, indicatorSize.height))
    } else {
        val fromStart = from.y - indicatorSize.height / 2f
        val fromEnd = from.y + indicatorSize.height / 2f
        val toStart = to.y - indicatorSize.height / 2f
        val toEnd = to.y + indicatorSize.height / 2f
        val top: Float
        val bottom: Float
        if (to.y >= from.y) {
            top = if (phase <= 1f) fromStart else lerpFloat(fromStart, toStart, phase - 1f)
            bottom = if (phase <= 1f) lerpFloat(fromEnd, toEnd, phase) else toEnd
        } else {
            top = if (phase <= 1f) lerpFloat(fromStart, toStart, phase) else toStart
            bottom = if (phase <= 1f) fromEnd else lerpFloat(fromEnd, toEnd, phase - 1f)
        }
        drawIndicator(style, color, Offset(from.x, (top + bottom) / 2f), Size(indicatorSize.width, bottom - top))
    }
}

private fun DrawScope.drawIndicator(
    style: IndicatorStyle,
    color: Color,
    center: Offset,
    indicatorSize: Size,
) {
    val topLeft = Offset(
        center.x - indicatorSize.width / 2f,
        center.y - indicatorSize.height / 2f,
    )
    when (style) {
        IndicatorStyle.Circle -> {
            if (indicatorSize.width == indicatorSize.height) {
                drawCircle(color, indicatorSize.width / 2f, center)
            } else {
                drawRoundRect(
                    color = color,
                    topLeft = topLeft,
                    size = indicatorSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(min(indicatorSize.width, indicatorSize.height) / 2f),
                )
            }
        }
        IndicatorStyle.Dash -> drawRect(color, topLeft, indicatorSize)
        IndicatorStyle.RoundRect -> drawRoundRect(
            color = color,
            topLeft = topLeft,
            size = indicatorSize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(indicatorSize.height / 2f),
        )
    }
}

private fun lerpOffset(start: Offset, stop: Offset, fraction: Float): Offset = Offset(
    lerpFloat(start.x, stop.x, fraction),
    lerpFloat(start.y, stop.y, fraction),
)

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction
