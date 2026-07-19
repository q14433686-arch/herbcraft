package com.herbcraft.cuisine.item;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.cuisine.registry.CuisineComponents;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.logic.HerbStackManager;
import com.herbcraft.logic.LastMealTracker;
import com.herbcraft.logic.SpecialCounters;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import com.herbcraft.registry.HerbEntry.Effect;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class HodgepodgeItem extends Item {
   private static final float CHANCE_BONUS = 0.15F;
   private static final float CLASH_CHANCE = 0.3F;
   private static final int CLASH_NAUSEA_SECONDS = 10;
   private static final int MAX_STACK_GAIN = 3;

   public HodgepodgeItem(Properties properties) {
      super(properties);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (!level.isClientSide() && entity instanceof Player player) {
         resolve(stack, level, player);
         if (player instanceof ServerPlayer serverPlayer) {
            HerbcraftAdvancements.grant(serverPlayer, "herbcraft_cuisine", "first_hodgepodge");
            LastMealTracker.record(serverPlayer, this);
         }
      }

      return super.finishUsingItem(stack, level, entity);
   }

   private static void resolve(ItemStack stack, Level level, Player player) {
      List<String> contents = (List<String>)stack.get(CuisineComponents.HODGEPODGE_CONTENTS);
      if (contents != null && !contents.isEmpty()) {
         long gameTime = level.getGameTime();
         boolean hasPositive = false;
         boolean hasPureNegative = false;
         int stackGain = 0;
         int herbStack = 0;
         int toxicCount = 0;
         int clearingCount = 0;
         int blandCount = 0;
         int floralCount = 0;

         for (String id : contents) {
            Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
            NatureProfile nature = HerbNatureAPI.getNatureOrFallback(new ItemStack(item));
            if (nature.traits().contains("toxic")) toxicCount++;
            if (nature.traits().contains("clearing")) clearingCount++;
            if (nature.traits().contains("floral")) floralCount++;
            if (nature.flavors().contains("bland")) blandCount++;
         }

         float clashChance = Math.max(0.0F, Math.min(0.8F, CLASH_CHANCE + toxicCount * 0.10F - clearingCount * 0.15F - blandCount * 0.10F));
         int clashNauseaTicks = Math.round(CLASH_NAUSEA_SECONDS * 20 * (toxicCount > 0 && clearingCount == 0 ? 1.5F : 1.0F));

         for (String id : contents) {
            Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
            HerbEntry herb = HerbFoodRegistry.get(item);
            if (herb != null) {
               boolean anyPositive = herb.effects.stream().anyMatch(Effect::positive);
               boolean anyEffect = !herb.effects.isEmpty();
               hasPositive |= anyPositive;
               hasPureNegative |= anyEffect && !anyPositive;
               herb.removeEffects.forEach(player::removeEffect);
               if (herb.clearPositiveEffects) {
                  List<Holder<MobEffect>> positiveEffects = player.getActiveEffects()
                     .stream()
                     .filter(effect -> ((MobEffect)effect.getEffect().value()).isBeneficial())
                     .<Holder<MobEffect>>map(MobEffectInstance::getEffect)
                     .toList();
                  positiveEffects.forEach(player::removeEffect);
               }

               if (herb.extinguish) {
                  player.setRemainingFireTicks(0);
               }

               herb.effects.forEach(effect -> {
                  float chance = Math.min(effect.chance() + 0.15F, 1.0F);
                  if (EffectResolver.roll(player, chance)) {
                     player.addEffect(new MobEffectInstance(effect.effect(), effect.durationTicks(), effect.amplifier()));
                  }
               });
               if (level instanceof ServerLevel serverLevel) {
                  herb.damageEvents.forEach(event -> {
                     if (EffectResolver.roll(player, event.chance())) {
                        player.hurtServer(serverLevel, level.damageSources().magic(), event.amount());
                     }
                  });
               }

               herb.healEvents.forEach(event -> {
                  if (EffectResolver.roll(player, event.chance())) {
                     player.heal(event.amount());
                  }
               });
               if (herb.stackGroup && stackGain < 3) {
                  stackGain++;
                  herbStack = HerbStackManager.updateAndGet(player, gameTime, true);
               }

               SpecialCounters.apply(player, item, gameTime);
            }
         }

         if (floralCount >= 2) {
            herbStack = HerbStackManager.updateAndGet(player, gameTime, true);
            stackGain++;
         }

         if (hasPositive && hasPureNegative && EffectResolver.roll(player, clashChance)) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, clashNauseaTicks, 0));
         }

         if (stackGain > 0) {
            HerbStackManager.overloadIfNeeded(player, herbStack);
         }
      }
   }
}
