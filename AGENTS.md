# Whitelist Maker Agent Guide

## 协作约定

- 默认使用简体中文，表达简洁、可复制。
- 动手前先说明计划和改动范围；执行命令前说明目的。
- 默认所有 shell 命令通过 `rtk` 执行；若 PATH 中找不到，使用 `/opt/homebrew/bin/rtk`。
- 保持改动小且聚焦，不顺手重构无关代码、升级依赖或清理生成文件。
- 不编造路径、配置、依赖、设备状态或测试结果。无法执行的验证要明确说明。
- 不读取、输出或提交 `local.properties`、签名信息、token、API key 等敏感内容。
- 复杂逻辑、高风险后台行为和数据格式变更优先补测试。

## 项目介绍

Whitelist Maker 是一个单模块 Android 应用，用于监听设备中新安装的应用，并让用户决定是否将其包名追加到白名单文件。

主要能力：

- 通过前台服务动态注册 `ACTION_PACKAGE_ADDED` 广播接收器。
- 记录新安装应用的名称、包名、安装时间和处理状态。
- 通过应用页面或通知操作将安装事件标记为“已添加”或“已忽略”。
- 导入、导出、复制和粘贴白名单文件。
- 在开机完成后按用户设置恢复监听服务。
- 使用本地 JSON、私有文件和 `SharedPreferences` 保存数据。

当前白名单格式只有 `WhitelistFileFormat.TrueHeader`。首行默认是 `false`；设置中开启“反转模式”后为 `true`：

```text
false
com.example.first
com.example.second
```

首个非空行必须为 `true` 或 `false`，其余非空行必须是合法包名。解析时兼容两种布尔值，并保留原顺序、去重。

## 技术栈

- Kotlin `2.3.21`，官方代码风格
- Android Gradle Plugin `9.2.0`
- Gradle Wrapper `9.4.1`
- Java 11 字节码目标
- `minSdk 26`、`targetSdk 36`、`compileSdk 36.1`
- Jetpack Compose + Material 3
- Lifecycle `ViewModel`、`StateFlow`、Coroutines
- Navigation Compose 依赖已引入，但当前页面切换由 `AppScreen` 内部状态控制
- JUnit 4、AndroidX Test、Espresso

项目没有 Hilt、Room、DataStore、Retrofit、detekt 或 ktlint。不要假设这些工具存在，也不要仅为小改动引入新框架。

## 目录与职责

```text
app/src/main/java/com/ajiew/whitelistmaker/
├── MainActivity.kt                 # Compose 入口、权限请求、SAF launcher、UI action 组装
├── WhitelistMakerApp.kt            # Application 与 AppContainer 初始化
├── core/
│   └── AppContainer.kt             # 手写依赖注入
├── domain/
│   └── Models.kt                   # UI、设置、安装事件和白名单领域模型
├── data/
│   ├── AppPreferences.kt           # SharedPreferences 与设置 StateFlow
│   ├── InstallEventStore.kt        # install_events.json 的读写
│   ├── WhitelistFileStore.kt       # 默认文件、导入 URI、白名单读写
│   ├── WhitelistFormatParser.kt    # 纯 Kotlin 格式解析与生成
│   └── WhitelistRepository.kt      # 数据编排、聚合 UI 状态、业务操作
├── monitor/standard/
│   ├── InstallMonitorService.kt    # 前台服务和安装广播监听
│   ├── NotificationHelper.kt       # 通知渠道、前台通知、安装事件通知
│   ├── InstallEventActionReceiver.kt # 通知“添加/忽略”操作
│   ├── BootReceiver.kt             # 开机恢复服务
│   └── ServiceController.kt        # 服务启动和停止入口
└── ui/
    ├── AppScreen.kt                # Scaffold、底部导航、撤销删除 Snackbar
    ├── AppActions.kt               # UI 事件回调集合
    ├── AppViewModel.kt             # Repository 到 UI 的生命周期边界
    ├── PreviewDefaults.kt          # Compose Preview 固定数据
    ├── home/                       # 服务状态与安装事件页面
    ├── whitelist/                  # 白名单列表页面
    ├── settings/                   # 设置页面
    ├── components/                 # 跨页面复用的小型 Compose 组件
    └── theme/                      # Material 3 主题与排版
```

测试目录：

```text
app/src/test/                       # JVM 单元测试
app/src/androidTest/                # 设备或模拟器测试
```

## 核心调用链

### UI 状态

