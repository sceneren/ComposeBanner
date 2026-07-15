package com.github.sceneren.compose.banner.indicator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Compact `current / total` figure indicator. */
@Composable
fun NumberIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    backgroundColor: Color = Color(0x66000000),
    fontSize: TextUnit = 12.sp,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 4.dp,
) {
    if (pageCount <= 0) return
    val normalizedPage = Math.floorMod(currentPage, pageCount)
    BasicText(
        text = "${normalizedPage + 1} / $pageCount",
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(100))
            .padding(horizontalPadding, verticalPadding),
        style = TextStyle(color = textColor, fontSize = fontSize),
    )
}
