# ComposeBanner ![Jitpack](https://jitpack.io/v/sceneren/ComposeBanner.svg)

一个完全使用 Jetpack Compose 实现的 Banner 组件，能力对齐
[BannerViewPager](https://github.com/zhpanvip/BannerViewPager)，但 API 按 Compose 的状态提升、插槽和
`Modifier` 模型重新设计。

## 模块

- `:banner`：轮播、自动播放、横向/纵向、有限页真循环、多页露出和页面变换。
- `:indicator`：可独立使用的 Indicator，支持 Circle、Dash、RoundRect，以及 Normal、Smooth、
  Worm、Color、Scale 五种滑动模式。
- `:app`：Tab 分组全样式 showcase（Banner / Indicator / 组合）。

Banner 与 Indicator 相互解耦。只需要轮播时无需引入 Indicator。

## 有界无限循环

循环模式使用有限倍增虚拟页（`pageCount * LOOP_COUNT`），每一页两侧始终是真实内容页，
因此 multi-page 露出、手势滑过首尾时不会出现空白邻居。停止滚动后若偏离中间块会无动画归位。
实现中没有使用 `Int.MAX_VALUE` 哨兵，也没有复制业务数据列表。

## 依赖

```kotlin
implementation("com.github.sceneren:ComposeBanner:<version>")
implementation("com.github.sceneren:ComposeIndicator:<version>")
```

发布坐标、库版本、示例版本统一配置在根目录 `gradle.properties`：

```properties
GROUP=com.github.sceneren
VERSION_NAME=1.0.0
VERSION_CODE=1
BANNER_ARTIFACT_ID=ComposeBanner
INDICATOR_ARTIFACT_ID=ComposeBannerIndicator
```

## 基础用法

```kotlin
val state = rememberBannerState()

Box {
    Banner(
        pageCount = images.size,
        state = state,
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
    ) {
        Image(
            painter = painterResource(images[index]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }

    BannerIndicator(
        pageCount = images.size,
        currentPage = state.currentPage,
        currentPageOffsetFraction = state.currentPageOffsetFraction,
        style = IndicatorStyle.RoundRect,
        slideMode = IndicatorSlideMode.Worm,
        modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
    )
}
```

## 多页、缩放和重叠

```kotlin
Banner(
    pageCount = images.size,
    contentPadding = PaddingValues(horizontal = 48.dp),
    pageSpacing = 12.dp,
    pageTransformer = BannerPageTransformers.overlap(
        minimumScale = 0.82f,
        overlapFraction = 0.08f,
    ),
) {
    // page content
}
```

内置 `None`、`scale(...)`、`overlap(...)`、`depth(...)` 变换，也可以通过
`BannerPageTransformer` 完全自定义。`CustomIndicator` 提供连续的选中比例，可实现图片、文字或任意
Composable 指示器。

## 状态控制

```kotlin
scope.launch {
    state.animateScrollToPage(2)
    state.animateScrollBy(BannerScrollDirection.Next)
    state.scrollToPage(0)
}
```

自动播放会在用户拖动或宿主 Lifecycle 不处于 `STARTED` 时暂停。

只有一页时默认禁止手势滑动（`disableScrollWhenSinglePage = true`）。若希望单页也可循环滑动：

```kotlin
Banner(
    pageCount = 1,
    infiniteLoop = true,
    disableScrollWhenSinglePage = false,
) {
    // content
}
```
