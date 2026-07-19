package com.herbcraft.knowledge;

import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class CodexLoot {
   private static final Map<ResourceKey<LootTable>, Float> HERBAL_TABLES = Map.ofEntries(
      Map.entry(BuiltInLootTables.VILLAGE_WEAPONSMITH, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_TOOLSMITH, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_ARMORER, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_CARTOGRAPHER, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_MASON, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_SHEPHERD, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_BUTCHER, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_FLETCHER, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_FISHER, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_TANNERY, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_TEMPLE, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_DESERT_HOUSE, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_PLAINS_HOUSE, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_TAIGA_HOUSE, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_SNOWY_HOUSE, 0.35F),
      Map.entry(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, 0.35F),
      Map.entry(BuiltInLootTables.SHIPWRECK_MAP, 0.2F),
      Map.entry(BuiltInLootTables.SHIPWRECK_SUPPLY, 0.2F),
      Map.entry(BuiltInLootTables.SHIPWRECK_TREASURE, 0.2F),
      Map.entry(BuiltInLootTables.BURIED_TREASURE, 0.25F),
      Map.entry(BuiltInLootTables.UNDERWATER_RUIN_SMALL, 0.2F),
      Map.entry(BuiltInLootTables.UNDERWATER_RUIN_BIG, 0.2F),
      Map.entry(BuiltInLootTables.JUNGLE_TEMPLE, 0.25F),
      Map.entry(BuiltInLootTables.NETHER_BRIDGE, 0.12F),
      Map.entry(BuiltInLootTables.BASTION_TREASURE, 0.1F),
      Map.entry(BuiltInLootTables.BASTION_OTHER, 0.12F),
      Map.entry(BuiltInLootTables.BASTION_BRIDGE, 0.12F),
      Map.entry(BuiltInLootTables.BASTION_HOGLIN_STABLE, 0.12F),
      Map.entry(BuiltInLootTables.WOODLAND_MANSION, 0.05F)
   );

   private CodexLoot() {
   }

   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         Float chance = HERBAL_TABLES.get(key);
         if (chance != null && source.isBuiltin()) {
            addTatteredPagePool(tableBuilder, chance, "herbal", "");
         }
      });
   }

   public static void addTatteredPagePool(Builder tableBuilder, float chance, String chapter, String entry) {
      tableBuilder.pool(
         LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance(chance))
            .add(
               LootItem.lootTableItem(CodexItems.TATTERED_PAGE)
                  .apply(SetComponentsFunction.setComponent(CodexComponents.PAGE_ENTRY, new CodexComponents.PageEntry(chapter, entry)))
            )
            .build()
      );
   }
}
