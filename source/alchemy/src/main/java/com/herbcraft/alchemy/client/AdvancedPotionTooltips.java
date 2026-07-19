package com.herbcraft.alchemy.client;

import com.herbcraft.alchemy.potion.AdvancedPotionStackInitializer;
import com.herbcraft.alchemy.potion.AlchemyPotionComponents;
import com.herbcraft.alchemy.potion.PotionBonusComponent;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public final class AdvancedPotionTooltips {
   private AdvancedPotionTooltips() {
   }

   public static void register() {
      ItemTooltipCallback.EVENT.register(AdvancedPotionTooltips::appendTooltip);
   }

   private static void appendTooltip(ItemStack stack, TooltipContext context, TooltipFlag flag, List<Component> lines) {
      AlchemyPotionComponents.PotionQuality quality = (AlchemyPotionComponents.PotionQuality)stack.get(AlchemyPotionComponents.POTION_QUALITY);
      if (quality != null || AdvancedPotionStackInitializer.isAdvanced(stack)) {
         String path = AdvancedPotionStackInitializer.advancedPotionPath(stack).orElse("advanced");
         String typeKey = AdvancedPotionStackInitializer.highTierTypeKey(path);
         lines.add(
            Component.translatable("tooltip.herbcraft.potion.type", new Object[]{Component.translatable("tooltip.herbcraft.potion.type." + typeKey)})
               .withStyle(typeStyle(typeKey))
         );
         if (quality != null) {
            String multiplier = String.format(Locale.ROOT, "%.1f", quality.durationMultiplier());
            lines.add(
               Component.translatable(
                     "tooltip.herbcraft.potion.quality",
                     new Object[]{Component.translatable("tooltip.herbcraft.potion.quality." + quality.getSerializedName()), multiplier}
                  )
                  .withStyle(qualityStyle(quality))
            );
         } else {
            lines.add(Component.translatable("tooltip.herbcraft.potion.uninitialized").withStyle(ChatFormatting.GRAY));
         }

         PotionBonusComponent bonus = (PotionBonusComponent)stack.get(AlchemyPotionComponents.POTION_BONUS);
         if (bonus != null) {
            BuiltInRegistries.MOB_EFFECT
               .get(bonus.effectId())
               .ifPresent(
                  effect -> lines.add(
                     Component.translatable("tooltip.herbcraft.potion.bonus", new Object[]{effectLine(effect, bonus, quality)})
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                  )
               );
         }
      }
   }

   private static Component effectLine(Holder<MobEffect> effect, PotionBonusComponent bonus, AlchemyPotionComponents.PotionQuality quality) {
      Component name = Component.translatable(((MobEffect)effect.value()).getDescriptionId());
      if (bonus.amplifier() > 0) {
         name = Component.translatable("potion.withAmplifier", new Object[]{name, Component.translatable("potion.potency." + bonus.amplifier())});
      }

      int durationTicks = bonus.durationTicks();
      if (quality != null && !((MobEffect)effect.value()).isInstantaneous()) {
         durationTicks = Math.min(9600, Math.max(1, Math.round(durationTicks * quality.durationMultiplier())));
      }

      if (!((MobEffect)effect.value()).isInstantaneous()) {
         name = Component.translatable("potion.withDuration", new Object[]{name, formatTicks(durationTicks)});
      }

      return name;
   }

   private static String formatTicks(int ticks) {
      int seconds = Math.max(0, ticks / 20);
      return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
   }

   private static ChatFormatting typeStyle(String typeKey) {
      return !"murky".equals(typeKey) && !"t3".equals(typeKey) ? ChatFormatting.AQUA : ChatFormatting.DARK_RED;
   }

   private static ChatFormatting qualityStyle(AlchemyPotionComponents.PotionQuality quality) {
      return switch (quality) {
         case STANDARD -> ChatFormatting.GRAY;
         case FINE -> ChatFormatting.BLUE;
         case EXCEPTIONAL -> ChatFormatting.GOLD;
      };
   }
}
