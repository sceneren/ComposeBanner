package com.github.sceneren.compose.banner

import androidx.annotation.IntDef

@IntDef(
    IndicatorStyle.CIRCLE,
    IndicatorStyle.DASH,
    IndicatorStyle.ROUND_RECT,
)
annotation class IndicatorStyle {
    companion object {
        const val CIRCLE = 0
        const val DASH = 1 shl 1
        const val ROUND_RECT = 1 shl 2
    }
}