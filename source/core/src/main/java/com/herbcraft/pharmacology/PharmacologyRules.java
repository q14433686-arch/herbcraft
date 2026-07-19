package com.herbcraft.pharmacology;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herbcraft.Herbcraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class PharmacologyRules {
   private static PharmacologyRules INSTANCE = defaults();

   public final int windowTicks;
   public final int regressionIntervalTicks;
   public final int regressionAmount;
   public final int sameQiThreshold;
   public final float harmonizeNegativeProbabilityMultiplier;
   public final float harmonizeNegativeDurationMultiplier;
   public final float sameQiNegativeProbabilityMultiplier;
   public final float sameQiNegativeDurationMultiplier;
   public final float sweetHealMultiplier;
   public final float bitterImmunityMultiplier;
   public final int baseImmunityTicks;
   public final float pungentPositiveDurationMultiplier;
   public final float pungentNauseaChance;
   public final int pungentNauseaTicks;
   public final float sourPositiveDurationMultiplier;
   public final float sourNegativeDurationMultiplier;
   public final int astringentCooldownTicks;
   public final float saltyImmunityMultiplier;
   public final float saltyWaterDurationMultiplier;
   public final float blandSideEffectProbabilityMultiplier;
   public final int xiangFanWindowTicks;
   public final boolean xiangFanAppliesEffects;

   private PharmacologyRules(
      int windowTicks,
      int regressionIntervalTicks,
      int regressionAmount,
      int sameQiThreshold,
      float harmonizeNegativeProbabilityMultiplier,
      float harmonizeNegativeDurationMultiplier,
      float sameQiNegativeProbabilityMultiplier,
      float sameQiNegativeDurationMultiplier,
      float sweetHealMultiplier,
      float bitterImmunityMultiplier,
      int baseImmunityTicks,
      float pungentPositiveDurationMultiplier,
      float pungentNauseaChance,
      int pungentNauseaTicks,
      float sourPositiveDurationMultiplier,
      float sourNegativeDurationMultiplier,
      int astringentCooldownTicks,
      float saltyImmunityMultiplier,
      float saltyWaterDurationMultiplier,
      float blandSideEffectProbabilityMultiplier,
      int xiangFanWindowTicks,
      boolean xiangFanAppliesEffects
   ) {
      this.windowTicks = windowTicks;
      this.regressionIntervalTicks = regressionIntervalTicks;
      this.regressionAmount = regressionAmount;
      this.sameQiThreshold = sameQiThreshold;
      this.harmonizeNegativeProbabilityMultiplier = harmonizeNegativeProbabilityMultiplier;
      this.harmonizeNegativeDurationMultiplier = harmonizeNegativeDurationMultiplier;
      this.sameQiNegativeProbabilityMultiplier = sameQiNegativeProbabilityMultiplier;
      this.sameQiNegativeDurationMultiplier = sameQiNegativeDurationMultiplier;
      this.sweetHealMultiplier = sweetHealMultiplier;
      this.bitterImmunityMultiplier = bitterImmunityMultiplier;
      this.baseImmunityTicks = baseImmunityTicks;
      this.pungentPositiveDurationMultiplier = pungentPositiveDurationMultiplier;
      this.pungentNauseaChance = pungentNauseaChance;
      this.pungentNauseaTicks = pungentNauseaTicks;
      this.sourPositiveDurationMultiplier = sourPositiveDurationMultiplier;
      this.sourNegativeDurationMultiplier = sourNegativeDurationMultiplier;
      this.astringentCooldownTicks = astringentCooldownTicks;
      this.saltyImmunityMultiplier = saltyImmunityMultiplier;
      this.saltyWaterDurationMultiplier = saltyWaterDurationMultiplier;
      this.blandSideEffectProbabilityMultiplier = blandSideEffectProbabilityMultiplier;
      this.xiangFanWindowTicks = xiangFanWindowTicks;
      this.xiangFanAppliesEffects = xiangFanAppliesEffects;
   }

   public static PharmacologyRules current() {
      return INSTANCE;
   }

   public static void loadFromBundledJson() {
      INSTANCE = defaults();
      try (InputStream input = PharmacologyRules.class.getClassLoader().getResourceAsStream("data/herbcraft/pharmacology_rules.json")) {
         if (input == null) {
            return;
         }
         JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
         INSTANCE = new PharmacologyRules(
            intValue(root, "window_ticks", INSTANCE.windowTicks),
            intValue(root, "qi_regression_interval_ticks", INSTANCE.regressionIntervalTicks),
            intValue(root, "qi_regression_amount", INSTANCE.regressionAmount),
            intValue(root, "same_qi_threshold", INSTANCE.sameQiThreshold),
            floatValue(root, "harmonize_negative_probability_multiplier", INSTANCE.harmonizeNegativeProbabilityMultiplier),
            floatValue(root, "harmonize_negative_duration_multiplier", INSTANCE.harmonizeNegativeDurationMultiplier),
            floatValue(root, "same_qi_negative_probability_multiplier", INSTANCE.sameQiNegativeProbabilityMultiplier),
            floatValue(root, "same_qi_negative_duration_multiplier", INSTANCE.sameQiNegativeDurationMultiplier),
            floatValue(root, "sweet_heal_multiplier", INSTANCE.sweetHealMultiplier),
            floatValue(root, "bitter_immunity_multiplier", INSTANCE.bitterImmunityMultiplier),
            intValue(root, "base_immunity_ticks", INSTANCE.baseImmunityTicks),
            floatValue(root, "pungent_positive_duration_multiplier", INSTANCE.pungentPositiveDurationMultiplier),
            floatValue(root, "pungent_nausea_chance", INSTANCE.pungentNauseaChance),
            intValue(root, "pungent_nausea_ticks", INSTANCE.pungentNauseaTicks),
            floatValue(root, "sour_positive_duration_multiplier", INSTANCE.sourPositiveDurationMultiplier),
            floatValue(root, "sour_negative_duration_multiplier", INSTANCE.sourNegativeDurationMultiplier),
            intValue(root, "astringent_cooldown_ticks", INSTANCE.astringentCooldownTicks),
            floatValue(root, "salty_immunity_multiplier", INSTANCE.saltyImmunityMultiplier),
            floatValue(root, "salty_water_duration_multiplier", INSTANCE.saltyWaterDurationMultiplier),
            floatValue(root, "bland_side_effect_probability_multiplier", INSTANCE.blandSideEffectProbabilityMultiplier),
            intValue(root, "xiang_fan_window_ticks", INSTANCE.xiangFanWindowTicks),
            boolValue(root, "xiang_fan_applies_effects", INSTANCE.xiangFanAppliesEffects)
         );
      } catch (Exception e) {
         Herbcraft.LOGGER.warn("Failed to load pharmacology_rules.json; using defaults", e);
      }
   }

   private static PharmacologyRules defaults() {
      return new PharmacologyRules(600, 1200, 5, 50, 0.7F, 0.75F, 1.3F, 1.25F, 1.15F, 1.2F, 600, 1.15F, 0.08F, 60, 1.1F, 0.9F, 600, 1.15F, 1.1F, 0.85F, 200, true);
   }

   private static int intValue(JsonObject root, String key, int fallback) {
      return root.has(key) ? root.get(key).getAsInt() : fallback;
   }

   private static float floatValue(JsonObject root, String key, float fallback) {
      return root.has(key) ? root.get(key).getAsFloat() : fallback;
   }

   private static boolean boolValue(JsonObject root, String key, boolean fallback) {
      return root.has(key) ? root.get(key).getAsBoolean() : fallback;
   }
}
