package com.herbcraft.alchemy.knowledge;

import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.knowledge.CodexLoot;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class AlchemyCodexHooks {
   private static final Map<ResourceKey<LootTable>, Float> ALCHEMY_TABLES = Map.ofEntries(Map.entry(BuiltInLootTables.VILLAGE_TEMPLE, 0.18F));
   private static final ResourceKey<LootTable> WITCH_LOOT = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("minecraft", "entities/witch"));
   private AlchemyCodexHooks() {}
   public static void register() {
      LootTableEvents.MODIFY.register((Modify)(key, tableBuilder, source, registries) -> {
         Float chance = ALCHEMY_TABLES.get(key);
         if (chance != null && source.isBuiltin()) CodexLoot.addTatteredPagePool(tableBuilder, chance, "alchemy", "");
         if (source.isBuiltin() && key.equals(WITCH_LOOT)) addWitchLoot(tableBuilder);
      });
   }
   private static void addWitchLoot(LootTable.Builder tableBuilder) {
      CodexLoot.addTatteredPagePool(tableBuilder, 0.15F, "alchemy", "");
      tableBuilder.pool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.10F)).add(LootItem.lootTableItem(AlchemyRegistries.ESSENCE_POPPY)).add(LootItem.lootTableItem(AlchemyRegistries.ESSENCE_AZURE_BLUET)).add(LootItem.lootTableItem(AlchemyRegistries.ESSENCE_TULIP)).add(LootItem.lootTableItem(AlchemyRegistries.ESSENCE_AZALEA)).add(LootItem.lootTableItem(AlchemyRegistries.ESSENCE_PITCHER_PLANT)).build());
      tableBuilder.pool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.03F)).add(LootItem.lootTableItem(Items.POTION).apply(SetComponentsFunction.setComponent(DataComponents.POTION_CONTENTS, new PotionContents(AlchemyRegistries.WITCH_TULIP_MURKY)))).build());
   }
}