```text
WhitelistMakerApp
  -> AppContainer
  -> WhitelistRepository
  -> combine(StateFlow...)
  -> AppViewModel.uiState
  -> collectAsStateWithLifecycle()
  -> AppScreen / Route
```

`AppUiState` 是当前页面共享状态。业务状态应由 Repository 或 Store 持有，不要把可持久化状态只放进 Composable。

### 安装监听

```text
MainActivity / BootReceiver
  -> ServiceController / startForegroundService
  -> InstallMonitorService
  -> dynamic ACTION_PACKAGE_ADDED receiver
  -> WhitelistRepository.recordInstalledPackage()
  -> InstallEventStore
  -> NotificationHelper
```

通知按钮通过 `InstallEventActionReceiver.goAsync()` 调用同一个 Repository，避免出现 UI 与通知两套业务实现。

### 白名单读写

```text
MainActivity SAF launcher / clipboard
  -> AppViewModel
  -> WhitelistRepository
  -> WhitelistFileStore
  -> WhitelistFormatParser
```

未导入文件时写入应用私有目录的 `default_whitelist.txt`；导入后通过持久化 URI 权限继续读写用户选择的文档。

## 代码风格

### Kotlin

- 遵循 `kotlin.code.style=official`。
- 使用 4 空格缩进；多行参数和调用保留尾随逗号。
- 类、枚举使用 `PascalCase`，函数和属性使用 `camelCase`，常量使用 `UPPER_SNAKE_CASE`。
- 能用不可变值时使用 `val`；模型优先使用 `data class`。
- 单一职责的小工具使用 `object`；Android 依赖通过构造参数传入。
- 对预期失败使用 `Result` 或明确返回值；不要用空 `catch` 隐藏业务错误。
- 仅在逻辑不直观或涉及 Android 平台限制时添加注释，不复述代码。

### 架构与状态

- 延续当前手写 `AppContainer` 注入方式，不在局部功能中自行创建 Repository 或 Store。
- UI 事件先进入 `AppViewModel`，再委托给 Repository。
- Repository 负责编排，Store 负责具体持久化，纯格式逻辑放在可 JVM 测试的独立对象中。
- 新增状态优先暴露只读 `StateFlow` 或 `Flow`；内部可变流保持私有。
- 在 Compose 中使用 `collectAsStateWithLifecycle()` 收集生命周期相关状态。
- 一次性 UI 事件可参考删除撤销流程使用 `SharedFlow`，不要塞进长期状态后反复消费。

### 协程与 I/O

- 文件、JSON 和 ContentResolver I/O 放在 `Dispatchers.IO`。
- 页面发起的挂起操作使用 `viewModelScope`。
- Service 自有协程使用带 `SupervisorJob` 的 scope，并在 `onDestroy()` 中取消。
- `BroadcastReceiver` 中的异步工作必须配合 `goAsync()`，并确保 `PendingResult.finish()` 在 `finally` 中执行。
- 不使用 `GlobalScope`，也不要创建无法取消的长期 scope。

### Compose UI

- Route 接收 `AppUiState`、`AppActions` 和 `PaddingValues`，保持状态提升和单向事件流。
- 页面专属 Composable 默认放在对应 Route 文件中并设为 `private`；跨页面复用后再移动到 `ui/components/`，设为 `internal`。
- 新增或明显修改页面时同步维护 `@Preview`；复用 `PreviewDefaults`，避免 Preview 依赖真实 Android 状态。
- 优先使用 `MaterialTheme` 的颜色和排版。仅在明确的品牌或交互场景使用硬编码颜色。
- 长列表使用 `LazyColumn`，稳定数据提供 `key`；昂贵派生结果使用带正确 key 的 `remember`。
- 保留 edge-to-edge 下 `innerPadding` 的处理，避免内容被系统栏或底部导航遮挡。
- 当前界面文案以简体中文为主，且多数直接位于 Compose。小改动遵循所在文件风格；涉及复用或本地化时再迁移到资源，不要顺手批量改写现有文案。

## 数据约束

- `WhitelistFormatParser` 是白名单格式的唯一解析和生成入口。
- 包名正则当前要求至少包含一个点号；修改规则时必须补充解析器单元测试。
- 白名单追加操作不得产生重复包名。
- `InstallEventStore` 将事件按 `installedAt` 降序保存到内存状态。
- 同一包名的新待处理事件会替换旧的待处理事件，但不会删除已处理历史。
- 读取旧安装事件时，缺失 `appLabel` 应回退到 `packageName`。
- 导入 URI、显示名称、格式和自动启动设置由 `AppPreferences` 保存。
- `serviceStatus` 是运行时状态，应用重新启动时默认为 `Stopped`，不要把旧运行状态当成服务真实状态。

