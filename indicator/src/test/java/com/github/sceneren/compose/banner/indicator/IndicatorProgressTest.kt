package com.github.sceneren.compose.banner.indicator

import org.junit.Assert.assertEquals
import org.junit.Test

class IndicatorProgressTest {
    @Test
    fun positiveOffsetTargetsNextPage() {
        val progress = resolveIndicatorProgress(4, 1, 0.25f)

        assertEquals(1, progress.currentPage)
        assertEquals(2, progress.targetPage)
        assertEquals(0.25f, progress.fraction)
    }

    @Test
    fun negativeOffsetWrapsToLastPage() {
        val progress = resolveIndicatorProgress(4, 0, -0.4f)

        assertEquals(0, progress.currentPage)
        assertEquals(3, progress.targetPage)
        assertEquals(0.4f, progress.fraction)
    }

    @Test
    fun selectionFractionsAreContinuous() {
        val progress = resolveIndicatorProgress(4, 1, 0.25f)

        assertEquals(0.75f, progress.selectionFraction(1))
        assertEquals(0.25f, progress.selectionFraction(2))
        assertEquals(0f, progress.selectionFraction(3))
    }
}
