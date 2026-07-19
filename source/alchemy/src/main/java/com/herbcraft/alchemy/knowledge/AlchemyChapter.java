package com.herbcraft.alchemy.knowledge;

import com.herbcraft.alchemy.coating.CoatingSystem;
import com.herbcraft.alchemy.coating.WeaponCoating;
import com.herbcraft.alchemy.registry.AlchemyRegistries;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeAPI.Entry;
import com.herbcraft.api.KnowledgeAPI.State;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

public final class AlchemyChapter {
   public static final String CHAPTER_ID = "alchemy";

   private AlchemyChapter() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter("alchemy", Component.translatable("book.herbcraft_alchemy.chapter"), 200);
      KnowledgeAPI.registerSection("alchemy", "essences", Component.translatable("book.herbcraft_alchemy.section.essences"));
      KnowledgeAPI.registerSection("alchemy", "potions", Component.translatable("book.herbcraft_alchemy.section.potions"));
      KnowledgeAPI.registerSection("alchemy", "coatings", Component.translatable("book.herbcraft_alchemy.section.coatings"));

      for (Item item : AlchemyRegistries.essenceItems()) {
         AlchemyRegistries.essenceInfo(item)
            .ifPresent(
               info -> {
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "essence_" + info.id(),
                        "essences",
                        Component.translatable(item.getDescriptionId()),
                        (player, state) -> essenceLines(info, state),
                        player -> player.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0,
                        null
                     )
                  );
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "potion_" + info.id(),
                        "potions",
                        Component.translatable("item.minecraft.potion.effect." + info.id()),
                        (player, state) -> essenceLines(info, state),
                        null,
                        null
                     )
                  );
                  KnowledgeAPI.registerEntry(
                     "alchemy",
                     new Entry(
                        "coating_" + info.id(),
                        "coatings",
                        Component.translatable("book.herbcraft_alchemy.coating_name", new Object[]{Component.translatable(item.getDescriptionId())}),
                        (player, state) -> coatingLines(info, state),
                        null,
                        null
                     )
                  );
               }
            );
      }
   }

   private static List<Component> essenceLines(AlchemyRegistries.EssenceInfo info, State state) {
      List<Component> lines = new ArrayList<>();
      if (state == State.HEARD) {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         return lines;
      } else {
         for (AlchemyRegistries.EssenceEffect effect : info.effects()) {
            lines.add(effectLine(((MobEffect)effect.effect().value()).getDescriptionId(), effect.amplifier(), effect.durationSeconds(), effect.positive()));
         }

         return lines;
      }
   }

   private static List<Component> coatingLines(AlchemyRegistries.EssenceInfo info, State state) {
      List<Component> lines = new ArrayList<>();
      if (state == State.HEARD) {
         lines.add(Component.translatable("book.herbcraft_alchemy.entry.heard").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         return lines;
      } else {
         List<AlchemyRegistries.EssenceEffect> effects = CoatingSystem.effectsFor(info, WeaponCoating.Mode.FULL);
         lines.add(
            Component.translatable("book.herbcraft_alchemy.coating_uses", new Object[]{CoatingSystem.usesFor(info, WeaponCoating.Mode.FULL)})
               .withStyle(ChatFormatting.GRAY)
         );

         for (AlchemyRegistries.EssenceEffect effect : effects) {
            lines.add(
               effectLine(
                  ((MobEffect)effect.effect().value()).getDescriptionId(), effect.amplifier(), CoatingSystem.hitDurationTicks(effect) / 20, effect.positive()
               )
            );
         }

         return lines;
      }
   }

   private static Component effectLine(String descriptionId, int amplifier, int seconds, boolean positive) {
      MutableComponent name = Component.translatable(descriptionId);
      if (amplifier > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + amplifier)});
      }

      return Component.translatable("potion.withDuration", new Object[]{name, String.format("%d:%02d", seconds / 60, seconds % 60)})
         .withStyle(positive ? ChatFormatting.BLUE : ChatFormatting.RED);
   }
}
