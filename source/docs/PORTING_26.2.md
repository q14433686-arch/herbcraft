# Herbcraft 26.2 移植调查与记录

日期：2026-07-19 · 分支：`arena/019f793d-herbcraft` · 基线：仓库内 `herbcraft-source-1.0.0.zip`（26.1.2）

## 结论先行

**不是"仅改版本声明"，但改动量很小**：26.2 相比 26.1.2 对本项目只造成 **2 类、共 9 处代码修改（5 个文件）**，其余全部是版本/工具链声明。没有 Mixin 失效、没有数据包格式变动、没有 Fabric API 用法变动。

## 一、版本对齐表

| 依赖 | 旧（26.1.2 支持） | 新（26.2 支持） | 说明 |
|---|---|---|---|
| Minecraft | 26.1.2 | 26.2 | 官方博客描述为"比 26.1 小得多的更新"，主攻渲染与注册 |
| Fabric Loader | 0.19.3 | **0.19.3（不变）** | 官方博客确认 0.19.3 即 26.2 当前稳定版，即任务中说的 "fabric 0.193" |
| Fabric API | 0.151.0+26.1.2 | 0.155.2+26.2 | 26.2 首个可用版为 0.152.1+26.2（2026-06-15），最新 0.155.2+26.2（2026-07-17） |
| Fabric Loom | 1.16-SNAPSHOT | 1.17.16 | 26.2 需要 Loom 1.17（Vulkan/枚举扩展支持），取 1.17 最新稳定 |
| Gradle（Wrapper） | 9.4.1 | 9.5.1 | 官方博客指定配套版本 |
| Java | 25 | 25（不变） | Mixin `compatibilityLevel: JAVA_25` 不变 |
| Jade（可选兼容） | 26.1.0+fabric | 26.2.9+fabric | compileOnly/runtimeOnly，`suggests` 下限随之提到 26.2.0 |

`fabric.mod.json` 三件套：`minecraft` 由 `~26.1.2` 改为 `~26.2`，`fabric-api` 下限随编译版本提升（沿用旧工程"`>=` 编译版本"的惯例）。

## 二、逐条排查结果（对照官方 26.2 博客 + NeoForged 26.1.x→26.2 移植primer）

### 必须改的（2 类，9 处）

1. **Gui 重组**（`Minecraft` 上的屏幕字段移入 `Gui`）：
   - `core/.../client/HerbcraftCoreClient.java:12`：`context.client().setScreen(...)` → `context.client().gui.setScreen(...)`
   - `core/.../client/HerbNatureItemTooltips.java:27`：`Minecraft.getInstance().screen` → `Minecraft.getInstance().gui.screen()`
2. **拼写修正改名** `MobEffect#isInstantenous` → `isInstantaneous`：
   - alchemy 3 个文件共 7 处（`AdvancedPotionTooltips` ×2、`AdvancedPotionStackInitializer` ×2、`AlchemyRegistries` ×3）

### 排查后确认不用改的

