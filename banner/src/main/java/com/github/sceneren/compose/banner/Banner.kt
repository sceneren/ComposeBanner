package com.github.sceneren.compose.banner

import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.annotation.Visibility

@Composable
fun <T> Banner(
    modifier: Modifier = Modifier,
    items: List<T>,
    autoPlay: Boolean = true,
    looper: Boolean = true,
    autoPlayInterval: Int = 3000,
    indicatorCheckColor: Color = Color.White,
    indicatorNormalColor: Color = Color.Gray,
    @IndicatorGravity indicatorGravity: Int = IndicatorGravity.CENTER,
    @IndicatorSlideMode indicatorSlideMode: Int = IndicatorSlideMode.SCALE,
    @IndicatorStyle indicatorStyle: Int = IndicatorStyle.CIRCLE,
    indicatorNormalWidth: Dp = 4.dp,
    indicatorCheckWidth: Dp = 4.dp,
    indicatorHeight: Dp = 4.dp,
    @Visibility visibility: Int = View.VISIBLE,
    @BannerPageStyle pageStyle: Int = BannerPageStyle.NORMAL,
    pageScale: Float = 0.85f,
    pageRevealWidth: Dp = 0.dp,
    pageMargin: Dp = 0.dp,
    itemBuilder: @Composable (item: T, index: Int) -> Unit,
    itemTypeBuilder: BannerAdapter.ItemTypeBuilder? = null,
) {

    val lifecycle = LocalLifecycleOwner.current

    val context = LocalContext.current

    val density = LocalDensity.current

    val indicatorNormalWidthPx = with(density) {
        indicatorNormalWidth.toPx().toInt()
    }

    val indicatorCheckWidthPx = with(density) {
        indicatorCheckWidth.toPx().toInt()
    }

    val indicatorHeightPx = with(density) {
        indicatorHeight.toPx().toInt()
    }

    val pageRevealWidthPx = with(density) {
        pageRevealWidth.toPx().toInt()
    }

    val pageMarginPx = with(density) {
        pageMargin.toPx().toInt()
    }

    val adapter = remember {
        BannerAdapter<T>().apply {
            this.itemList = items
            this.itemBuilder = itemBuilder
            itemTypeBuilder?.let {
                this.itemTypeBuilder = itemTypeBuilder
            }
        }
    }

    val bannerViewPager = remember {
        BannerViewPager<T>(context).apply {
            this.clipToOutline = true
            this.registerLifecycleObserver(lifecycle.lifecycle)
            this.setAutoPlay(autoPlay)
            this.setCanLoop(looper)
            this.setInterval(autoPlayInterval)
            this.setIndicatorVisibility(visibility)
            this.setIndicatorGravity(indicatorGravity)
            this.setIndicatorStyle(indicatorStyle)
            this.setIndicatorSliderColor(
                indicatorNormalColor.toArgb(),
                indicatorCheckColor.toArgb()
            )
            this.setIndicatorSlideMode(indicatorSlideMode)
            this.setIndicatorHeight(indicatorHeightPx)
            this.setIndicatorSliderWidth(indicatorNormalWidthPx, indicatorCheckWidthPx)

            this.setPageStyle(pageStyle, pageScale)
            if (pageRevealWidthPx > 0) {
                this.setRevealWidth(pageRevealWidthPx)
            }

            this.setPageMargin(pageMarginPx)

            this.adapter = adapter
        }
    }

    AndroidView(
        factory = {
            bannerViewPager.apply {
                create()
            }
        },
        modifier = modifier,
        update = {
            Log.e("banner", "${looper}")
            it.refreshData(items)
            if (looper) {
                it.startLoop()
            } else {
                it.stopLoop()
            }
        }
    )

}

