package com.herbcraft;

import com.herbcraft.api.HerbDefinition;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.advancement.HerbcraftAdvancementHooks;
import com.herbcraft.knowledge.ClaimMarkPayload;
import com.herbcraft.knowledge.CodexComponents;
import com.herbcraft.knowledge.CodexItems;
import com.herbcraft.knowledge.CodexLoot;
import com.herbcraft.knowledge.CodexSnapshotPayload;
import com.herbcraft.knowledge.CreativeKnowledgeItems;
import com.herbcraft.knowledge.HerbalChapter;
import com.herbcraft.knowledge.KnowledgeInventoryScanner;
import com.herbcraft.knowledge.KnowledgeSyncManager;
import com.herbcraft.knowledge.KnowledgeRumorRegistry;
import com.herbcraft.knowledge.KnowledgeSyncPayload;
import com.herbcraft.logic.EffectResolver;
import com.herbcraft.logic.LastMealTracker;
import com.herbcraft.nature.HerbNatureRegistry;
import com.herbcraft.pharmacology.PharmacologyRules;
import com.herbcraft.logic.ProjectileUseHandler;
import com.herbcraft.registry.HerbFoodRegistry;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Herbcraft implements ModInitializer {
   public static final String MOD_ID = "herbcraft";
   public static final Logger LOGGER = LoggerFactory.getLogger("herbcraft");
   public static final int SHORT_USE_THROW_THRESHOLD_TICKS = 6;

   public void onInitialize() {
      HerbFoodRegistry.loadFromBundledJson();
      HerbNatureRegistry.loadFromBundledJson();
      PharmacologyRules.loadFromBundledJson();
      KnowledgeRumorRegistry.loadFromBundledJson();
      HerbFoodRegistry.getHerbDefinitions().keySet().forEach(item -> com.herbcraft.api.HerbUsageAPI.registerUsage(item, "usage.herbcraft.edible"));
      DefaultItemComponentEvents.MODIFY.register(HerbFoodRegistry::registerDefaultComponents);
      CodexComponents.initialize();
      CodexItems.initialize();
      HerbalChapter.register();
      CodexLoot.register();
      KnowledgeInventoryScanner.register();
      KnowledgeInventoryScanner.registerHandler((player, stack) -> {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (id != null && HerbFoodRegistry.get(stack.getItem()) != null) {
            KnowledgeManager.markEncountered(player, "herbal", id.getPath());
         }
      });
      KnowledgeSyncManager.register();
      CreativeKnowledgeItems.register();
      LastMealTracker.register();
      HerbcraftAdvancementHooks.register();
      PayloadTypeRegistry.clientboundPlay().register(CodexSnapshotPayload.TYPE, CodexSnapshotPayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(KnowledgeSyncPayload.TYPE, KnowledgeSyncPayload.STREAM_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ClaimMarkPayload.TYPE, ClaimMarkPayload.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(ClaimMarkPayload.TYPE, (payload, context) -> {
         KnowledgeAPI.cycleClaimMark(context.player(), payload.entryKey(), payload.claimId());
         ServerPlayNetworking.send(context.player(), CodexSnapshotPayload.build(context.player()));
      });
      LOGGER.info("{} loaded: registered {} edible definitions and the Herbal Codex framework.", "herbcraft", getHerbDefinitions().size());
   }

   public static Map<Item, HerbDefinition> getHerbDefinitions() {
      return HerbFoodRegistry.getHerbDefinitions();
   }

   public static boolean isShortUseProjectile(ItemStack stack) {
      return ProjectileUseHandler.isShortUseProjectile(stack);
   }

   public static InteractionResult startShortUseProjectileEating(Level level, Player player, InteractionHand hand) {
      return ProjectileUseHandler.start(level, player, hand);
   }

   public static boolean releaseShortUseProjectile(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      return ProjectileUseHandler.release(stack, level, entity, timeLeft);
   }

   public static void applyAfterConsume(ItemStack stack, Level level, LivingEntity entity) {
      EffectResolver.applyAfterConsume(stack, level, entity);
   }
}
