/*
 * Copyright lt 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sceneren.compose.banner

import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * creator: lt  2022/6/25  lt.dygzs@qq.com
 * effect : 可以自动循环轮播的ComposePager
 *          [ComposePager] that can auto scroll
 * warning:
 * @param pageCount 一共有多少页
 *                  Sum page count
 * @param modifier 修饰
 * @param bannerState Banner的状态
 *                    Banner's state
 * @param orientation 滑动的方向
 *                    Scroll orientation
 * @param userEnable 用户是否可以滑动,等于false时用户滑动无反应,但代码可以执行翻页
 *                   Whether the user can scroll
 * @param autoScroll 是否自动滚动
 *                   Whether to scroll automatically
 * @param autoScrollTime 自动滚动间隔时间
 *                       Auto scroll interval
 * @param bannerKey 使用key来提高性能,减少重组,效果等同于[LazyColumn#items#key]
 *                  Using key to improve performance, reduce recombination, and achieve the same effect as [LazyColumn#items#key]
 * @param content compose内容区域
 *                Content of compose
 */
@Composable
fun Banner(
    pageCount: Int,
    modifier: Modifier = Modifier,
    bannerState: BannerState = rememberBannerState(),
    orientation: Orientation = Orientation.Horizontal,
    userEnable: Boolean = true,
    autoScroll: Boolean = true,
    autoScrollTime: Long = 3000,
    pageSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(),
    @FloatRange(from = 0.0, to = 1.0) animScale: Float = 1f,
    bannerKey: (index: Int) -> Any = { it },
    content: @Composable BannerScope.() -> Unit,
) {
    if (pageCount <= 0) return
    //是否正在滚动倒计时中
    var scrolling by remember(key1 = autoScroll, key2 = pageCount) {
        mutableStateOf(autoScroll && pageCount > 1)
    }

    val scope = rememberCoroutineScope()

    //计算总共多少页
    val maxPageCount = remember(pageCount) {
        bannerState.pageCount = pageCount
        scope.launch {
            bannerState.composePagerState.scrollToPage(pageCount * bannerState.startMultiple)
        }
        minOf(pageCount * bannerState.sumMultiple, Int.MAX_VALUE)
    }

    //自动滚动
    if (scrolling) {
        LaunchedEffect(key1 = autoScrollTime, block = {
            while (true) {
                delay(autoScrollTime)
                val index = bannerState.composePagerState.settledPage
                if (index + 1 >= maxPageCount) bannerState.composePagerState.scrollToPage(pageCount * bannerState.startMultiple)
                else bannerState.composePagerState.animateScrollToPage(index + 1)
            }
        })
    }

    val draggedState by bannerState.composePagerState.interactionSource.collectIsDraggedAsState()

    scrolling = !draggedState

    CompositionLocalProvider(LocalIndexToKey provides { it % pageCount }) {
        //使用ComposePager放置元素
        if (orientation == Orientation.Vertical) {
            VerticalPager(
                modifier = modifier,
                state = bannerState.composePagerState,
                userScrollEnabled = userEnable,
                beyondViewportPageCount = maxOf(2, (pageCount - 1) / 2),
                key = bannerKey,
                pageSpacing = pageSpacing,
                contentPadding = contentPadding,
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (pageCount > 1 && animScale != 1f) {
                                val pageOffset =
                                    ((bannerState.currentPage - page) + bannerState.currentPageOffsetFraction).absoluteValue

                                scaleX = lerp(
                                    start = animScale,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                        }
                ) {
                    content(BannerScope(page % pageCount))
                }
            }
        } else {
            HorizontalPager(
                modifier = modifier,
                state = bannerState.composePagerState,
                userScrollEnabled = userEnable,
                beyondViewportPageCount = maxOf(2, (pageCount - 1) / 2),
                key = bannerKey,
                pageSpacing = pageSpacing,
                contentPadding = contentPadding,
            ) { page ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (pageCount > 1 && animScale != 1f) {
                            val pageOffset =
                                ((bannerState.composePagerState.currentPage - page) + bannerState.currentPageOffsetFraction).absoluteValue

                            scaleY = lerp(
                                start = animScale,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                    }) {
                    content(BannerScope(page % pageCount))
                }
            }
        }
    }
}

//通过当前index确定pager的index,用来保存和复用content
internal val LocalIndexToKey = compositionLocalOf<(index: Int) -> Int> { { it } }