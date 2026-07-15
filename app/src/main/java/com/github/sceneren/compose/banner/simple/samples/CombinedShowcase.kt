package com.github.sceneren.compose.banner.simple.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.BannerPageTransformers
import com.github.sceneren.compose.banner.BannerScrollDirection
import com.github.sceneren.compose.banner.indicator.BannerIndicator
import com.github.sceneren.compose.banner.indicator.CustomIndicator
import com.github.sceneren.compose.banner.indicator.IndicatorSlideMode
import com.github.sceneren.compose.banner.indicator.IndicatorStyle
import com.github.sceneren.compose.banner.indicator.NumberIndicator
import com.github.sceneren.compose.banner.rememberBannerState
import kotlinx.coroutines.launch

@Composable
internal fun CombinedShowcase(modifier: Modifier = Modifier) {
    val images = sampleImages()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { StandardOverlaySection(images) }
        item { ClickToJumpSection(images) }
        item { MultiPageCustomSection(images) }
        item { AutoPlayTransitionSection(images) }
    }
}

@Composable
private fun StandardOverlaySection(images: List<Int>) {
    val state = rememberBannerState()
    val scope = rememberCoroutineScope()
    var autoPlay by remember { mutableStateOf(true) }

    SampleSection(title = "标准叠层") {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                autoPlay = autoPlay,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = "Combined page ${index + 1}",
                )
            }
            BannerIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                currentPageOffsetFraction = state.currentPageOffsetFraction,
                style = IndicatorStyle.RoundRect,
                slideMode = IndicatorSlideMode.Worm,
                indicatorColor = Color.White.copy(alpha = 0.5f),
                selectedIndicatorColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                onPageSelected = { page ->
                    scope.launch { state.animateScrollToPage(page) }
                },
            )
            NumberIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            )
        }
        BannerControlRow(
            autoPlay = autoPlay,
            onToggleAutoPlay = { autoPlay = !autoPlay },
            onNext = {
                scope.launch { state.animateScrollBy(BannerScrollDirection.Next) }
            },
        )
    }
}

@Composable
private fun ClickToJumpSection(images: List<Int>) {
    val state = rememberBannerState()
    val scope = rememberCoroutineScope()

    SampleSection(title = "点击指示器跳页") {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                autoPlay = false,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = null,
                )
            }
            BannerIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                currentPageOffsetFraction = state.currentPageOffsetFraction,
                style = IndicatorStyle.Circle,
                slideMode = IndicatorSlideMode.Scale,
                indicatorColor = Color.White.copy(alpha = 0.45f),
                selectedIndicatorColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                onPageSelected = { page ->
                    scope.launch { state.animateScrollToPage(page) }
                },
            )
        }
        Text(
            "点击底部圆点可动画跳到对应页",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MultiPageCustomSection(images: List<Int>) {
    val state = rememberBannerState(initialPage = 1)
    val scope = rememberCoroutineScope()

    SampleSection(title = "Multi-page + CustomIndicator") {
        Banner(
            pageCount = images.size,
            state = state,
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 12.dp,
            pageTransformer = BannerPageTransformers.overlap(
                minimumScale = 0.82f,
                overlapFraction = 0.08f,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            SampleBannerImage(
                imageRes = images[index],
                contentDescription = null,
                modifier = Modifier.clip(RoundedCornerShape(18.dp)),
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CustomIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                currentPageOffsetFraction = state.currentPageOffsetFraction,
                spacing = 10.dp,
                onPageSelected = { page ->
                    scope.launch { state.animateScrollToPage(page) }
                },
            ) { item ->
                val color = lerp(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.primary,
                    item.selectionFraction,
                )
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = 0.9f + 0.2f * item.selectionFraction
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(999.dp))
                        .background(color)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "P${item.page + 1}",
                        color = if (item.selectionFraction > 0.5f) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoPlayTransitionSection(images: List<Int>) {
    val state = rememberBannerState()
    SampleSection(title = "自动播放连续过渡") {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                autoPlay = true,
                autoPlayIntervalMillis = 2_500L,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = null,
                )
            }
            BannerIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                currentPageOffsetFraction = state.currentPageOffsetFraction,
                style = IndicatorStyle.Dash,
                slideMode = IndicatorSlideMode.Smooth,
                indicatorColor = Color.White.copy(alpha = 0.45f),
                selectedIndicatorColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            )
            NumberIndicator(
                pageCount = images.size,
                currentPage = state.currentPage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            )
        }
        Text(
            "自动播放时 Indicator 应平滑跟随跨页",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
