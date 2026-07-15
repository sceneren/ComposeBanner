package com.github.sceneren.compose.banner

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.graphics.GraphicsLayerScope
import kotlin.math.absoluteValue

enum class BannerScrollDirection {
    Next,
    Previous,
}

/** A graphics-layer transformation applied to every visible page. */
fun interface BannerPageTransformer {
    fun transform(
        layer: GraphicsLayerScope,
        pageOffset: Float,
        orientation: Orientation,
    )
}

object BannerPageTransformers {
    val None = BannerPageTransformer { _, _, _ -> }

    fun scale(
        minimumScale: Float = 0.85f,
        minimumAlpha: Float = 1f,
    ): BannerPageTransformer {
        require(minimumScale in 0f..1f) { "minimumScale must be in 0f..1f" }
        require(minimumAlpha in 0f..1f) { "minimumAlpha must be in 0f..1f" }
        return BannerPageTransformer { layer, offset, _ ->
            val distance = offset.absoluteValue.coerceIn(0f, 1f)
            val scale = 1f - (1f - minimumScale) * distance
            layer.scaleX = scale
            layer.scaleY = scale
            layer.alpha = 1f - (1f - minimumAlpha) * distance
        }
    }

    fun overlap(
        minimumScale: Float = 0.85f,
        overlapFraction: Float = 0.2f,
    ): BannerPageTransformer {
        require(minimumScale in 0f..1f) { "minimumScale must be in 0f..1f" }
        require(overlapFraction in 0f..1f) { "overlapFraction must be in 0f..1f" }
        return BannerPageTransformer { layer, offset, orientation ->
            val distance = offset.absoluteValue.coerceIn(0f, 1f)
            val scale = 1f - (1f - minimumScale) * distance
            layer.scaleX = scale
            layer.scaleY = scale
            when (orientation) {
                Orientation.Horizontal -> layer.translationX =
                    layer.size.width * overlapFraction * offset
                Orientation.Vertical -> layer.translationY =
                    layer.size.height * overlapFraction * offset
            }
        }
    }

    fun depth(minimumScale: Float = 0.75f): BannerPageTransformer {
        require(minimumScale in 0f..1f) { "minimumScale must be in 0f..1f" }
        return BannerPageTransformer { layer, offset, orientation ->
            val positiveOffset = offset.coerceIn(0f, 1f)
            layer.alpha = 1f - positiveOffset
            val scale = 1f - (1f - minimumScale) * positiveOffset
            layer.scaleX = scale
            layer.scaleY = scale
            when (orientation) {
                Orientation.Horizontal -> layer.translationX = -layer.size.width * positiveOffset
                Orientation.Vertical -> layer.translationY = -layer.size.height * positiveOffset
            }
        }
    }
}

object BannerDefaults {
    const val AutoPlayIntervalMillis: Long = 3_000L
}
