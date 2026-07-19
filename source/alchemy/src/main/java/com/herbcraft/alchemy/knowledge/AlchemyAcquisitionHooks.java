package com.herbcraft.alchemy.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.alchemy.coating.CoatingComponents;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.KnowledgePracticeRegistry;
import com.herbcraft.api.KnowledgeAPI.State;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AlchemyAcquisitionHooks {
   private AlchemyAcquisitionHooks() {
   }

   public static void register() {
      AlchemyRegistries.essenceItems().forEach(item -> AlchemyRegistries.essenceInfo(item).ifPresent(info -> KnowledgePracticeRegistry.registerRoute("herbal/" + info.id(), "alchemy:essence")));
      KnowledgeInventoryScanner.registerHandler(AlchemyAcquisitionHooks::onStack);
   }

   private static void onStack(ServerPlayer player, ItemStack stack) {
      Optional<AlchemyRegistries.EssenceInfo> essence = AlchemyRegistries.essenceInfo(stack.getItem());
      if (essence.isPresent()) {
         String id = essence.get().id();
         HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_essence");
         boolean crafted = player.getStats().getValue(Stats.ITEM_CRAFTED.get(stack.getItem())) > 0;
         if (crafted) {
            KnowledgeManager.markPracticed(player, "alchemy", "essence_" + id, true);
            KnowledgeManager.markPracticed(player, "herbal", id, false);
         } else {
            // TODO: a future trade-specific hook can distinguish bought essences from other acquisition paths.
            KnowledgeManager.markHeard(player, "alchemy", "essence_" + id, true);
            KnowledgeManager.markHeard(player, "herbal", id, false);
         }
         KnowledgeManager.markHeard(player, "alchemy", "potion_" + id, false);
      } else {
         PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
         if (contents != null) {
            contents.potion().flatMap(holder -> holder.unwrapKey()).ifPresent(key -> {
               if ("herbcraft".equals(key.identifier().getNamespace())) {
                  String potionName = key.identifier().getPath();
                  awardPotionAdvancements(player, potionName, stack);
                  String base = stripModifierSuffix(potionName);
                  KnowledgeManager.markHeard(player, "alchemy", "potion_" + base, true);
                  KnowledgeManager.markHeard(player, "herbal", base, false);
               }
            });
         }

         WeaponCoating coating = (WeaponCoating)stack.get(CoatingComponents.WEAPON_COATING);
         if (coating != null) {
            HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_coating");
            KnowledgeManager.unlock(player, "alchemy", "coating_" + coating.essenceId(), State.MASTERED);
         }
      }
   }

   private static void awardPotionAdvancements(ServerPlayer player, String potionName, ItemStack stack) {
      HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_potion");
      if (potionName.endsWith("_clarified")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_clarified");
      if (potionName.endsWith("_murky") || potionName.equals("undead_venom") || potionName.equals("cardiac_toxin") || potionName.equals("wither_venom_iii")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "first_murky");
      if (potionName.equals("undead_venom")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "undead_toxin");
      if (potionName.startsWith("witch_")) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "witch_knowledge");
      AlchemyPotionComponents.PotionQuality quality = (AlchemyPotionComponents.PotionQuality)stack.get(AlchemyPotionComponents.POTION_QUALITY);
      if (quality == AlchemyPotionComponents.PotionQuality.EXCEPTIONAL) HerbcraftAdvancements.grant(player, "herbcraft_alchemy", "epic_potion");
   }

   private static String stripModifierSuffix(String potionName) {
      for (String suffix : new String[]{"_long", "_strong", "_clarified", "_murky"}) {
         if (potionName.endsWith(suffix)) {
            return potionName.substring(0, potionName.length() - suffix.length());
         }
      }

      return potionName;
   }
}
