package com.github.sceneren.compose.banner.simple.samples

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.sceneren.compose.banner.simple.R

@Composable
internal fun sampleImages(): List<Int> = remember {
    listOf(R.drawable.a1, R.drawable.a2, R.drawable.a3)
}

@Composable
internal fun SampleSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        },
    )
}

@Composable
internal fun SampleBannerFrame(
    modifier: Modifier = Modifier,
    aspectRatio: Float? = 16f / 9f,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val sizedModifier = if (aspectRatio != null) {
        modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    } else {
        modifier.fillMaxWidth()
    }
    Box(
        modifier = sizedModifier.clip(shape),
        content = content,
    )
}

@Composable
internal fun SampleBannerImage(
    imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
internal fun BannerControlRow(
    autoPlay: Boolean,
    onToggleAutoPlay: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onToggleAutoPlay) {
            Text(if (autoPlay) "Pause" else "Play")
        }
        Button(onClick = onNext) {
            Text("Next")
        }
    }
}
