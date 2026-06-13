# Herbcraft / 百草可食 1.0.0 模组发布页介绍

> 可用于 Modrinth / CurseForge / GitHub Release 的发布页正文。  
> 建议发布页顶部先放“短简介”，再放“完整介绍”。

---

## 短简介

**百草可食 Herbcraft** 是一个围绕“可食自然物、草药认知、药性蓄积、七情配伍、炼金与药膳”展开的 Fabric 模组。它让大量原版物品可以入口，但不会立刻告诉你全部答案；你需要见过、听闻、尝过、辨伪、实践，才能真正识得其性。

---

## 一句话版本

让花草、树叶、冰雪、菌类与奇物皆可入口，并在百草经、药理学、炼金和药膳中慢慢认识它们。

---

# 百草可食 Herbcraft

这个 Mod 的起点很简单：

> 为什么 Minecraft 里的甘蔗不能吃？

从这个小问题开始，事情慢慢变得不那么简单。花能不能吃？树叶呢？雪球、冰、树脂、蜜脾、下界菌、荧石粉呢？如果它们都能入口，那它们只是补一点饥饿值，还是应该有自己的性味、传闻、风险和用途？

**百草可食 Herbcraft** 就是在这个想法上长出来的 Mod。它不是单纯的“吃东西加 Buff”，而是一套慢热的草药认知与药理互动系统。

你可以吃很多原版不能吃的东西，但你不一定知道它们会带来什么。你需要亲自尝试，也需要读残页、做标记、炼药、做菜，最后才可能真正掌握一种物品的性味与用途。

---

## 主要特色

### 大量原版物品变得可以食用

Herbcraft 让许多原版自然物与奇物可以入口，例如：

- 花、草、蕨、树叶、树苗、种子
- 海草、海带、海泡菜
- 雪球、冰、蓝冰、细雪桶
- 树脂、蜜脾、蜂蜜、黏液球
- 下界疣、下界菌、烈焰粉、岩浆膏等炼药材料
- 南瓜、西瓜、鸡蛋，以及更多自然材料

有些东西温和，有些危险，有些只是口感奇怪，有些则具有真正的药理意义。

---

### 生吃是赌博

直接吃草药并不总是稳定的选择。

某些物品可能带来恢复、速度、抗性、夜视、清除负面状态等效果；也可能造成中毒、反胃、饥饿、虚弱，甚至因为药性冲突而反噬。

你可以通过生吃快速获得经验，但也要承担风险。

---

### 药性蓄积系统

连续食用草药会让药性在体内蓄积。

随着层数上升，你可能跨过不同阶段，药效会逐渐增强；但如果吃得太多、太杂、太急，最终可能触发药性过载。

药性蓄积不是单纯奖励，它更像身体对草药的承受极限。

---

### 药理学：寒热、五味、七情

Herbcraft 加入了一套简化的药理系统：

- **寒热相济**：寒、凉、平、温、热会影响体内平衡；
- **五味调和**：甘、苦、辛、酸、涩、咸、淡分别影响不同机制；
- **七情配伍**：短时间内连续食用不同草药，可能相须、相使、相畏、相杀、相恶、相反。

这些不是单纯的提示语。它们会影响效果概率、持续时间、强度、负面效果，甚至让本次药效完全失效。

当前版本已经区分：

- `flavors`：药理五味，参与机制计算；
- `taste_flavors`：口感味道，只用于玩家尝过后的感性描述。

也就是说，**吃起来苦** 不再必然等于 **药理上有苦味效果**。

---

## 百草经：知识不是免费给你的

Herbcraft 的核心之一是“认识”。

百草经不会一开始告诉你所有物品的完整数据。每个条目都有认知状态：

```text
未知 → 见过 → 听闻 / 尝过 → 谙熟
```

### 未知

你还不了解它。

### 见过

你曾见过此物，但尚未识得其性。此时不会显示真实性味、特质或用途。

### 听闻

你通过残页或传闻知道了一些信息。但传闻未必全是真的。

### 尝过

你亲自吃过它，能知道口感、体感，以及自己实际触发过的效果。

