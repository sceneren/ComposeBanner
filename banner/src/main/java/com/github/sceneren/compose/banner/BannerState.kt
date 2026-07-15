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
 * Infinite scrolling uses two boundary pages, so the underlying pager contains at most
 * `pageCount + 2` pages. No large virtual page count or duplicated data set is used.
 */
@Stable
class BannerState internal constructor(
    private val initialPage: Int,
) {
    private var configuredPageCount by mutableIntStateOf(0)
    private var loopEnabled by mutableStateOf(false)
    private var pendingLogicalPage: Int? = initialPage.coerceAtLeast(0)

    /** The bounded Compose pager state used by this banner. */
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
            configuredPageCount <= 1 -> configuredPageCount
            loopEnabled -> configuredPageCount + BOUNDARY_PAGE_COUNT
            else -> configuredPageCount
        }

    internal val canLoop: Boolean
        get() = loopEnabled && configuredPageCount > 1

    internal fun updateConfiguration(pageCount: Int, infiniteLoop: Boolean) {
        if (configuredPageCount == pageCount && loopEnabled == infiniteLoop) return

        val previousPage = if (configuredPageCount == 0) initialPage else currentPage
        configuredPageCount = pageCount
        loopEnabled = infiniteLoop
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

    internal suspend fun recenterIfNeeded() {
        if (!canLoop || pagerState.isScrollInProgress) return
        when (pagerState.settledPage) {
            0 -> pagerState.scrollToPage(configuredPageCount)
            configuredPageCount + 1 -> pagerState.scrollToPage(1)
        }
    }

    /** Immediately moves to a logical page. */
    suspend fun scrollToPage(page: Int) {
        requirePage(page)
        pagerState.scrollToPage(physicalPageFor(page))
    }

    /** Animates to a logical page and takes the short boundary path when looping. */
    suspend fun animateScrollToPage(
        page: Int,
        animationSpec: AnimationSpec<Float> = spring(),
    ) {
        requirePage(page)
        val target = when {
            canLoop && currentPage == configuredPageCount - 1 && page == 0 -> configuredPageCount + 1
            canLoop && currentPage == 0 && page == configuredPageCount - 1 -> 0
            else -> physicalPageFor(page)
        }
        pagerState.animateScrollToPage(target, animationSpec = animationSpec)
    }

    /** Animates by one page and wraps at either boundary when looping is enabled. */
    suspend fun animateScrollBy(
        direction: BannerScrollDirection,
        animationSpec: AnimationSpec<Float> = spring(),
    ) {
        if (configuredPageCount <= 1) return
        val target = when (direction) {
            BannerScrollDirection.Next -> currentPage + 1
            BannerScrollDirection.Previous -> currentPage - 1
        }
        when {
            target in 0 until configuredPageCount -> animateScrollToPage(target, animationSpec)
            canLoop && target == configuredPageCount -> pagerState.animateScrollToPage(
                configuredPageCount + 1,
                animationSpec = animationSpec,
            )
            canLoop && target == -1 -> pagerState.animateScrollToPage(
                0,
                animationSpec = animationSpec,
            )
        }
    }

    internal fun logicalPageFor(physicalPage: Int): Int {
        if (!canLoop) return physicalPage.coerceIn(0, (configuredPageCount - 1).coerceAtLeast(0))
        return when (physicalPage) {
            0 -> configuredPageCount - 1
            configuredPageCount + 1 -> 0
            else -> (physicalPage - 1).coerceIn(0, configuredPageCount - 1)
        }
    }

    private fun physicalPageFor(logicalPage: Int): Int =
        if (canLoop) logicalPage + 1 else logicalPage

    private fun requirePage(page: Int) {
        require(configuredPageCount > 0) { "Banner has no pages." }
        require(page in 0 until configuredPageCount) {
            "page ($page) must be in 0 until $configuredPageCount"
        }
    }

    companion object {
        private const val BOUNDARY_PAGE_COUNT = 2

        internal val Saver = Saver<BannerState, Int>(
            save = { it.currentPage },
            restore = { BannerState(initialPage = it) },
        )
    }
}

/** Remembers a saveable [BannerState]. */
@Composable
fun rememberBannerState(initialPage: Int = 0): BannerState {
    require(initialPage >= 0) { "initialPage must be at least 0" }
    return rememberSaveable(saver = BannerState.Saver) {
        BannerState(initialPage = initialPage)
    }
}
