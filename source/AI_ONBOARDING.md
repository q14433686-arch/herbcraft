# AI_ONBOARDING

This is the mandatory entry document for the next AI agent.

## Absolute first step

Before editing code, you must:

1. Read these files:
   - `README.md`
   - `AI_ONBOARDING.md`
   - `PROJECT_STATE.md`
   - `NEXT_TASK.md`
   - `CHANGE_IMPACT.md`
   - `BUGS_TODO.md`
   - `docs/PROJECT_SUMMARY.md` if present
2. Inspect the actual source tree and resources. Do not trust chat history or release jars blindly.
3. Produce a **接手状态报告** using the template below.
4. Wait for the author to confirm before coding, unless the author explicitly says to proceed immediately.

## Locked environment

- Minecraft `26.2`
- Fabric Loader `0.19.3`
- Fabric API `0.155.2+26.2`
- Java `25`
- Gradle Wrapper `9.5.1`
- Fabric Loom `1.17.16` (26.2 requires Loom 1.17)
- Mojang official mappings

## Three design philosophies

1. **Stable IDs are sacred.** Do not rename existing IDs casually.
2. **Information is earned.** Tooltips, Jade/REI integrations, Codex pages, and recipe hints must respect unknown/heard/mastered knowledge tiers.
3. **Modules must remain independent.** Core must not depend on alchemy or cuisine; addons depend on core only.

## Absolute architecture rules

- No old `TradeOfferHelper`; use 26.1 data-driven villager trade JSON and tags.
- No full vanilla loot table replacement; use Fabric loot events and `source.isBuiltin()`.
- No hard dependency on Jade/REI unless an optional compat setup is intentionally added.
- Do not use deprecated `appendHoverText`; use current tooltip APIs such as Fabric item tooltip callbacks.
- Do not write important balance constants as random scattered Java literals when they belong in JSON/config.
- For brewing, remember: vanilla/Fabric `addMix` can reset output stack components. If preserving components, use a custom path and copy the input stack.

## Current high-risk area

Alchemy brewing catalyst code is the highest-risk current area. At the time of this handoff, `alchemy/src/main/resources/herbcraft_alchemy.mixins.json` references `BrewingStandCatalystMixin`, but the source tree must be checked to ensure the class exists and builds. If this class is missing, game startup will crash during Mixin prepare.

## Required 接手状态报告 format

```md
# 接手状态报告

## 1. 我已阅读的文件
- README.md
- AI_ONBOARDING.md
- PROJECT_STATE.md
- NEXT_TASK.md
- CHANGE_IMPACT.md
- BUGS_TODO.md
- docs/PROJECT_SUMMARY.md
- 其他：...

## 2. 当前环境确认
- Minecraft:
- Fabric Loader:
- Fabric API:
- Java:
- Gradle/Loom:

## 3. 当前源码真实状态
### core
- ...
### alchemy
- ...
### cuisine
- ...

## 4. 我发现的风险点 / 明显不一致
- ...

## 5. 我建议下一步做什么
- ...

## 6. 开始编码前需要作者确认的问题
- ...
```

## Minimum validation commands before releasing

```bash
./gradlew build --rerun-tasks
python3 scripts/check_lang_diff.py
python3 scripts/validate_recipe_isolation.py
python3 scripts/validate_item_textures.py
python3 scripts/validate_task_abcd.py
python3 scripts/validate_animal_food_tags.py
python3 scripts/validate_extended_features.py
python3 scripts/verify_built_jars.py
```

Do not claim success if these are not run or if they fail.
