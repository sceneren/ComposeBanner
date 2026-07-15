package com.github.sceneren.compose.banner.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.BannerPageTransformers
import com.github.sceneren.compose.banner.indicator.BannerIndicator
import com.github.sceneren.compose.banner.indicator.IndicatorSlideMode
import com.github.sceneren.compose.banner.indicator.IndicatorStyle
import com.github.sceneren.compose.banner.indicator.NumberIndicator
import com.github.sceneren.compose.banner.rememberBannerState
import com.github.sceneren.compose.banner.simple.theme.ComposeBannerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeBannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BannerSamples(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun BannerSamples(modifier: Modifier = Modifier) {
    val images = remember { listOf(R.drawable.a1, R.drawable.a2, R.drawable.a3) }
    val state = rememberBannerState()
    val overlapState = rememberBannerState(initialPage = 1)
    val scope = rememberCoroutineScope()
    var autoPlay by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Bounded infinite banner", style = MaterialTheme.typography.titleLarge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(20.dp)),
                ) {
                    Banner(
                        pageCount = images.size,
                        state = state,
                        autoPlay = autoPlay,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(
                            painter = painterResource(images[index]),
                            contentDescription = "Banner page ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { autoPlay = !autoPlay }) {
                        Text(if (autoPlay) "Pause" else "Play")
                    }
                    Button(onClick = {
                        scope.launch { state.animateScrollToPage((state.currentPage + 1) % images.size) }
                    }) {
                        Text("Next")
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Multi-page overlap",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Banner(
                    pageCount = images.size,
                    state = overlapState,
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    pageSpacing = 12.dp,
                    pageTransformer = BannerPageTransformers.overlap(
                        minimumScale = 0.82f,
                        overlapFraction = 0.08f,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 8f),
                ) {
                    Image(
                        painter = painterResource(images[index]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Indicator slide modes", style = MaterialTheme.typography.titleLarge)
                IndicatorSlideMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(mode.name)
                        BannerIndicator(
                            pageCount = images.size,
                            currentPage = 0,
                            currentPageOffsetFraction = 0.4f,
                            style = IndicatorStyle.RoundRect,
                            slideMode = mode,
                            indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                            selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
