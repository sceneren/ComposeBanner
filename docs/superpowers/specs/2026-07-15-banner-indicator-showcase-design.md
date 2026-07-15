# ComposeBanner 全样式演示页设计

日期：2026-07-15  
范围：仅 `:app` 示例模块；不改 `:banner` / `:indicator` 公共 API

## 背景

当前 `MainActivity` 只覆盖了少量 Banner/Indicator 组合：

- 一个有界无限循环 Banner + RoundRect/Worm + Number
- 一个 multi-page overlap Banner
- 一组静态 Indicator slide mode 预览（固定 offset，不可交互）

库本身已具备更完整能力，需要一份可交互 showcase，覆盖 Banner 与 Indicator 的全部样式与主要能力，便于人工回归与对外演示。

## 目标

1. 用 Tab 分组展示 **Banner 全能力**、**Indicator 全样式矩阵**、**组合场景**。
2. 所有视觉效果可真实滑动/自动播放，不只是静态截图或固定 fraction。
3. 示例代码结构清晰，后续补样式时只需加 section，不继续膨胀单文件。
4. 不引入 Navigation Compose；不修改库 API。

## 非目标

- 不新增库功能、不改 Banner/Indicator 行为
- 不做截图自动化 UI 测试（本期仅人工可交互 demo）
- 不接入网络图片；继续使用本地 `a1/a2/a3`
- 不做主题切换、深色专属皮肤

## 信息架构

```text
Scaffold
├─ TopAppBar: "ComposeBanner Samples"
├─ PrimaryTabRow: Banner | Indicator | 组合
└─ 当前 Tab 内容（LazyColumn）
     0 → BannerShowcase
     1 → IndicatorShowcase
     2 → CombinedShowcase
```

Tab 切换使用本地 `selectedTabIndex` 状态即可，不需要 `HorizontalPager` 强绑手势（避免与内部 Banner 横向滑动冲突）。若后续需要滑动切 Tab，必须禁用嵌套冲突处理；本期默认点击 Tab 切换。

## Tab 内容规格

### Tab 0：Banner

每个 section 标题 + 可交互卡片。

| Section | 验证点 |
|---|---|
| 基础循环 + 自动播放 | `infiniteLoop=true`、`autoPlay` 可 Pause/Play、Next 按钮、页码 |
| None 变换 | `BannerPageTransformers.None` |
| Scale 变换 | `BannerPageTransformers.scale(...)` |
| Overlap 多页露出 | `contentPadding` + `pageSpacing` + `overlap(...)` |
| Depth 变换 | `BannerPageTransformers.depth(...)` |
| 纵向 Banner | `Orientation.Vertical`，固定高度约 220.dp |
| 非循环 Banner | `infiniteLoop=false`，验证到首尾不可继续循环 |

约定：

- 每个独立 Banner 使用独立 `rememberBannerState(...)`
- 默认图片列表：`listOf(R.drawable.a1, R.drawable.a2, R.drawable.a3)`
- 卡片统一圆角裁剪与 16:9（纵向除外）

### Tab 1：Indicator

| Section | 验证点 |
|---|---|
| 驱动 Banner | 顶部一个真实可滑 Banner，向整个矩阵提供 `currentPage` / `currentPageOffsetFraction` |
| 样式 × 滑动模式矩阵 | `IndicatorStyle` 3 种 × `IndicatorSlideMode` 5 种，共 15 行 |
| NumberIndicator | 右上或独立 section 显示 `current / total` |
| CustomIndicator | 自定义 item（文字/色块/缩放等）随 `selectionFraction` 变化 |
| 纵向指示器 | 一组 `Orientation.Vertical` 的 BannerIndicator 或 CustomIndicator |

矩阵展示规则：

- 每一行左侧是 `Style · Mode` 文案，右侧是绑定同一 state 的 `BannerIndicator`
- 颜色在浅色背景上使用 Material theme onSurface/primary，避免白底白点不可见
- 点击指示器可 `animateScrollToPage`（若 `onPageSelected` 可用）