修改持久化字段、JSON 结构、Preference key 或白名单格式时，应考虑已有安装数据的兼容读取，并增加对应测试。

## Android 平台注意事项

- `QUERY_ALL_PACKAGES` 是高敏感权限，Manifest 已注明其商店审核风险。不要在不了解安装检测需求时删除或扩大其用途。
- 前台服务声明为 `specialUse`，修改 service 类型时必须同步检查 Manifest、启动方式和目标 Android 版本限制。
- Android 13 及以上启动监听前需要处理 `POST_NOTIFICATIONS`。
- 动态安装广播在 Android 13 及以上使用 `RECEIVER_EXPORTED`；调整导出状态前先验证系统广播是否仍能收到。
- `BootReceiver` 仅在用户启用自动启动后恢复服务。
- 通知 small icon 使用 `ic_launcher_monochrome`。Android 通知图标要求透明单色资源，不要直接替换成普通彩色启动图标。
- 修改通知 action、Intent extra 或 request code 时，要同步检查创建方和 `InstallEventActionReceiver`。
- SAF 导入依赖 `takePersistableUriPermission()`。修改文件选择流程时必须验证重启后 URI 仍可访问。

## 测试规范

当前有效的业务测试主要是 `WhitelistFormatParserTest`；模板生成的 `ExampleUnitTest` 和 `ExampleInstrumentedTest` 只提供基础占位覆盖，不能作为业务正确性的证据。

按改动风险选择测试：

- 纯 Kotlin 解析、转换、去重和状态决策：优先添加 JVM 单元测试。
- Repository、Store 或 Android framework 交互：优先拆出可测试纯逻辑；确实依赖 Context、ContentResolver 或生命周期时使用设备测试。
- Compose 交互或可访问性变化：补 Compose/设备测试，或至少提供可稳定复现的 Preview 与人工验证步骤。
- Service、Receiver、通知、权限、开机启动和 SAF 持久权限：除自动化测试外，需要在模拟器或真机验证真实平台流程。
- 修复缺陷时，测试应覆盖原始失败场景，而不只覆盖正常路径。

常用验证命令：

```bash
# JVM 单元测试
rtk ./gradlew testDebugUnitTest

# Android Lint
rtk ./gradlew lintDebug

# Debug 构建
rtk ./gradlew assembleDebug

# 需要已连接设备或已启动模拟器
rtk ./gradlew connectedDebugAndroidTest
```

若 `rtk` 不在 PATH：

```bash
/opt/homebrew/bin/rtk ./gradlew testDebugUnitTest
```

文档或极小配置改动可做轻量验证；业务逻辑改动至少运行相关测试，跨层或 Android 组件改动再补 lint、构建或设备验证。不要声称未实际执行的命令通过。

## Agent 工作流程

1. 先运行 `rtk git status --short --branch`，确认工作树中是否已有用户改动。
2. 阅读与任务直接相关的测试，再沿真实调用链读取实现。
3. 明确计划、改动文件和不触碰的边界。
4. 采用最小实现；不要覆盖或回退用户已有改动。
5. 只格式化和检查本次触碰的文件，避免无关 diff。
6. 运行能证明本次改动正确的 focused verification。
7. 最终说明改了什么、验证了什么，以及未能验证的部分。

## 常见任务入口

- 改白名单格式：`domain/Models.kt` -> `data/WhitelistFormatParser.kt` -> `WhitelistFormatParserTest.kt` -> `WhitelistFileStore.kt`
- 改安装事件行为：`InstallMonitorService.kt` -> `WhitelistRepository.kt` -> `InstallEventStore.kt`
- 改通知操作：`NotificationHelper.kt` -> `InstallEventActionReceiver.kt` -> `WhitelistRepository.kt`
- 改服务启动逻辑：`MainActivity.kt` / `BootReceiver.kt` -> `ServiceController.kt` -> `InstallMonitorService.kt`
- 改页面交互：对应 Route -> `AppActions.kt` -> `AppViewModel.kt` -> `WhitelistRepository.kt`
- 改共享 UI：`ui/components/`，并检查所有调用页面和 Preview
- 改持久化：先确认旧文件、旧 Preference 或旧 URI 的兼容行为，再修改 Store
