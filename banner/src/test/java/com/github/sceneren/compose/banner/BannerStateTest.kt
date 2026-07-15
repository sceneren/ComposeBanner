package com.github.sceneren.compose.banner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BannerStateTest {
    @Test
    fun loopingPagerUsesBoundedMultiplierNotSentinelPages() {
        val state = BannerState(initialPage = 0)

        state.updateConfiguration(pageCount = 5, infiniteLoop = true)

        // 使用有限倍增虚拟页，而不是 pageCount+2 哨兵（哨兵在边界没有相邻页）。
        assertEquals(5 * BannerState.LOOP_COUNT, state.virtualPageCount)
        assertTrue(state.virtualPageCount > 5)
    }

    @Test
    fun loopingPagesMapByModulo() {
        val state = BannerState(initialPage = 0)
        state.updateConfiguration(pageCount = 3, infiniteLoop = true)

        val middle = 3 * (BannerState.LOOP_COUNT / 2)
        assertEquals(0, state.logicalPageFor(middle))
        assertEquals(1, state.logicalPageFor(middle + 1))
        assertEquals(2, state.logicalPageFor(middle + 2))
        assertEquals(0, state.logicalPageFor(middle + 3))
        assertEquals(2, state.logicalPageFor(middle - 1))
    }

    @Test
    fun nonLoopingPagerHasNoExtraPages() {
        val state = BannerState(initialPage = 0)

        state.updateConfiguration(pageCount = 5, infiniteLoop = false)

        assertEquals(5, state.virtualPageCount)
        assertEquals(2, state.logicalPageFor(2))
    }

    @Test
    fun physicalPageForInitialLogicalStartsInMiddleBlock() {
        val state = BannerState(initialPage = 1)
        state.updateConfiguration(pageCount = 3, infiniteLoop = true)

        val expected = 3 * (BannerState.LOOP_COUNT / 2) + 1
        assertEquals(expected, state.physicalPageFor(1))
    }

    @Test
    fun animateTargetPrefersShortPathAcrossLogicalBoundary() {
        val state = BannerState(initialPage = 0)
        state.updateConfiguration(pageCount = 3, infiniteLoop = true)
        // 假定当前落在中间块第 0 页
        val currentPhysical = 3 * (BannerState.LOOP_COUNT / 2)
        // 从逻辑 0 到逻辑 2，短路径应是 current-1，而不是 +2
        assertEquals(
            currentPhysical - 1,
            state.animateTargetPhysicalPage(
                currentPhysical = currentPhysical,
                targetLogicalPage = 2,
            ),
        )
        // 从逻辑 0 到逻辑 1，短路径应是 current+1
        assertEquals(
            currentPhysical + 1,
            state.animateTargetPhysicalPage(
                currentPhysical = currentPhysical,
                targetLogicalPage = 1,
            ),
        )
    }

    @Test
    fun beyondViewportIsNotForcedToAllPagesInLoopMode() {
        // 倍增架构任意位置都有真实相邻页，无需把 beyond 拉满。
        val resolved = resolveBeyondViewportPageCount(
            pageCount = 3,
            infiniteLoop = true,
            requested = 1,
        )
        assertEquals(1, resolved)
    }

    @Test
    fun singlePageDefaultsToNonLoopVirtualCount() {
        val state = BannerState(initialPage = 0)
        state.updateConfiguration(
            pageCount = 1,
            infiniteLoop = true,
            allowSinglePageLoop = false,
        )

        assertEquals(1, state.virtualPageCount)
        assertEquals(false, state.canLoop)
    }

    @Test
    fun singlePageCanLoopWhenAllowed() {
        val state = BannerState(initialPage = 0)
        state.updateConfiguration(
            pageCount = 1,
            infiniteLoop = true,
            allowSinglePageLoop = true,
        )

        assertEquals(1 * BannerState.LOOP_COUNT, state.virtualPageCount)
        assertEquals(true, state.canLoop)
        assertEquals(0, state.logicalPageFor(state.physicalPageFor(0) + 1))
    }

    @Test
    fun effectiveUserScrollDisabledForSinglePageByDefault() {
        assertEquals(
            false,
            resolveUserScrollEnabled(
                pageCount = 1,
                userScrollEnabled = true,
                disableScrollWhenSinglePage = true,
            ),
        )
        assertEquals(
            true,
            resolveUserScrollEnabled(
                pageCount = 1,
                userScrollEnabled = true,
                disableScrollWhenSinglePage = false,
            ),
        )
        assertEquals(
            true,
            resolveUserScrollEnabled(
                pageCount = 3,
                userScrollEnabled = true,
                disableScrollWhenSinglePage = true,
            ),
        )
    }
}
