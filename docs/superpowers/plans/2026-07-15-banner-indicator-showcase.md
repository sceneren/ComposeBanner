# Banner/Indicator Full Showcase Implementation Plan

> 状态：已按设计落地到 `:app`，MainActivity 仅保留 Tab 壳，内容按 samples 拆分。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `:app` 中实现 Tab 分组的全样式演示页，覆盖 Banner 全部变换/方向/循环能力与 Indicator 全部样式×滑动模式矩阵。

**Architecture:** `MainActivity` 提供 Scaffold + PrimaryTabRow；三个 showcase 文件分别承载 Banner / Indicator / 组合内容；公共卡片与 section 抽到 `SampleCommon`。只组合现有库 API，不改库行为。

**Tech Stack:** Kotlin, Jetpack Compose, Material3, 现有 `:banner` / `:indicator`

**Spec:** `docs/superpowers/specs/2026-07-15-banner-indicator-showcase-design.md`

---

### Task 1: SampleCommon 公共组件

**Files:**
- Create: `app/src/main/java/com/github/sceneren/compose/banner/simple/samples/SampleCommon.kt`

- [ ] **Step 1: 创建公共 composable**

包含：
- `sampleImages()`
- `SampleSection(title, content)`
- `SampleBannerFrame(modifier, content)`
- `BannerControlRow(autoPlay, onToggleAutoPlay, onNext)`
- `SampleBannerImage(imageRes, contentDescription)`

- [ ] **Step 2: 编译 `:app` 确认公共组件无误**

Run: `./gradlew :app:compileDebugKotlin`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/github/sceneren/compose/banner/simple/samples/SampleCommon.kt
git commit -m "feat(app): add showcase common helpers"
```

### Task 2: BannerShowcase

**Files:**
- Create: `app/src/main/java/com/github/sceneren/compose/banner/simple/samples/BannerShowcase.kt`

- [ ] **Step 1: 实现 Banner 全能力 section**
  - 基础循环 + autoPlay
  - None / scale / overlap / depth
  - 纵向
  - 非循环

- [ ] **Step 2: 编译**

Run: `./gradlew :app:compileDebugKotlin`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/github/sceneren/compose/banner/simple/samples/BannerShowcase.kt
git commit -m "feat(app): add banner capability showcase"
```

### Task 3: IndicatorShowcase

**Files:**
- Create: `app/src/main/java/com/github/sceneren/compose/banner/simple/samples/IndicatorShowcase.kt`

- [ ] **Step 1: 实现 Indicator 矩阵**
  - 顶部驱动 Banner
  - 3×5 样式矩阵
  - NumberIndicator
  - CustomIndicator
  - 纵向指示器

- [ ] **Step 2: 编译**

Run: `./gradlew :app:compileDebugKotlin`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/github/sceneren/compose/banner/simple/samples/IndicatorShowcase.kt
git commit -m "feat(app): add indicator style matrix showcase"
```

### Task 4: CombinedShowcase + MainActivity Tab 壳

**Files:**
- Create: `app/src/main/java/com/github/sceneren/compose/banner/simple/samples/CombinedShowcase.kt`
- Modify: `app/src/main/java/com/github/sceneren/compose/banner/simple/MainActivity.kt`

- [ ] **Step 1: 实现组合场景**
- [ ] **Step 2: MainActivity 改为 TopAppBar + 3 Tab**
- [ ] **Step 3: 全量编译与单测**

Run:
```bash
./gradlew :app:compileDebugKotlin :banner:testDebugUnitTest :indicator:testDebugUnitTest
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/github/sceneren/compose/banner/simple/
git commit -m "feat(app): wire tabbed full style showcase"
```

### Task 5: README 补充 + 验收

**Files:**
- Modify: `README.md`

- [ ] **Step 1: README 增加 app showcase 说明**
- [ ] **Step 2: 最终编译验证**
- [ ] **Step 3: Commit**
