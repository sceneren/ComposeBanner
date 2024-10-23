package com.github.sceneren.compose.banner

import androidx.annotation.IntDef

@IntDef(
    IndicatorGravity.CENTER,
    IndicatorGravity.START,
    IndicatorGravity.END,
)
annotation class IndicatorGravity {
    companion object {
        const val CENTER: Int = 0
        const val START: Int = 1 shl 1
        const val END: Int = 1 shl 2
    }
}