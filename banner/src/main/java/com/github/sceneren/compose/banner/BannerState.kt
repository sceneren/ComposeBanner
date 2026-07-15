package com.github.sceneren.compose.banner

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * State holder for [Banner].
 *
 * Infinite scrolling uses a bounded multiplier (`pageCount * [LOOP_COUNT]`) so that every
 * position always has real previous/next neighbors. After settling far from the middle block
 * the pager is recentered without animation. This avoids the blank peek pages that appear
 * with a pageCount+2 sentinel architecture at the first/last page.
 *
 * By default a single-page banner does not loop or allow user scrolling. When single-page
 * looping is explicitly allowed, the same multiplier strategy is used so the page can wrap.
 */
@Stable
class BannerState internal constructor(
    private val initialPage: Int,
) {
    private var configuredPageCount by mutableIntStateOf(0)
    private var loopEnabled by mutableStateOf(false)
    private var singlePageLoopAllowed by mutableStateOf(false)
    private var pendingLogicalPage: Int? = initialPage.coerceAtLeast(0)

    /** The Compose pager state used by this banner. */
    val pagerState: PagerState = PagerState { virtualPageCount }

    /** Number of logical content pages. */
    val pageCount: Int
        get() = configuredPageCount

    /** Currently visible logical page in `0 until pageCount`. */
    val currentPage: Int
        get() = if (configuredPageCount == 0) {
            initialPage.coerceAtLeast(0)
        } else {
            logicalPageFor(pagerState.currentPage)
        }

    /** Logical page on which scrolling has settled. */
    val settledPage: Int
        get() = if (configuredPageCount == 0) {
            initialPage.coerceAtLeast(0)
        } else {
            logicalPageFor(pagerState.settledPage)
        }

    /** Offset from [currentPage], normally in `-0.5f..0.5f`. */
    val currentPageOffsetFraction: Float
        get() = pagerState.currentPageOffsetFraction

    /** True while a drag or programmatic scroll is running. */
    val isScrollInProgress: Boolean
        get() = pagerState.isScrollInProgress

    internal val virtualPageCount: Int
        get() = when {
            configuredPageCount <= 0 -> 0
            canLoop -> configuredPageCount * LOOP_COUNT
            else -> configuredPageCount
        }

    /**
     * Whether the pager is in looping mode.
     * Single page only loops when [singlePageLoopAllowed] is true.
     */
    internal val canLoop: Boolean
        get() = loopEnabled && (
            configuredPageCount > 1 ||
                (configuredPageCount == 1 && singlePageLoopAllowed)
            )

    private val middleBlockStart: Int
        get() = if (canLoop) configuredPageCount * (LOOP_COUNT / 2) else 0

    internal fun updateConfiguration(
        pageCount: Int,
        infiniteLoop: Boolean,
        allowSinglePageLoop: Boolean = false,
    ) {
        if (
            configuredPageCount == pageCount &&
            loopEnabled == infiniteLoop &&
            singlePageLoopAllowed == allowSinglePageLoop
        ) {
            return
        }

        val previousPage = if (configuredPageCount == 0) initialPage else currentPage
        configuredPageCount = pageCount
        loopEnabled = infiniteLoop
        singlePageLoopAllowed = allowSinglePageLoop
        pendingLogicalPage = when {
            pageCount == 0 -> null
            else -> previousPage.coerceIn(0, pageCount - 1)
        }
    }

    internal suspend fun applyPendingPosition() {
        val logicalPage = pendingLogicalPage ?: return
        pendingLogicalPage = null
        val target = physicalPageFor(logicalPage)
        if (pagerState.currentPage != target) pagerState.scrollToPage(target)
    }

    /**
     * 当滚动停止后，若当前位置偏离中间块较远，则无动画归位到中间同逻辑页。
     * 保证两侧始终有足够真实邻居，避免在首尾出现“无相邻页”空白。
     */
    internal suspend fun recenterIfNeeded() {
        if (!canLoop || pagerState.isScrollInProgress || configuredPageCount <= 0) return
        val settled = pagerState.settledPage
        val keepWindow = configuredPageCount.coerceAtLeast(1)
        val minKeep = keepWindow
        val maxKeep = virtualPageCount - keepWindow - 1
        if (settled in minKeep..maxKeep) return
        val target = physicalPageFor(logicalPageFor(settled))
        if (settled != target) {
            pagerState.scrollToPage(target)
        }
    }

    /** Immediately moves to a logical page. */
    suspend fun scrollToPage(page: Int) {
        requirePage(page)
        pagerState.scrollToPage(physicalPageFor(page))
    }

    /** Animates to a logical page and takes the short path when looping. */
    suspend fun animateScrollToPage(
        page: Int,
        animationSpec: AnimationSpec<Float> = spring(),
    ) {
        requirePage(page)
        val target = animateTargetPhysicalPage(
            currentPhysical = pagerState.currentPage,
            targetLogicalPage = page,
        )
        pagerState.animateScrollToPage(target, animationSpec = animationSpec)
        recenterIfNeeded()
    }

    /** Animates by one page and wraps at either boundary when looping is enabled. */
    suspend fun animateScrollBy(
        direction: BannerScrollDirection,
        animationSpec: AnimationSpec<Float> = spring(),
    ) {
        if (configuredPageCount <= 0) return
        if (configuredPageCount == 1 && !canLoop) return
        val delta = when (direction) {
            BannerScrollDirection.Next -> 1
            BannerScrollDirection.Previous -> -1
        }
        if (canLoop) {
            val target = (pagerState.currentPage + delta).coerceIn(0, virtualPageCount - 1)
            pagerState.animateScrollToPage(target, animationSpec = animationSpec)
            recenterIfNeeded()
        } else {
            val target = (currentPage + delta).coerceIn(0, configuredPageCount - 1)
            animateScrollToPage(target, animationSpec)
        }
    }

    internal fun logicalPageFor(physicalPage: Int): Int {
        if (configuredPageCount <= 0) return 0
        if (!canLoop) {
            return physicalPage.coerceIn(0, configuredPageCount - 1)
        }
        val mod = physicalPage % configuredPageCount
        return if (mod < 0) mod + configuredPageCount else mod
    }

    internal fun physicalPageFor(logicalPage: Int): Int {
        if (!canLoop) return logicalPage
        return middleBlockStart + logicalPage
    }

    /**
     * 计算从当前物理页动画到目标逻辑页时的物理目标页，优先走短路径。
     */
    internal fun animateTargetPhysicalPage(
        currentPhysical: Int,
        targetLogicalPage: Int,
    ): Int {
        if (!canLoop) return targetLogicalPage
        val currentLogical = logicalPageFor(currentPhysical)
        if (currentLogical == targetLogicalPage) {
            return physicalPageFor(targetLogicalPage)
        }
        val count = configuredPageCount
        val forward = (targetLogicalPage - currentLogical + count) % count
        val backward = (currentLogical - targetLogicalPage + count) % count
        return if (forward <= backward) {
            currentPhysical + forward
        } else {
            currentPhysical - backward
        }.coerceIn(0, virtualPageCount - 1)
    }

    private fun requirePage(page: Int) {
        require(configuredPageCount > 0) { "Banner has no pages." }
        require(page in 0 until configuredPageCount) {
            "page ($page) must be in 0 until $configuredPageCount"
        }
    }

    companion object {
        /**
         * 虚拟页倍增系数。足够大以支持长时间滑动，又远小于 Int.MAX_VALUE，
         * 并在偏离中间块后归位，避免接近两端。
         */
        const val LOOP_COUNT: Int = 500

        internal val Saver = Saver<BannerState, Int>(
            save = { it.currentPage },
            restore = { BannerState(initialPage = it) },
        )
    }
}

