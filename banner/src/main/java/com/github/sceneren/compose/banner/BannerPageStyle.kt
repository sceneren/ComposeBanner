package com.github.sceneren.compose.banner

import androidx.annotation.IntDef

@IntDef(
    BannerPageStyle.NORMAL,
    BannerPageStyle.MULTI_PAGE_OVERLAP,
    BannerPageStyle.MULTI_PAGE_SCALE,
)
annotation class BannerPageStyle {
    companion object {

        const val NORMAL: Int = 0

        /**
         * Requires Api Version >= 21
         */
        const val MULTI_PAGE_OVERLAP: Int = 1 shl 2

        const val MULTI_PAGE_SCALE: Int = 1 shl 3
    }
}