package com.github.sceneren.compose.banner.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.sceneren.compose.banner.simple.samples.BannerShowcase
import com.github.sceneren.compose.banner.simple.samples.CombinedShowcase
import com.github.sceneren.compose.banner.simple.samples.IndicatorShowcase
import com.github.sceneren.compose.banner.simple.theme.ComposeBannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeBannerTheme {
                ShowcaseApp()
            }
        }
    }
}

/** MainActivity 仅作 Tab 壳；各 Tab 内容在 samples 包拆分实现。 */
private enum class ShowcaseTab(val title: String) {
    BannerTab("Banner"),
    IndicatorTab("Indicator"),
    CombinedTab("组合"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowcaseApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = ShowcaseTab.entries

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("ComposeBanner Samples") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab.title) },
                    )
                }
            }

            val contentModifier = Modifier.fillMaxSize()
            when (tabs[selectedTab]) {
                ShowcaseTab.BannerTab -> BannerShowcase(modifier = contentModifier)
                ShowcaseTab.IndicatorTab -> IndicatorShowcase(modifier = contentModifier)
                ShowcaseTab.CombinedTab -> CombinedShowcase(modifier = contentModifier)
            }
        }
    }
}
