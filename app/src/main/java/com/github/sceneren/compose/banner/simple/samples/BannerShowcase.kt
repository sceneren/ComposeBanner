package com.github.sceneren.compose.banner.simple.samples

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.BannerPageTransformer
import com.github.sceneren.compose.banner.BannerPageTransformers
import com.github.sceneren.compose.banner.BannerScrollDirection
import com.github.sceneren.compose.banner.indicator.NumberIndicator
import com.github.sceneren.compose.banner.rememberBannerState
import kotlinx.coroutines.launch

@Composable
internal fun BannerShowcase(modifier: Modifier = Modifier) {
    val images = sampleImages()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            BasicLoopSection(images)
        }
        item {
            TransformerSection(
                title = "None transformer",
                images = images,
                transformer = BannerPageTransformers.None,
            )
        }
        item {
            TransformerSection(
                title = "Scale transformer",
                images = images,
                transformer = BannerPageTransformers.scale(
                    minimumScale = 0.86f,
                    minimumAlpha = 0.75f,
                ),
            )
        }
        item {
            OverlapSection(images)
        }
        item {
            TransformerSection(
                title = "Depth transformer",
                images = images,
                transformer = BannerPageTransformers.depth(minimumScale = 0.78f),
            )
        }
        item {
            VerticalSection(images)
        }
        item {
            NonLoopSection(images)
        }
        item {
            SinglePageSection(
                title = "单页默认不可滑",
                images = images,
                disableScrollWhenSinglePage = true,
                description = "disableScrollWhenSinglePage=true（默认），只有 1 项时不能手势滑动",
            )
        }
        item {
            SinglePageSection(
                title = "单页允许循环滑动",
                images = images,
                disableScrollWhenSinglePage = false,
                description = "disableScrollWhenSinglePage=false，单页也可循环滑动/自动播放",
            )
        }
    }
}

@Composable
private fun BasicLoopSection(images: List<Int>) {
    val state = rememberBannerState()
    val scope = rememberCoroutineScope()
    var autoPlay by remember { mutableStateOf(true) }

    SampleSection(title = "基础循环 + 自动播放") {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                autoPlay = autoPlay,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = "Banner page ${index + 1}",
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
            "逻辑页 ${state.currentPage + 1} / ${images.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BannerControlRow(
            autoPlay = autoPlay,
            onToggleAutoPlay = { autoPlay = !autoPlay },
            onNext = {
                scope.launch {
                    state.animateScrollBy(BannerScrollDirection.Next)
                }
            },
        )
    }
}

@Composable
private fun TransformerSection(
    title: String,
    images: List<Int>,
    transformer: BannerPageTransformer,
) {
    val state = rememberBannerState()
    SampleSection(title = title) {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                pageTransformer = transformer,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = null,
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
    }
}

@Composable
private fun OverlapSection(images: List<Int>) {
    val state = rememberBannerState(initialPage = 1)
    SampleSection(title = "Overlap 多页露出") {
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
        Text(
            "逻辑页 ${state.currentPage + 1} / ${images.size} · 首尾应有真实邻居",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VerticalSection(images: List<Int>) {
    val state = rememberBannerState()
    SampleSection(title = "纵向 Banner") {
        SampleBannerFrame(aspectRatio = null, modifier = Modifier.height(220.dp)) {
            Banner(
                pageCount = images.size,
                state = state,
                orientation = Orientation.Vertical,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = "Vertical page ${index + 1}",
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
    }
}

@Composable
private fun NonLoopSection(images: List<Int>) {
    val state = rememberBannerState()
    SampleSection(title = "非循环 Banner") {
        SampleBannerFrame {
            Banner(
                pageCount = images.size,
                state = state,
                infiniteLoop = false,
                autoPlay = false,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = images[index],
                    contentDescription = "Non-loop page ${index + 1}",
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
            "infiniteLoop=false，滑到首尾后不可环绕",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SinglePageSection(
    title: String,
    images: List<Int>,
    disableScrollWhenSinglePage: Boolean,
    description: String,
) {
    val state = rememberBannerState()
    val singleImages = remember(images) { images.take(1) }
    SampleSection(title = title) {
        SampleBannerFrame {
            Banner(
                pageCount = singleImages.size,
                state = state,
                infiniteLoop = true,
                autoPlay = !disableScrollWhenSinglePage,
                disableScrollWhenSinglePage = disableScrollWhenSinglePage,
                modifier = Modifier.fillMaxSize(),
            ) {
                SampleBannerImage(
                    imageRes = singleImages[index],
                    contentDescription = "Single page",
                )
            }
            NumberIndicator(
                pageCount = singleImages.size,
                currentPage = state.currentPage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            )
        }
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
