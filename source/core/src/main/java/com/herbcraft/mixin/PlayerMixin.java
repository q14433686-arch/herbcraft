package com.herbcraft.mixin;

import com.herbcraft.HerbcraftPlayerData;
import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.pharmacology.PlayerPharmacologyState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements HerbcraftPlayerData {
   @Unique
   private int herbcraft$herbStack;
   @Unique
   private long herbcraft$herbExpiresAt;
   @Unique
   private int herbcraft$poppyCount;
   @Unique
   private long herbcraft$poppyExpiresAt;
   @Unique
   private int herbcraft$torchflowerCount;
   @Unique
   private long herbcraft$torchflowerExpiresAt;
   @Unique
   private int herbcraft$sugarCount;
   @Unique
   private long herbcraft$sugarExpiresAt;
   @Unique
   private int herbcraft$slimeCount;
   @Unique
   private long herbcraft$slimeExpiresAt;
   @Unique
   private final Map<String, Integer> herbcraft$knowledgeMap = new HashMap<>();
   @Unique
   private final Map<String, KnowledgeFlags> herbcraft$knowledgeFlagsMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$experiencedEffectsMap = new HashMap<>();
   @Unique
   private final Map<String, Map<String, PlayerClaimMark>> herbcraft$claimMarksMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$verifiedTrueClaimsMap = new HashMap<>();
   @Unique
   private final Map<String, Set<String>> herbcraft$verifiedFalseClaimsMap = new HashMap<>();
   @Unique
   private final Map<String, Long> herbcraft$effectImmunityMap = new HashMap<>();
   @Unique
   private final PlayerPharmacologyState herbcraft$pharmacologyState = new PlayerPharmacologyState();

   @Override
   public Map<String, Integer> herbcraft$knowledge() {
      return this.herbcraft$knowledgeMap;
   }

   @Override
   public Map<String, KnowledgeFlags> herbcraft$knowledgeFlags() { return this.herbcraft$knowledgeFlagsMap; }

   @Override
   public Map<String, Set<String>> herbcraft$experiencedEffects() { return this.herbcraft$experiencedEffectsMap; }

   @Override
   public Map<String, Map<String, PlayerClaimMark>> herbcraft$claimMarks() { return this.herbcraft$claimMarksMap; }

   @Override
   public Map<String, Set<String>> herbcraft$verifiedTrueClaims() { return this.herbcraft$verifiedTrueClaimsMap; }

   @Override
   public Map<String, Set<String>> herbcraft$verifiedFalseClaims() { return this.herbcraft$verifiedFalseClaimsMap; }

   @Override
   public Map<String, Long> herbcraft$effectImmunities() {
      return this.herbcraft$effectImmunityMap;
   }

   @Override
   public void herbcraft$clearEffectImmunities() {
      this.herbcraft$effectImmunityMap.clear();
   }

   @Override
   public int herbcraft$getHerbStack(long gameTime) {
      if (gameTime > this.herbcraft$herbExpiresAt) {
         if (this.herbcraft$herbStack > 0) {
            ((Player)(Object)this).sendSystemMessage(Component.translatable("message.herbcraft.herb_stack.fade"));
         }
         this.herbcraft$herbStack = 0;
      }

      return this.herbcraft$herbStack;
   }

   @Override
   public int herbcraft$addHerbStack(long gameTime) {
      if (gameTime > this.herbcraft$herbExpiresAt) {
         this.herbcraft$herbStack = 0;
      }

      this.herbcraft$herbStack++;
      this.herbcraft$herbExpiresAt = gameTime + 1200L;
      if (this.herbcraft$herbStack == 3 || this.herbcraft$herbStack == 5 || this.herbcraft$herbStack == 8) {
         ((Player)(Object)this).sendSystemMessage(Component.translatable("message.herbcraft.herb_stack.stage_" + this.herbcraft$herbStack));
      }
      return this.herbcraft$herbStack;
   }

   @Override
   public void herbcraft$clearHerbStack() {
      this.herbcraft$herbStack = 0;
      this.herbcraft$herbExpiresAt = 0L;
   }

   @Override
   public int herbcraft$decrementHerbStack(long gameTime) {
      this.herbcraft$getHerbStack(gameTime);
      if (this.herbcraft$herbStack > 0) {
         this.herbcraft$herbStack--;
      }
      if (this.herbcraft$herbStack <= 0) {
         this.herbcraft$herbExpiresAt = 0L;
      }
      return this.herbcraft$herbStack;
   }

   @Override
   public PlayerPharmacologyState herbcraft$pharmacologyState() {
      return this.herbcraft$pharmacologyState;
   }

   @Override
   public void herbcraft$clearPharmacologyState() {
      this.herbcraft$pharmacologyState.clear();
   }

   @Override
   public int herbcraft$incrementWindowCounter(String counter, long gameTime, int windowTicks) {
      if ("poppy".equals(counter)) {
         if (gameTime > this.herbcraft$poppyExpiresAt) {
            this.herbcraft$poppyCount = 0;
         }

         this.herbcraft$poppyCount++;
         this.herbcraft$poppyExpiresAt = gameTime + windowTicks;
         return this.herbcraft$poppyCount;
      } else if ("torchflower".equals(counter)) {
         if (gameTime > this.herbcraft$torchflowerExpiresAt) {
            this.herbcraft$torchflowerCount = 0;
         }

         this.herbcraft$torchflowerCount++;
         this.herbcraft$torchflowerExpiresAt = gameTime + windowTicks;
         return this.herbcraft$torchflowerCount;
      } else if ("sugar".equals(counter)) {
         if (gameTime > this.herbcraft$sugarExpiresAt) {
            this.herbcraft$sugarCount = 0;
         }

         this.herbcraft$sugarCount++;
         this.herbcraft$sugarExpiresAt = gameTime + windowTicks;
         return this.herbcraft$sugarCount;
      } else if ("slime".equals(counter)) {
         if (gameTime > this.herbcraft$slimeExpiresAt) {
            this.herbcraft$slimeCount = 0;
         }

         this.herbcraft$slimeCount++;
         this.herbcraft$slimeExpiresAt = gameTime + windowTicks;
         return this.herbcraft$slimeCount;
      } else {
         return 0;
      }
   }


   @Unique
   private static String herbcraft$encodeFlags(Map<String, KnowledgeFlags> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((key, flags) -> { if (builder.length() > 0) builder.append(';'); builder.append(key).append('=').append(flags.encode()); });
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeFlags(String value, Map<String, KnowledgeFlags> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { int split = entry.indexOf('='); if (split > 0) target.put(entry.substring(0, split), KnowledgeFlags.decode(entry.substring(split + 1))); }
   }
   @Unique
   private static String herbcraft$encodeSets(Map<String, Set<String>> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((key, set) -> { if (builder.length() > 0) builder.append(';'); builder.append(key).append('=').append(String.join(",", set)); });
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeSets(String value, Map<String, Set<String>> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { int split = entry.indexOf('='); if (split > 0) { Set<String> set = new HashSet<>(); String rest = entry.substring(split + 1); if (!rest.isBlank()) for (String v : rest.split(",")) if (!v.isBlank()) set.add(v); target.put(entry.substring(0, split), set); } }
   }
   @Unique
   private static String herbcraft$encodeMarks(Map<String, Map<String, PlayerClaimMark>> map) {
      StringBuilder builder = new StringBuilder();
      map.forEach((entryKey, marks) -> marks.forEach((claimId, mark) -> { if (builder.length() > 0) builder.append(';'); builder.append(entryKey).append('|').append(claimId).append('|').append(mark.ordinal()); }));
      return builder.toString();
   }
   @Unique
   private static void herbcraft$decodeMarks(String value, Map<String, Map<String, PlayerClaimMark>> target) {
      target.clear(); if (value == null || value.isBlank()) return;
      for (String entry : value.split(";")) { String[] p = entry.split("\\|"); if (p.length == 3) try { PlayerClaimMark mark = PlayerClaimMark.values()[Math.max(0, Math.min(PlayerClaimMark.values().length - 1, Integer.parseInt(p[2])))]; if (mark != PlayerClaimMark.UNMARKED) target.computeIfAbsent(p[0], k -> new HashMap<>()).put(p[1], mark); } catch (Exception ignored) {} }
   }

   @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
   private void herbcraft$writeData(ValueOutput output, CallbackInfo ci) {
      output.putInt("HerbcraftHerbStack", this.herbcraft$herbStack);
      output.putLong("HerbcraftHerbExpiresAt", this.herbcraft$herbExpiresAt);
      output.putInt("HerbcraftPoppyCount", this.herbcraft$poppyCount);
      output.putLong("HerbcraftPoppyExpiresAt", this.herbcraft$poppyExpiresAt);
      output.putInt("HerbcraftTorchflowerCount", this.herbcraft$torchflowerCount);
      output.putLong("HerbcraftTorchflowerExpiresAt", this.herbcraft$torchflowerExpiresAt);
      output.putInt("HerbcraftSugarCount", this.herbcraft$sugarCount);
      output.putLong("HerbcraftSugarExpiresAt", this.herbcraft$sugarExpiresAt);
      output.putInt("HerbcraftSlimeCount", this.herbcraft$slimeCount);
      output.putLong("HerbcraftSlimeExpiresAt", this.herbcraft$slimeExpiresAt);
      StringBuilder builder = new StringBuilder();
      this.herbcraft$knowledgeMap.forEach((key, state) -> {
         if (builder.length() > 0) {
            builder.append(';');
         }

         builder.append(key).append(':').append(state);
      });
      output.putString("HerbcraftKnowledge", builder.toString());
      output.putString("HerbcraftKnowledgeFlags", herbcraft$encodeFlags(this.herbcraft$knowledgeFlagsMap));
      output.putString("HerbcraftExperiencedEffects", herbcraft$encodeSets(this.herbcraft$experiencedEffectsMap));
      output.putString("HerbcraftClaimMarks", herbcraft$encodeMarks(this.herbcraft$claimMarksMap));
      output.putString("HerbcraftVerifiedTrueClaims", herbcraft$encodeSets(this.herbcraft$verifiedTrueClaimsMap));
      output.putString("HerbcraftVerifiedFalseClaims", herbcraft$encodeSets(this.herbcraft$verifiedFalseClaimsMap));
      StringBuilder immunityBuilder = new StringBuilder();
      this.herbcraft$effectImmunityMap.forEach((key, expiresAt) -> {
         if (immunityBuilder.length() > 0) {
            immunityBuilder.append(';');
         }

         immunityBuilder.append(key).append('|').append(expiresAt);
      });
      output.putString("HerbcraftEffectImmunities", immunityBuilder.toString());
      output.putString("HerbcraftPharmacologyState", this.herbcraft$pharmacologyState.encode());
   }

   @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
   private void herbcraft$readData(ValueInput input, CallbackInfo ci) {
      this.herbcraft$herbStack = input.getIntOr("HerbcraftHerbStack", 0);
      this.herbcraft$herbExpiresAt = input.getLongOr("HerbcraftHerbExpiresAt", 0L);
      this.herbcraft$poppyCount = input.getIntOr("HerbcraftPoppyCount", 0);
      this.herbcraft$poppyExpiresAt = input.getLongOr("HerbcraftPoppyExpiresAt", 0L);
      this.herbcraft$torchflowerCount = input.getIntOr("HerbcraftTorchflowerCount", 0);
      this.herbcraft$torchflowerExpiresAt = input.getLongOr("HerbcraftTorchflowerExpiresAt", 0L);
      this.herbcraft$sugarCount = input.getIntOr("HerbcraftSugarCount", 0);
      this.herbcraft$sugarExpiresAt = input.getLongOr("HerbcraftSugarExpiresAt", 0L);
      this.herbcraft$slimeCount = input.getIntOr("HerbcraftSlimeCount", 0);
      this.herbcraft$slimeExpiresAt = input.getLongOr("HerbcraftSlimeExpiresAt", 0L);
      this.herbcraft$knowledgeMap.clear();
      String knowledge = input.getStringOr("HerbcraftKnowledge", "");
      if (!knowledge.isEmpty()) {
         for (String entry : knowledge.split(";")) {
            int split = entry.lastIndexOf(58);
            if (split > 0) {
               try {
                  this.herbcraft$knowledgeMap.put(entry.substring(0, split), Integer.parseInt(entry.substring(split + 1)));
               } catch (NumberFormatException var10) {
               }
            }
         }
      }

      herbcraft$decodeFlags(input.getStringOr("HerbcraftKnowledgeFlags", ""), this.herbcraft$knowledgeFlagsMap);
      if (this.herbcraft$knowledgeFlagsMap.isEmpty() && !this.herbcraft$knowledgeMap.isEmpty()) {
         this.herbcraft$knowledgeMap.forEach((key, state) -> { KnowledgeFlags flags = new KnowledgeFlags(); flags.setLegacy(state); this.herbcraft$knowledgeFlagsMap.put(key, flags); });
      }
      herbcraft$decodeSets(input.getStringOr("HerbcraftExperiencedEffects", ""), this.herbcraft$experiencedEffectsMap);
      herbcraft$decodeMarks(input.getStringOr("HerbcraftClaimMarks", ""), this.herbcraft$claimMarksMap);
      herbcraft$decodeSets(input.getStringOr("HerbcraftVerifiedTrueClaims", ""), this.herbcraft$verifiedTrueClaimsMap);
      herbcraft$decodeSets(input.getStringOr("HerbcraftVerifiedFalseClaims", ""), this.herbcraft$verifiedFalseClaimsMap);

      this.herbcraft$effectImmunityMap.clear();
      String immunities = input.getStringOr("HerbcraftEffectImmunities", "");
      if (!immunities.isEmpty()) {
         for (String entry : immunities.split(";")) {
            int split = entry.lastIndexOf('|');
            if (split > 0) {
               try {
                  this.herbcraft$effectImmunityMap.put(entry.substring(0, split), Long.parseLong(entry.substring(split + 1)));
               } catch (NumberFormatException var11) {
               }
            }
         }
      }
      this.herbcraft$pharmacologyState.decode(input.getStringOr("HerbcraftPharmacologyState", ""));
   }

   @Inject(method = "die", at = @At("TAIL"))
   private void herbcraft$clearOnDeath(DamageSource damageSource, CallbackInfo ci) {
      this.herbcraft$clearHerbStack();
      this.herbcraft$clearEffectImmunities();
      this.herbcraft$clearPharmacologyState();
   }
}