### 谙熟

你经过足够实践，真正掌握了它。完整数据、用途和被证伪的传闻才会逐步显现。

---

## 残页、疑载与辨伪

世界中可以找到残页。残页会揭示某些条目的传闻。

传闻可能是：

- **据载**：较可信的记载；
- **疑载**：似是而非的信息。

你可以在百草经中给每条传闻做自己的判断：

- 我认为它是真的；
- 我认为它是假的；
- 我还不确定。

之后通过实践、勘误和谙熟，你会知道自己当初有没有判断正确。

这让“知识”本身也成为玩法的一部分。

---

## 炼金分册：喝药是契约

安装 **Herbcraft: Alchemy** 后，你可以将草药萃取成精华，并进一步酿造成药水。

炼金系统包含：

- 草药精华
- 草药药水
- 澄清药水：偏向稳定正向效果
- 浊化药水：偏向危险或武器化负面效果
- 药水品质系统
- 附加药性组件
- 武器淬层

炼金比生吃更稳定，但需要材料、流程和理解。

---

## 膳房分册：做菜是手艺

安装 **Herbcraft: Cuisine** 后，你可以制作药膳与百草盅。

膳房系统包含：

- 水碗与烹饪流程
- 固定药膳配方
- 清除负面状态与短暂免疫
- 百草盅：随机混合草药，可能调和，也可能相冲
- 百花宴、剧毒蛊等隐藏料理

药膳不是牛奶的替代品。牛奶是事后清除，药膳更像是提前理解风险、处理风险。

---

## 三个模块

| 模块 | 文件 | 是否必需 | 内容 |
|---|---|---|---|
| Core | `herbcraft-1.0.0.jar` | 必需 | 可食物、生吃、药理、百草经、知识系统、Jade/tooltip 信息门禁 |
| Alchemy | `herbcraft-alchemy-1.0.0.jar` | 可选 | 精华、药水、品质、澄清/浊化、淬层 |
| Cuisine | `herbcraft-cuisine-1.0.0.jar` | 可选 | 水碗、药膳、百草盅、隐藏料理 |

Alchemy 和 Cuisine 都依赖 Core，但二者彼此独立，可以只安装其中一个。

---

## 适合谁？

这个 Mod 适合：

- 喜欢探索和试错的玩家；
- 喜欢图鉴、草药、炼金、药膳系统的玩家；
- 喜欢慢慢解锁知识，而不是一开始看完所有数据的玩家；
- 喜欢有一点中医文化气质和模拟感的玩法。

它不太适合：

- 只想快速获得强力 Buff 的玩家；
- 不喜欢概率、风险和信息隐藏的玩家；
- 不想阅读百草经或研究系统的玩家。

---

## 环境要求

- Minecraft Java Edition `26.1.2`
- Fabric Loader `0.19.3+`
- Fabric API `0.151.0+26.1.2`
- Java `25`

---

## 已知局限

Herbcraft 1.0.0 已经可以作为完整版本游玩，但仍有一些未来可改进方向：

- 百草经 UI 仍可继续打磨；
- 残页与传闻内容未来可继续扩充；
- 药理系统目前主要覆盖寒热、五味、七情；
- 升降浮沉、毒性与减毒等更复杂内容留给未来版本；
- 多人服务器建议先小范围测试；
- Jade / tooltip 显示可能受客户端配置影响，建议实际确认。

---

## 截图建议

如果要为发布页配图，建议至少准备以下画面：

1. **主视觉**：玩家手持百草经站在花草、树叶、冰雪和下界菌附近。
2. **见过态 tooltip**：显示“见过”，但性味和特质为问号。
3. **百草经界面**：展示左侧目录和右侧条目详情。
4. **残页与疑载**：展示带“据载 / 疑载”和真伪标记的 claim 行。
5. **七情配伍**：ActionBar 显示相须、相杀或相反提示。
6. **药性蓄积**：聊天框出现 herb_stack 阶段提示。
7. **炼金**：展示精华、澄清/浊化药水和品质 tooltip。
8. **膳房**：展示水碗、药膳、百草盅或隐藏料理。

