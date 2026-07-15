package com.github.sceneren.compose.banner

import org.junit.Assert.assertEquals
import org.junit.Test

class BannerStateTest {
    @Test
    fun loopingPagerIsStrictlyBounded() {
        val state = BannerState(initialPage = 0)

        state.updateConfiguration(pageCount = 5, infiniteLoop = true)

        assertEquals(7, state.virtualPageCount)
    }

    @Test
    fun boundaryPagesMapToOppositeLogicalPages() {
        val state = BannerState(initialPage = 0)
        state.updateConfiguration(pageCount = 3, infiniteLoop = true)

        assertEquals(2, state.logicalPageFor(0))
        assertEquals(0, state.logicalPageFor(1))
        assertEquals(2, state.logicalPageFor(3))
        assertEquals(0, state.logicalPageFor(4))
    }

    @Test
    fun nonLoopingPagerHasNoExtraPages() {
        val state = BannerState(initialPage = 0)

        state.updateConfiguration(pageCount = 5, infiniteLoop = false)

        assertEquals(5, state.virtualPageCount)
    }
}