| 26.2 变化 | 对本项目影响 |
|---|---|
| Vulkan 后端 / Blaze3D 重写（RenderPipeline、VertexFormat、FeatureRenderer 等） | 无 — 全仓零 `RenderSystem`/GL/`blaze3d` 调用；Codex 界面走的是 26.1 引入的 `GuiGraphicsExtractor`/`extractRenderState`，26.2 未删改（仅删了 `GuiGraphicsExtractor#sign`，未用） |
| `ChatFormatting` 瘦身（CODEC/getName/getColor 等访问器删除，队伍色改 `TeamColor`） | 无需改 — 我们 114 处用法全部是 `Component.withStyle(ChatFormatting.XXX)`，该重载不在删除清单内（primer 的 changes/removals 列表中 `net.minecraft.network.chat` 无任何条目）；不使用被删的访问器，不碰队伍系统。**首次实机构建后建议目检一次 tooltip 配色，属低风险项** |
| 进度谓词大重组（`advancements.criterion.*` → `triggers.*`/`predicates.*`） | 无 — 进度 JSON 全部使用 `minecraft:impossible` 由代码授予（`HerbcraftAdvancements`），不引用任何谓词类 |
| `EntityType`/`BlockEntityType` 常量移入 `EntityTypes`/`BlockEntityTypes`；`Items`/`Blocks` 色变体并入 `ColorCollection` | 无 — 全仓未引用被迁移的常量（无 `EntityType.X`、无染料/铜类 `Items` 常量） |
| 注册/数据生成改用 `BlockIds`/`ItemIds`；`valueLookupBuilder` 删除 | 无 — 不用 datagen |
| `BlockPos#getCenter`/`getBottomCenter` 删除；`InstantenousMobEffect` 改名；`Minecraft#isSingleplayer` 删除 | 无 — grep 确认零引用 |
| FAPI 0.150.1–0.155 新增（标签移除、流体交互、attended 命令、atlas registry 等） | 全部为新增 API，无破坏；我们用到的 12 个模块（item.v1、networking.v1、loot.v3、entity.event.v1、event.lifecycle.v1、event.player、creativetab.v1、registry、client.item.v1、client.networking.v1）在 0.155.2 中签名不变 |
| Mixin 目标（`Item#use`/`finishUsingItem`/`releaseUsing`、`LivingEntity#addEffect`、`Player#add/readAdditionalSaveData`/`die`、`BucketItem`、`SnowballItem`、`EggItem`、`Witch#performRangedAttack`、`BrewingStandBlockEntity#canPlaceItem/isBrewable/doBrew`、`BrewingStandMenu$IngredientsSlot#mayPlace`、`ItemCost#test`） | primer 全表核对：这些方法的签名在 26.2 均未列出变更（primer 声明"non-exhaustive"，首次构建时以编译器/mixin 校验最终确认） |
| 数据包（进度/配方/战利品注入/村民交易 JSON/标签） | 无格式变更；`tags/item` 兼 `tags/items` 双目录沿用现状 |

### 顺带说明

- 26.2 版本本体加入了硫磺方块/硫磺史莱姆等新内容，与本模组无冲突，也暂无计划纳入可食清单（留待策划定夺）。
- zip 源码内 `CHANGELOG.md` 记录的构建号（core 1.1.3 等）与 `build.gradle`/`fabric.mod.json` 声明的 `1.0.0` 不一致（`NEXT_TASK.md` 自己也提到源码/产物曾失同步）。本次移植**未改动模组版本号**，三模块仍为 `1.0.0`；发布时请由作者拍板版本号。
- 仓库历史布局是"根目录放交接 zip"，本分支新增 `source/` 目录作为当前工作源码树（排除 `.gradle/`、`eula.txt`），与文档中既有引用（如 `source/docs/...`）保持一致；旧的 `herbcraft-source-1.0.0.zip` 保留作历史快照。

## 三、构建验证清单（需联网环境，本沙箱无法下载 Gradle/Maven 依赖）

1. `./gradlew build` —— 预期一次通过；若有遗漏符号错误，大概率也集中在客户端渲染包。
2. 实机冒烟：
   - 打开百草经（右键 `herbal_codex`）——验证 `gui.setScreen` 路径与界面渲染；
   - 背包内悬浮草药 tooltip（'??'门禁生效，需屏幕存在）——验证 `gui.screen()`；
   - 喝下任一非瞬时精华药水，查看 tooltip 时长行——验证 `isInstantaneous` 路径；
   - 彩蛋：鸡蛋/雪球长按吃、短按扔；细雪桶对空气饮用；女巫 10% 替换投掷。
3. 目检 tooltip 配色（`ChatFormatting` 低风险项，见上表）。

## 参考来源

- Fabric 官方博客《Fabric for Minecraft 26.2》（2026-06-15）：Loader 0.19.3、Loom 1.17、Gradle 9.5.1、Gui 重组示例
- NeoForged《26.1.x → 26.2 Mod Migration Primer》（ChampionAsh5357）：逐类变更清单
- Fabric Maven `fabric-api` / `fabric-loom` maven-metadata：0.155.2+26.2、1.17.16
- Modrinth Maven `jade` maven-metadata：26.2.9+fabric
