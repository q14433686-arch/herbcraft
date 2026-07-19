package com.herbcraft.pharmacology;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.logic.HerbStackManager;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.registry.HerbEntry;
import com.herbcraft.registry.HerbFoodRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PharmacologyResolver {
   private PharmacologyResolver() {
   }

   public static PharmacologyResult resolve(ItemStack stack, Player player, HerbEntry entry, long gameTime) {
      PharmacologyRules rules = PharmacologyRules.current();
      PlayerPharmacologyState state = ((HerbcraftPlayerData)player).herbcraft$pharmacologyState();
      long nowMillis = System.currentTimeMillis();
      state.regressQi(gameTime, rules);
      state.cleanup(gameTime, nowMillis, rules.windowTicks);

      NatureProfile profile = HerbNatureAPI.getNatureOrFallback(stack);
      float probabilityMultiplier = 1.0F;
      float probabilityBonus = 0.0F;
      float positiveDurationMultiplier = 1.0F;
      float negativeDurationMultiplier = 1.0F;
      int positiveAmplifierBonus = 0;
      float healMultiplier = 1.0F;
      float damageMultiplier = 1.0F;
      float immunityMultiplier = 1.0F;
      boolean cancelAll = false;
      SevenEmotionType sevenEmotion = SevenEmotionType.NONE;
      String overlayKey = null;

      int tempValue = temperatureValue(profile.temperature());
      int qiBefore = state.qiBalance();
      if (tempValue != 0 && qiBefore != 0 && Integer.signum(tempValue) != Integer.signum(qiBefore)) {
         probabilityMultiplier *= rules.harmonizeNegativeProbabilityMultiplier;
         negativeDurationMultiplier *= rules.harmonizeNegativeDurationMultiplier;
         overlayKey = "message.herbcraft.pharmacology.harmonize";
      } else if (tempValue != 0 && qiBefore != 0 && Integer.signum(tempValue) == Integer.signum(qiBefore) && Math.abs(qiBefore) >= rules.sameQiThreshold) {
         probabilityMultiplier *= rules.sameQiNegativeProbabilityMultiplier;
         negativeDurationMultiplier *= rules.sameQiNegativeDurationMultiplier;
         overlayKey = tempValue > 0 ? "message.herbcraft.pharmacology.same_qi_hot" : "message.herbcraft.pharmacology.same_qi_cold";
      }

      for (String flavor : profile.flavors()) {
         switch (flavor) {
            case "sweet" -> healMultiplier *= rules.sweetHealMultiplier;
            case "bitter" -> {
               immunityMultiplier *= rules.bitterImmunityMultiplier;
               HerbStackManager.decrement(player, gameTime);
            }
            case "pungent" -> {
               positiveDurationMultiplier *= rules.pungentPositiveDurationMultiplier;
               if (EffectResolver.roll(player, rules.pungentNauseaChance)) {
                  player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, rules.pungentNauseaTicks, 0));
               }
            }
            case "sour", "astringent" -> {
               positiveDurationMultiplier *= rules.sourPositiveDurationMultiplier;
               negativeDurationMultiplier *= rules.sourNegativeDurationMultiplier;
            }
            case "salty" -> {
               immunityMultiplier *= rules.saltyImmunityMultiplier;
               if (nearWater(player)) {
                  positiveDurationMultiplier *= rules.saltyWaterDurationMultiplier;
                  negativeDurationMultiplier *= rules.saltyWaterDurationMultiplier;
               }
            }
            case "bland" -> probabilityMultiplier *= rules.blandSideEffectProbabilityMultiplier;
            default -> {
            }
         }
      }

      if (profile.flavors().contains("astringent") && player.getHealth() < 8.0F && state.consumeAstringentHealBonus(gameTime, rules.astringentCooldownTicks)) {
         healMultiplier += 0.35F;
      }

      if (gameTime < state.toxicProtectionExpiresAt() && hasTrait(profile, "toxic")) {
         negativeDurationMultiplier *= 0.6F;
      }

      PlayerPharmacologyState.RecentUse latest = state.latest().orElse(null);
      if (latest != null) {
         SevenEmotionType type = judgeSevenEmotion(profile, latest, state.recentUses(), gameTime, rules);
         sevenEmotion = type;
         switch (type) {
            case XIANG_FAN -> {
               cancelAll = true;
               state.setQiBalance(0);
               if (rules.xiangFanAppliesEffects) {
                  player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1));
                  player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
               }
            }
            case XIANG_SHA -> {
               negativeDurationMultiplier *= 0.6F;
               state.setToxicProtectionExpiresAt(gameTime + 400L);
               clearRecentToxicNegatives(player, latest);
            }
            case XIANG_WEI -> negativeDurationMultiplier *= 0.5F;
            case XIANG_XU -> {
               positiveAmplifierBonus += 1;
               healMultiplier *= 1.2F;
            }
            case XIANG_SHI -> positiveDurationMultiplier *= 1.15F;
            case XIANG_E -> {
               probabilityMultiplier *= 0.75F;
               healMultiplier *= 0.75F;
               damageMultiplier *= 0.75F;
            }
            case SINGLE -> probabilityBonus += 0.05F;
            default -> {
            }
         }
      }

      state.setQiBalance(qiBefore + tempValue);
      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      if (id != null) {
         state.addUse(id.toString(), profile, gameTime, nowMillis);
      }

      if (sevenEmotion.messageKey() != null) {
         player.sendOverlayMessage(Component.translatable(sevenEmotion.messageKey()));
      } else if (overlayKey != null) {
         player.sendOverlayMessage(Component.translatable(overlayKey));
      }

      return new PharmacologyResult(probabilityMultiplier, probabilityBonus, positiveDurationMultiplier, negativeDurationMultiplier, positiveAmplifierBonus, healMultiplier, damageMultiplier, immunityMultiplier, cancelAll, sevenEmotion);
   }

   public static boolean healingLike(Holder<MobEffect> effect) {
      return effect.is(MobEffects.INSTANT_HEALTH) || effect.is(MobEffects.REGENERATION) || effect.is(MobEffects.ABSORPTION) || effect.is(MobEffects.SATURATION);
   }

   private static SevenEmotionType judgeSevenEmotion(NatureProfile current, PlayerPharmacologyState.RecentUse latest, List<PlayerPharmacologyState.RecentUse> history, long gameTime, PharmacologyRules rules) {
      NatureProfile previous = latest.profile();
      if (isHotColdConflict(current, previous) && gameTime - latest.gameTime() < rules.xiangFanWindowTicks && (hasTrait(current, "toxic") || hasTrait(previous, "toxic"))) {
         return SevenEmotionType.XIANG_FAN;
      }
      if (hasTrait(current, "clearing") && hasTrait(previous, "toxic")) {
         return SevenEmotionType.XIANG_SHA;
      }
      if (hasTrait(current, "toxic") && hasTrait(previous, "clearing")) {
         return SevenEmotionType.XIANG_WEI;
      }
      if ((hasTrait(current, "stirring") && hasTrait(previous, "sedating")) || (hasTrait(current, "sedating") && hasTrait(previous, "stirring"))) {
         return SevenEmotionType.XIANG_E;
      }
      if (sharedTraits(current, previous) >= 2) {
         return SevenEmotionType.XIANG_XU;
      }
      if (hasAny(current, "nourishing", "clearing", "sedating") && hasAny(previous, "stirring", "clearing", "nourishing") && !sameProfile(current, previous)) {
         return SevenEmotionType.XIANG_SHI;
      }
      if (!history.isEmpty() && history.stream().allMatch(use -> sharedTraits(current, use.profile()) == 0)) {
         return SevenEmotionType.SINGLE;
      }
      return SevenEmotionType.NONE;
   }

   private static void clearRecentToxicNegatives(Player player, PlayerPharmacologyState.RecentUse latest) {
      try {
         ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(latest.itemId())));
         HerbEntry entry = HerbFoodRegistry.get(stack);
         if (entry != null) {
            entry.effects.stream().filter(effect -> !effect.positive()).forEach(effect -> player.removeEffect(effect.effect()));
         }
      } catch (Exception ignored) {
      }
   }

   private static boolean nearWater(Player player) {
      BlockPos center = player.blockPosition();
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
         if (player.level().getFluidState(pos).is(FluidTags.WATER)) {
            return true;
         }
      }
      return player.isInWaterOrRain();
   }

   private static boolean isHotColdConflict(NatureProfile a, NatureProfile b) {
      return ("hot".equals(a.temperature()) && "cold".equals(b.temperature())) || ("cold".equals(a.temperature()) && "hot".equals(b.temperature()));
   }

   private static int temperatureValue(String temperature) {
      return switch (temperature) {
         case "cold" -> -25;
         case "cool" -> -12;
         case "warm" -> 12;
         case "hot" -> 25;
         default -> 0;
      };
   }

   private static boolean hasTrait(NatureProfile profile, String trait) {
      return profile.traits().contains(trait);
   }

   private static boolean hasAny(NatureProfile profile, String... traits) {
      for (String trait : traits) {
         if (profile.traits().contains(trait)) {
            return true;
         }
      }
      return false;
   }

   private static int sharedTraits(NatureProfile a, NatureProfile b) {
      Set<String> traits = new HashSet<>(a.traits());
      traits.retainAll(b.traits());
      return traits.size();
   }

   private static boolean sameProfile(NatureProfile a, NatureProfile b) {
      return a.temperature().equals(b.temperature()) && a.flavors().equals(b.flavors()) && a.traits().equals(b.traits());
   }
}
