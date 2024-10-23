package com.github.sceneren.compose.banner.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.sceneren.compose.banner.Banner
import com.github.sceneren.compose.banner.Indicator
import com.github.sceneren.compose.banner.rememberIndicatorState
import com.github.sceneren.compose.banner.rememberPagerSwipeState
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val list = listOf(
        R.drawable.a1, R.drawable.a2,
        R.drawable.a3
    )

    var looper by remember {
        mutableStateOf(true)
    }

    val linearPagerSwipeState = rememberPagerSwipeState()

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = LocalDensity.current.run {
        screenWidthDp.toPx()
    }
    val indicatorState = rememberIndicatorState()


    LazyColumn(modifier = Modifier.fillMaxSize()) {

//        item {
//            LinearPager(
//                data = list,// data list
//                pagerSwipeState = linearPagerSwipeState,// indicator need this
//                duration = 3000,// auto scroll delay
//                widthPx = screenWidthPx// ⚠️ need a fixed width, it's very important!
//            ) { item, index -> // `it` is the data list's item, index is list's index
//                Image(
//                    modifier = Modifier.fillMaxSize(),
//                    painter = painterResource(item),
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop
//                )
//            }
//        }


        item {
            Column {
                Banner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    widthPx = screenWidthPx,
                    indicatorEnable = true,
                    indicatorState = indicatorState,
                    data = list
                ) { index, item, state, width ->
                    Column {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(item),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }

                }
                Indicator(state = indicatorState)
            }

        }

        item {
            Banner(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                pageCount = list.size
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(list[index]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }

        items(30) {
            Button({
                looper = !looper
            }) {
                Text("切换")
            }
        }


    }


}