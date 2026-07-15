package com.github.sceneren.compose.banner.indicator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Immutable
data class IndicatorItemState(
    val page: Int,
    val selectionFraction: Float,
    val isCurrentPage: Boolean,
    val isTargetPage: Boolean,
)

/**
 * Slot-based indicator for fully custom Compose content.
 *
 * [itemContent] receives a continuous selection fraction for implementing custom color,
 * size, alpha, image, or text transitions.
 */
@Composable
fun CustomIndicator(
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    spacing: Dp = IndicatorDefaults.Spacing,
    onPageSelected: ((Int) -> Unit)? = null,
    itemContent: @Composable (IndicatorItemState) -> Unit,
) {
    if (pageCount <= 0) return
    val progress = resolveIndicatorProgress(pageCount, currentPage, currentPageOffsetFraction)
    val item: @Composable (Int) -> Unit = { page ->
        val clickModifier = if (onPageSelected == null) {
            Modifier
        } else {
            Modifier.clickable { onPageSelected(page) }
        }
        Box(clickModifier) {
            itemContent(
                IndicatorItemState(
                    page = page,
                    selectionFraction = progress.selectionFraction(page),
                    isCurrentPage = page == progress.currentPage,
                    isTargetPage = page == progress.targetPage,
                ),
            )
        }
    }

    when (orientation) {
        Orientation.Horizontal -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            repeat(pageCount) { item(it) }
        }
        Orientation.Vertical -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            repeat(pageCount) { item(it) }
        }
    }
}
