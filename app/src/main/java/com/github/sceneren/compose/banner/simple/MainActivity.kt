package com.github.sceneren.compose.banner.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.BannerPageStyle
import com.github.sceneren.compose.banner.IndicatorGravity
import com.github.sceneren.compose.banner.IndicatorSlideMode
import com.github.sceneren.compose.banner.IndicatorStyle
import com.github.sceneren.compose.banner.simple.theme.ComposeBannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeBannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val list = listOf(
        R.drawable.a1, R.drawable.a2,
        R.drawable.a3
    )

    var looper by remember {
        mutableStateOf(true)
    }

    Column {
        Banner(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            items = list,
            looper = looper,
            indicatorGravity = IndicatorGravity.END,
            indicatorNormalColor = Color(0xFFFFFFFF),
            indicatorCheckColor = Color(0xFFF95521),
            indicatorNormalWidth = 4.dp,
            indicatorCheckWidth = 8.dp,
            indicatorHeight = 4.dp,
            indicatorSlideMode = IndicatorSlideMode.SCALE,
            indicatorStyle = IndicatorStyle.ROUND_RECT,
            pageStyle = BannerPageStyle.MULTI_PAGE_SCALE,
            pageScale = 0.85f,
            pageRevealWidth = 10.dp,
            pageMargin = 10.dp,
            itemBuilder = { item, index ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(item),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            },
//        onCreate = {
//
//        }
        )

        Button({
            looper = !looper
        }) {
            Text("切换")
        }
    }


}