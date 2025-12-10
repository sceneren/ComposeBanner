![Jitpack](https://jitpack.io/v/sceneren/ComposeBanner.svg)
# ComposeBanner
一个基于Android Compose的Banner组件，支持循环滚动（模拟无限滚动）。

## Compatibility

| ComposeBanner  | Jetpack Compose |
|----------------|-----------------|
| 0.1.6+         |     1.10.0      |

## Get started
1、Add it in your settings.gradle.kts at the end of repositories:
```kotlin
maven { url = uri("https://jitpack.io") }
```

2、Add dependency:

```kotlin
// Replace <TAG> with the latest version
implementation("com.github.sceneren:ComposeBanner:Tag")
```

3、用法:

```kotlin
@Composable
fun BannerTest() {
    val bannerState = rememberBannerState()

    Banner(
         modifier = Modifier
             .fillMaxWidth()
             .aspectRatio(1f),
         pageCount = list.size,
         bannerState = bannerState
     ) {
         Image(
             modifier = Modifier.fillMaxSize(),
             painter = painterResource(list[index]),
             contentDescription = null,
             contentScale = ContentScale.Crop
         )
     }

    //PagerIndicator
    PagerIndicator(
        modifier = Modifier.padding(end = 10.dp, bottom = 10.dp),
        size = list.size,
        offsetPercentWithSelectFlow = bannerState.createChildOffsetPercentFlow(),
        selectIndexFlow = bannerState.createCurrSelectIndexFlow(),
        indicatorItem = {
            Image(
                modifier = Modifier
                    .size(width = 4.dp, height = 3.dp)
                    .clip(RoundedCornerShape(100)),
                painter = defaultIndicatorPainter,
                contentDescription = null
            )
        },
        selectIndicatorItem = {
            Image(
                modifier = Modifier
                    .size(width = 10.dp, height = 3.dp)
                    .clip(RoundedCornerShape(100)),
                painter = selectIndicatorPainter,
                contentDescription = null
            )
        }
    )
}
```


## Thanks
- [ComposeViews](https://github.com/ltttttttttttt/ComposeViews)：ComposeBanner只是从ComposeViews中提取出对应的文件来方便依赖。而不是完整依赖于ComposeViews
