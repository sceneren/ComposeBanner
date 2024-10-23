package com.github.sceneren.compose.banner

import androidx.annotation.IntDef

@IntDef(
    IndicatorSlideMode.NORMAL,
    IndicatorSlideMode.SMOOTH,
    IndicatorSlideMode.WORM,
    IndicatorSlideMode.SCALE,
    IndicatorSlideMode.COLOR
)
annotation class IndicatorSlideMode {
    companion object {
        const val NORMAL = 0
        const val SMOOTH = 2
        const val WORM = 3
        const val SCALE = 4
        const val COLOR = 5
    }
}