### Tab 2：组合

| Section | 验证点 |
|---|---|
| 标准叠层 | Banner + 底部 RoundRect/Worm + 右上 Number |
| 点击跳页 | 指示器点击后 Banner 动画跳转 |
| Multi-page + Custom | overlap Banner + CustomIndicator |
| 自动播放连续过渡 | autoPlay 开启时 Indicator 平滑跟随跨页 |

## 文件拆分

```text
app/src/main/java/com/github/sceneren/compose/banner/simple/
  MainActivity.kt
  samples/
    SampleCommon.kt
    BannerShowcase.kt
    IndicatorShowcase.kt
    CombinedShowcase.kt
```

### 职责

- `MainActivity.kt`：`setContent`、Scaffold、TopAppBar、TabRow、按 index 渲染 showcase
- `SampleCommon.kt`：
  - `sampleImages(): List<Int>`
  - `SampleSection(title, content)`
  - `SampleBannerFrame(...)` 统一外框/裁剪
  - 可选：统一控制条 `Play/Pause`、`Next`
- `BannerShowcase.kt`：Tab0 全部 section
- `IndicatorShowcase.kt`：Tab1 矩阵与自定义指示器
- `CombinedShowcase.kt`：Tab2 组合场景

## 交互与状态

1. **状态隔离**：跨 Tab 不共享 BannerState，避免不可见 Tab 仍自动播放抢焦点。不可见 Tab 可通过“仅组合当前选中 Tab 内容”自然停止 composition 中的 autoPlay。
2. **自动播放**：展示页默认 `autoPlay=true`，每个相关卡片提供 Pause/Play。
3. **页码反馈**：关键卡片显示 `currentPage + 1 / pageCount` 或 `NumberIndicator`。
4. **资源**：仅本地 drawable，无需权限与网络。

## 依赖

`:app` 已依赖 `:banner` 与 `:indicator`。若 Tab/TopAppBar 需要 Material3 组件，继续使用现有 `material3` 依赖；不新增第三方库。

## 测试与验收

### 自动化
- 不新增 UI 自动化测试（本期）
- 现有 `:banner` / `:indicator` 单测保持通过

### 人工验收清单
- [ ] 三个 Tab 可切换，标题与内容对应
- [ ] Banner Tab 可见 None/scale/overlap/depth
- [ ] 纵向 Banner 可上下滑
- [ ] 非循环 Banner 到边缘后不能环绕
- [ ] Indicator 15 种组合均随顶部 Banner 滑动变化
- [ ] NumberIndicator 页码正确
- [ ] CustomIndicator 选中态连续变化
- [ ] 组合页点击指示器可跳页
- [ ] 自动播放开/关有效
- [ ] 首尾循环滑动时 multi-page 无空白邻居

## 风险与处理

| 风险 | 处理 |
|---|---|
| LazyColumn 内纵向 Banner 高度测量异常 | 纵向卡片固定高度，不用无限高 |
| Tab 横滑与 Banner 横滑冲突 | 本期只用点击切 Tab，不用外层横向 Pager |
| 白底指示器看不清 | Indicator Tab 使用主题色，不用纯白 |
| 单文件过大难维护 | 按 Tab 拆 3 个 showcase 文件 |

## 实现顺序（供后续 plan）

1. 抽出 `SampleCommon` 与 `MainActivity` Tab 壳
2. 实现 `BannerShowcase`
3. 实现 `IndicatorShowcase`（含 3×5 矩阵）
4. 实现 `CombinedShowcase`
5. 真机/模拟器人工走查验收清单
6. 如有 README 示例入口说明，可补一句“app 模块含全样式 showcase”

## 成功标准

打开 app 后，无需改代码即可目视确认 Banner 与 Indicator 的全部现有样式/主要能力是否工作正常。
