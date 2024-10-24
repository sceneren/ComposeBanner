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

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow

/**
 * Banner的状态
 * State of the [Banner]
 */
@Stable
class BannerState {
    //起始倍数,用于支持用户开始就向左划
    internal val startMultiple = 100

    //总倍数,正常情况下Banner可以循环滑动的次数
    internal val sumMultiple = 50000

    //内部的ComposePager的state
    internal val composePagerState: PagerState =
        PagerState { minOf(pageCount * sumMultiple, Int.MAX_VALUE) }

    internal var pageCount: Int = 1

    /**
     * 获取Banner当前所在的索引
     * Get current index in the [Banner]
     */
    fun getCurrSelectIndex(): Int = composePagerState.settledPage % pageCount

    /**
     * 创建Banner当前索引的flow对象
     * Create the [Flow] of the current index of the [Banner]
     */
    fun createCurrSelectIndexFlow(): StableFlow<Int> = snapshotFlow {
        composePagerState.currentPage % pageCount
    }.toStableFlow()

    /**
     * 动画是否执行中
     * Whether animate is running
     */
    fun isAnimRunning(): Boolean = composePagerState.isScrollInProgress

    /**
     * 获取Offset偏移量的state对象
     * Get the [State] of the offset
     */
    fun getOffsetState(): State<Float> =
        mutableFloatStateOf(composePagerState.currentPageOffsetFraction)

    /**
     * 创建子项Offset偏移比例的flow对象
     * Create the [Flow] of the percent of the offset
     */
    fun createChildOffsetPercentFlow(): StableFlow<Float> = snapshotFlow {
        composePagerState.currentPageOffsetFraction
    }.toStableFlow()

    /**
     * 切换选中的页数,无动画
     * Set the current index, no animate
     */
    suspend fun scrollToPage(index: Int) {
        composePagerState.scrollToPage(pageCount * startMultiple + index)
    }

    /**
     * 切换选中的页数,有动画
     * Set the current index, with animate
     */
    suspend fun animateScrollToPage(index: Int) {
        composePagerState.animateScrollToPage(index)
    }
}

/**
 * 创建一个[remember]的[BannerState]
 * Create the [BannerState] of [remember]
 */
@Composable
fun rememberBannerState() = remember { BannerState() }