@file:OptIn(ExperimentalFoundationApi::class)

package com.github.sceneren.compose.banner

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class IndicatorState(
    var total: Int = 0,
    internal var loopCount: Int
) {
    lateinit var pagerState: AnchoredDraggableState<Int>

    val current: Int
        get() {
            var current = pagerState.targetValue - loopCount
            when {
                current < 0 -> current = total - 1

                current > total - 1 -> current = 0
            }
            return current
        }


    val from: Int get() = pagerState.currentValue - loopCount
    val to: Int get() = pagerState.targetValue - loopCount
    val fraction: Float get() = pagerState.progress

}

@Composable
fun rememberIndicatorState(
    total: Int = 0,
    loopCount: Int = 1,
): IndicatorState {
    return remember {
        IndicatorState(total, loopCount)
    }
}

/**
 * @param content 自定义指示器内容
 */
@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    state: IndicatorState,
    orientation: Orientation = Orientation.Horizontal,
    content: @Composable (state: IndicatorState) -> Unit = { indicatorState ->
        for (i in 0 until indicatorState.total) {
            val select = indicatorState.current == i
            Spacer(
                modifier = Modifier
                    .size(if (select) 18.dp else 6.dp, 6.dp)
                    .background(
                        if (select) Color.White else Color.Gray,
                        CircleShape
                    )
            )
            if (i < indicatorState.total - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

    },
) {
    if (orientation == Orientation.Horizontal) {
        Row(modifier = modifier) {
            content(state)

        }
    } else {
        Column(modifier = modifier) {
            content(state)
        }
    }


}


/**
 * @param density dp转换为px的密度单位。默认为当前设备的密度。
 *
 * @param positionalThreshold 松手后会执行动画的位置阈值。默认位移一半。
 *
 * @param velocityThreshold 松手后的滑动速度阈值，超速后可以忽略位置阈值执行动画。
 *
 * @param loop 是否循环。
 *
 * @param loopCount 首尾分别添加的数量。默认从1开始。0的时候不能循环滑动。
 *
 * @param autoSwipe 是否自动滑动。
 *
 * @param orientation 滑动方向。
 *
 * @param duration 间隔时间。
 *
 * @param makeLoop 自定义列表插入数据方法。
 *
 * @param widthPx 根据[orientation]为宽度或者高度，默认全屏宽度或者高度
 *
 * @param animation 拖动屏幕的动画效果
 *
 * @param customAnimation 自定义item绘制动画效果
 *
 * @param data list数据
 *
 * @param content 自定义item
 */
@Composable
fun <T> Banner(
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    positionalThreshold: (Float) -> Float = { it * 0.5f },
    velocityThreshold: Dp = 125.dp,
    loop: Boolean = true,
    loopCount: Int = 1,
    autoSwipe: Boolean = true,
    indicatorState: IndicatorState = rememberIndicatorState(),
    indicatorEnable: Boolean = true,
    orientation: Orientation = Orientation.Horizontal,
    duration: Long = 3000L,
    makeLoop: List<T>.(Int) -> List<T> = { loop(it) },
    widthPx: Float = if (orientation == Orientation.Horizontal) LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    else LocalContext.current.resources.displayMetrics.heightPixels.toFloat(),
    animation: AnimationSpec<Float> = tween(),
    customAnimation: @Composable Modifier.(AnchoredDraggableState<Int>, index: Int, count: Int, widthPx: Float) -> Modifier = { state, index, count, width ->
        val originOffset = index * widthPx.roundToInt()
        graphicsLayer {
            when (orientation) {
                Orientation.Vertical -> {
                    translationY = originOffset + state.requireOffset()
                }

                Orientation.Horizontal -> {
                    translationX = originOffset + state.requireOffset()

                }
            }
        }

    },
    data: List<T>,
    content: @Composable (index: Int, item: T, state: AnchoredDraggableState<Int>, width: Float) -> Unit,
) {

    if (data.isEmpty()) {
        Box(modifier = modifier) {
            return
        }
    }
    var loopEnable by remember {
        mutableStateOf(loop)
    }
    var autoSwipeEnable by remember {
        mutableStateOf(autoSwipe)
    }
    /**
     * 循环滚动就给列表头尾各添加一条数据。
     * 如果只有一条数据禁止滑动和循环。
     */
    if (data.size == 1) {
        loopEnable = false
        autoSwipeEnable = false
    }
    val list by remember {
        derivedStateOf {
            if (loopEnable) {
                data.makeLoop(loopCount)
            } else {
                data
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val count by remember {
        mutableIntStateOf(list.size)
    }

    var enabled by remember {
        mutableStateOf(true)
    }

    /**
     * 初始化位置
     */
    val start by remember {
        derivedStateOf {
            //只有一条数据时的情况
            if (loopEnable) minOf(count - 1, loopCount) else 0

        }
    }


    val state = remember {
        AnchoredDraggableState(
            initialValue = start,
            anchors = DraggableAnchors {
                for (i in 0 until count) {
                    i at -i * widthPx
                }
            },
            positionalThreshold = positionalThreshold,
            velocityThreshold = { with(density) { velocityThreshold.toPx() } },
            snapAnimationSpec = animation,
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = { true }
        )
    }
    //indicator指示器
    if (indicatorEnable) {
        val total by
        remember {
            derivedStateOf {
                if (count > 2 * loopCount) {
                    count - 2 * loopCount
                } else {
                    count
                }
            }
        }
        indicatorState.pagerState = state
        indicatorState.loopCount = loopCount
        indicatorState.total = total
    }

    Box(
        modifier = Modifier.anchoredDraggable(
            state = state,
            orientation = orientation,
            enabled = if (count <= 1) false else enabled,
        )
    ) {
        for (i in 0 until count) {
            Box(modifier = with(Modifier) { customAnimation(state, i, count, widthPx) }
            ) {
                content(i - loopCount, list[i], state, widthPx)
            }
        }
    }


    if (loopEnable) {
//        从末尾向右滑动
        val rightState by remember {
            derivedStateOf {
                state.targetValue == count - loopCount && state.progress > 0.99f
            }
        }
        SideEffect {
            if (rightState) {
                enabled = false
                coroutineScope.launch {
                    state.snapTo(start)
                }
            } else {
                enabled = true
            }
        }
        //从初始位置向左滑动
        val leftState by remember {
            derivedStateOf {
                state.targetValue == start - 1 && state.progress > 0.99f
            }
        }
        SideEffect {
            if (leftState) {
                enabled = false
                coroutineScope.launch {
                    if (count - 1 - loopCount in 0 until state.anchors.size) {
                        state.snapTo(count - 1 - loopCount)
                    }
                }
            } else {
                enabled = true
            }
        }


    }

    if (autoSwipeEnable) {
        val autoState by remember {
            derivedStateOf {
                state.targetValue + 1 in 0 until state.anchors.size
            }
        }
        LaunchedEffect(state.targetValue) {
            delay(duration)
            if (autoState && !state.isAnimationRunning) {
                coroutineScope.launch {
                    state.animateTo(state.targetValue + 1)
                }
            }
        }
    }
}

/**
 * 开头插入最后一个，末尾加入第一个
 * 默认插入一条。
 * 要实现叠加效果的banner再添加多个。
 *
 */
fun <T> List<T>.loop(loopCount: Int): List<T> {
    val result = ArrayList<T>()
    if (isEmpty()) {
        return result
    }
    result.addAll(this)
    for (i in 0 until loopCount) {
        //头部位置插入
        result.add(i, this[size - 1 - i])
        //末尾添加
        result.add(this[i])
    }
    return result

}