---

# English Version

## Short Summary

**Herbcraft** makes many vanilla natural items edible and turns them into a slow herbal knowledge system involving taste, rumors, pharmacology, cuisine, and alchemy.

---

## Herbcraft

Herbcraft began with a simple question:

> Why can’t you eat sugar cane in Minecraft?

From there, it became a mod about edible plants, natural materials, odd ingredients, herbal knowledge, rumors, cooking, and alchemy.

This is not a simple “eat item, get buff” mod. Many things can be eaten, but you will not always know what they do at first. You learn by seeing, hearing rumors, tasting, marking claims, cooking, brewing, and practicing.

Knowledge is part of the gameplay.

---

## Features

### More edible vanilla items

Herbcraft makes many previously inedible vanilla items edible, including:

- flowers, grass, ferns, leaves, saplings, and seeds
- seagrass, kelp, and sea pickles
- snowballs, ice, blue ice, and powder snow buckets
- resin, honeycomb, honey blocks, and slimeballs
- Nether fungi, nether wart, blaze powder, magma cream, and brewing ingredients
- pumpkins, melons, eggs, and other natural oddities

Some are mild. Some are dangerous. Some only taste strange. Some have real medicinal meaning inside the mod’s system.

---

### Herb accumulation

Eating herbs repeatedly builds medicinal pressure in your body.

Crossing thresholds may strengthen effects, but eating too much can lead to overload.

Herb accumulation messages use private system messages, while immediate pharmacology reactions use the ActionBar.

---

### Pharmacology

Herbcraft uses a simplified herbal logic inspired by traditional Chinese medicine:

- temperature balance: cold, cool, neutral, warm, hot
- pharmacological flavors
- compatibility between recently eaten herbs

Compatibility may cause reinforcement, assistance, restraint, detoxification, antagonism, or severe incompatibility.

These are not just messages. They affect probability, duration, strength, negative effects, and sometimes cancel the current medicine entirely.

Herbcraft separates:

- `flavors`: mechanical medicinal flavors
- `taste_flavors`: sensory taste descriptions

So something that tastes bitter does not automatically count as pharmacological bitter.

---

## Herb Codex

The Herb Codex tracks what you know.

Knowledge progresses through states:

```text
Unknown → Seen → Heard / Tasted → Mastered
```

- **Seen** means you have encountered the item, but do not yet know its nature.
- **Heard** means you have read or heard claims about it.
- **Tasted** means you have personally eaten it.
- **Mastered** means you have practiced enough to understand it properly.

The Codex does not reveal everything immediately.

---

## Rumors and doubtful claims

Tattered pages can reveal claims about herbs and strange ingredients.

Some claims are reliable. Some are doubtful. You can mark each claim in the Codex as likely true, likely false, or uncertain.

Later, through errata and practice, you may discover whether your judgment was correct.

---

## Alchemy

With **Herbcraft: Alchemy**, herbs can be turned into essences and potions.

Features include:

- herbal essences
- base herbal potions
- clarified and murky potion lines
- potion quality and bonus components
- weapon coatings

Alchemy is more stable than raw eating, but it requires materials and understanding.

---

## Cuisine

With **Herbcraft: Cuisine**, herbs can be turned into medicinal dishes.

Features include:

- water bowls
- fixed medicinal meals
- cleansing and temporary immunity effects
- Herbal Hodgepodge
- hidden dishes such as Hundred-Flower Feast and Deadly Hodgepodge

Cuisine is not just milk with extra steps. It is preparation, not emergency cleanup.

---

## Who is this for?

Herbcraft is for players who enjoy:

- slow discovery
- uncertain information
- risk and experimentation
- Codex-style progression
- herbalism, alchemy, and cuisine systems

It may not be for players who only want quick, predictable buffs.

---

## Known limitations

- The Codex UI is functional, but future versions may improve layout and interaction.
- Rumor and errata content can continue to grow.
- Pharmacology is still in its first major stage.
- Large multiplayer testing is limited.
- Jade and tooltip behavior may vary depending on client configuration.
