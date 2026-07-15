package com.github.sceneren.compose.banner

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Stable
class BannerScope internal constructor(
    val index: Int,
    val pageOffset: Float,
)

/**
 * A lifecycle-aware Compose banner with bounded infinite looping and auto play.
 *
 * When [infiniteLoop] is enabled the pager uses a finite multiplier
 * (`pageCount * [BannerState.LOOP_COUNT]`) so every position has real neighbors.
 * After settling far from the middle block it is recentered without animation.
 * This avoids the blank peek pages of a pageCount+2 sentinel at first/last page.
 *
 * [disableScrollWhenSinglePage] defaults to true so a one-item banner cannot be
 * scrolled. Set it to false to allow single-item looping/gestures when [infiniteLoop]
 * is also enabled.
 */
@Composable
fun Banner(
    pageCount: Int,
    modifier: Modifier = Modifier,
    state: BannerState = rememberBannerState(),
    infiniteLoop: Boolean = true,
    autoPlay: Boolean = true,
    autoPlayIntervalMillis: Long = BannerDefaults.AutoPlayIntervalMillis,
    autoPlayDirection: BannerScrollDirection = BannerScrollDirection.Next,
    orientation: Orientation = Orientation.Horizontal,
    userScrollEnabled: Boolean = true,
    /**
     * 只有一页时是否禁止手势滑动。默认 true。
     * 设为 false 且 [infiniteLoop] 为 true 时，单页也可循环滑动。
     */
    disableScrollWhenSinglePage: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSpacing: Dp = 0.dp,
    pageSize: PageSize = PageSize.Fill,
    beyondViewportPageCount: Int = 1,
    pageTransformer: BannerPageTransformer = BannerPageTransformers.None,
    onPageChanged: (Int) -> Unit = {},
    content: @Composable BannerScope.() -> Unit,
) {
    require(pageCount >= 0) { "pageCount must not be negative" }
    require(autoPlayIntervalMillis > 0) { "autoPlayIntervalMillis must be positive" }
    require(beyondViewportPageCount >= 0) { "beyondViewportPageCount must not be negative" }

    // disableScrollWhenSinglePage=true（默认）时，单页不允许进入循环虚拟页。
    val allowSinglePageLoop = infiniteLoop && !disableScrollWhenSinglePage

    SideEffect {
        state.updateConfiguration(
            pageCount = pageCount,
            infiniteLoop = infiniteLoop,
            allowSinglePageLoop = allowSinglePageLoop,
        )
    }

    LaunchedEffect(state, pageCount, infiniteLoop, allowSinglePageLoop) {
        state.applyPendingPosition()
    }

    if (pageCount == 0) return

    val pagerState = state.pagerState
    val dragged by pagerState.interactionSource.collectIsDraggedAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleStarted by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            lifecycleStarted = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state, pageCount, infiniteLoop, allowSinglePageLoop) {
        snapshotFlow { pagerState.settledPage to pagerState.isScrollInProgress }
            .filter { !it.second }
            .collect { state.recenterIfNeeded() }
    }

    val currentOnPageChanged by rememberUpdatedState(onPageChanged)
    LaunchedEffect(state, pageCount) {
        snapshotFlow { state.settledPage }
            .distinctUntilChanged()
            .collect(currentOnPageChanged)
    }

    val canAutoPlay = autoPlay &&
        lifecycleStarted &&
        !dragged &&
        (pageCount > 1 || allowSinglePageLoop)

    LaunchedEffect(
        state,
        autoPlay,
        autoPlayIntervalMillis,
        autoPlayDirection,
        pageCount,
        dragged,
        lifecycleStarted,
        allowSinglePageLoop,
    ) {
        if (!canAutoPlay) return@LaunchedEffect
        while (true) {
            delay(autoPlayIntervalMillis)
            if (!pagerState.isScrollInProgress) {
                state.animateScrollBy(autoPlayDirection)
                state.recenterIfNeeded()
            }
        }
    }

    val pageContent: @Composable PagerScope.(Int) -> Unit = { physicalPage ->
        val logicalPage = state.logicalPageFor(physicalPage)
        val pageOffset =
            (pagerState.currentPage - physicalPage) + pagerState.currentPageOffsetFraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    pageTransformer.transform(this, pageOffset, orientation)
                },
        ) {
            content(BannerScope(logicalPage, pageOffset))
        }
    }

    val resolvedBeyondViewportPageCount = resolveBeyondViewportPageCount(
        pageCount = pageCount,
        infiniteLoop = infiniteLoop,
        requested = beyondViewportPageCount,
    )
    val resolvedUserScrollEnabled = resolveUserScrollEnabled(
        pageCount = pageCount,
        userScrollEnabled = userScrollEnabled,
        disableScrollWhenSinglePage = disableScrollWhenSinglePage,
    )

    when (orientation) {
        Orientation.Horizontal -> HorizontalPager(
            state = pagerState,
            modifier = modifier,
            contentPadding = contentPadding,
            pageSize = pageSize,
            beyondViewportPageCount = resolvedBeyondViewportPageCount,
            pageSpacing = pageSpacing,
            userScrollEnabled = resolvedUserScrollEnabled,
            reverseLayout = reverseLayout,
            pageContent = pageContent,
        )

        Orientation.Vertical -> VerticalPager(
            state = pagerState,
            modifier = modifier,
            contentPadding = contentPadding,
            pageSize = pageSize,
            beyondViewportPageCount = resolvedBeyondViewportPageCount,
            pageSpacing = pageSpacing,
            userScrollEnabled = resolvedUserScrollEnabled,
            reverseLayout = reverseLayout,
            pageContent = pageContent,
        )
    }
}
