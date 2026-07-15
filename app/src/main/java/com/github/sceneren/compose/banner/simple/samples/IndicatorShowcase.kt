package com.github.sceneren.compose.banner.simple.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.indicator.BannerIndicator
import com.github.sceneren.compose.banner.indicator.CustomIndicator
import com.github.sceneren.compose.banner.indicator.IndicatorSlideMode
import com.github.sceneren.compose.banner.indicator.IndicatorStyle
import com.github.sceneren.compose.banner.indicator.NumberIndicator
import com.github.sceneren.compose.banner.rememberBannerState
import kotlinx.coroutines.launch

@Composable
internal fun IndicatorShowcase(modifier: Modifier = Modifier) {
    val images = sampleImages()
    val state = rememberBannerState()
    val scope = rememberCoroutineScope()
    val unselected = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    val selected = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            SampleSection(title = "驱动 Banner") {
                SampleBannerFrame {
                    Banner(
                        pageCount = images.size,
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SampleBannerImage(
                            imageRes = images[index],
                            contentDescription = "Driver page ${index + 1}",
                        )
                    }
                    NumberIndicator(
                        pageCount = images.size,
                        currentPage = state.currentPage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                    )
                }
                Text(
                    "下方矩阵全部绑定此 Banner 的 currentPage / offset",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SampleSection(title = "样式 × 滑动模式矩阵") {
                IndicatorStyle.entries.forEach { style ->
                    IndicatorSlideMode.entries.forEach { mode ->
                        IndicatorMatrixRow(
                            label = "${style.name} · ${mode.name}",
                            pageCount = images.size,
                            currentPage = state.currentPage,
                            currentPageOffsetFraction = state.currentPageOffsetFraction,
                            style = style,
                            slideMode = mode,
                            indicatorColor = unselected,
                            selectedIndicatorColor = selected,
                            onPageSelected = { page ->
                                scope.launch { state.animateScrollToPage(page) }
                            },
                        )
                    }
                }
            }
        }

        item {
            SampleSection(title = "NumberIndicator") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("current / total")
                    NumberIndicator(
                        pageCount = images.size,
                        currentPage = state.currentPage,
                        backgroundColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.75f),
                        textColor = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            }
        }

        item {
            SampleSection(title = "CustomIndicator") {
                CustomIndicator(
                    pageCount = images.size,
                    currentPage = state.currentPage,
                    currentPageOffsetFraction = state.currentPageOffsetFraction,
                    spacing = 10.dp,
                    onPageSelected = { page ->
                        scope.launch { state.animateScrollToPage(page) }
                    },
                ) { item ->
                    val scale = 0.85f + 0.25f * item.selectionFraction
                    val color = lerp(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.primary,
                        item.selectionFraction,
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${item.page + 1}",
                            color = if (item.selectionFraction > 0.5f) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 12.sp,
                            fontWeight = if (item.isCurrentPage) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }

        item {
            SampleSection(title = "纵向指示器") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BannerIndicator(
                        pageCount = images.size,
                        currentPage = state.currentPage,
                        currentPageOffsetFraction = state.currentPageOffsetFraction,
                        style = IndicatorStyle.Circle,
                        slideMode = IndicatorSlideMode.Smooth,
                        orientation = Orientation.Vertical,
                        indicatorColor = unselected,
                        selectedIndicatorColor = selected,
                        onPageSelected = { page ->
                            scope.launch { state.animateScrollToPage(page) }
                        },
                    )
                    BannerIndicator(
                        pageCount = images.size,
                        currentPage = state.currentPage,
                        currentPageOffsetFraction = state.currentPageOffsetFraction,
                        style = IndicatorStyle.RoundRect,
                        slideMode = IndicatorSlideMode.Worm,
                        orientation = Orientation.Vertical,
                        indicatorColor = unselected,
                        selectedIndicatorColor = selected,
                        onPageSelected = { page ->
                            scope.launch { state.animateScrollToPage(page) }
                        },
                    )
                    CustomIndicator(
                        pageCount = images.size,
                        currentPage = state.currentPage,
                        currentPageOffsetFraction = state.currentPageOffsetFraction,
                        orientation = Orientation.Vertical,
                        spacing = 8.dp,
                        onPageSelected = { page ->
                            scope.launch { state.animateScrollToPage(page) }
                        },
                    ) { item ->
                        val size = 8.dp + 6.dp * item.selectionFraction
                        Box(
                            modifier = Modifier
                                .size(size)
                                .clip(CircleShape)
                                .background(
                                    lerp(unselected, selected, item.selectionFraction),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicatorMatrixRow(
    label: String,
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    style: IndicatorStyle,
    slideMode: IndicatorSlideMode,
    indicatorColor: Color,
    selectedIndicatorColor: Color,
    onPageSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        BannerIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
            currentPageOffsetFraction = currentPageOffsetFraction,
            style = style,
            slideMode = slideMode,
            indicatorColor = indicatorColor,
            selectedIndicatorColor = selectedIndicatorColor,
            onPageSelected = onPageSelected,
        )
    }
}