/**
 * 计算传给 Pager 的 beyondViewportPageCount。
 *
 * 倍增循环下相邻页始终真实存在；预加载数量交给调用方。
 */
@Suppress("UNUSED_PARAMETER")
internal fun resolveBeyondViewportPageCount(
    pageCount: Int,
    infiniteLoop: Boolean,
    requested: Int,
): Int {
    return requested.coerceAtLeast(0)
}

/**
 * 解析最终是否允许用户手势滑动。
 *
 * [disableScrollWhenSinglePage] 默认 true：只有一页时禁止手势滑动。
 * 设为 false 时，单页也可滑动（配合循环时会在同内容间环绕）。
 */
internal fun resolveUserScrollEnabled(
    pageCount: Int,
    userScrollEnabled: Boolean,
    disableScrollWhenSinglePage: Boolean,
): Boolean {
    if (!userScrollEnabled) return false
    if (disableScrollWhenSinglePage && pageCount <= 1) return false
    return true
}

/** Remembers a saveable [BannerState]. */
@Composable
fun rememberBannerState(initialPage: Int = 0): BannerState {
    require(initialPage >= 0) { "initialPage must be at least 0" }
    return rememberSaveable(saver = BannerState.Saver) {
        BannerState(initialPage = initialPage)
    }
}
