package com.herbcraft.knowledge;

import com.herbcraft.api.HerbDefinition;
import com.herbcraft.api.HerbEffectEntry;
import com.herbcraft.api.HerbNatureAPI;
import com.herbcraft.api.HerbUsageAPI;
import com.herbcraft.api.HerbcraftAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import com.herbcraft.api.KnowledgeFlags;
import com.herbcraft.api.PlayerClaimMark;
import com.herbcraft.nature.NatureProfile;
import com.herbcraft.api.KnowledgeAPI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

public final class HerbalChapter {
   public static final String CHAPTER_ID = "herbal";

   private HerbalChapter() {
   }

   public static void register() {
      KnowledgeAPI.registerChapter("herbal", Component.translatable("book.herbcraft.chapter.herbal"), 0);
      KnowledgeAPI.registerSection("herbal", "overview", Component.translatable("book.herbcraft.section.herbal.overview"));
      KnowledgeAPI.registerEntry(
         "herbal",
         new KnowledgeAPI.Entry(
            "overview",
            "overview",
            Component.translatable("book.herbcraft.entry.overview.title"),
            (player, state) -> overviewLines(),
            player -> true,
            player -> true
         )
      );

      for (String section : List.of("flowers", "foliage", "sea", "nether", "ice", "curios")) {
         KnowledgeAPI.registerSection("herbal", section, Component.translatable("book.herbcraft.section.herbal." + section));
      }

      HerbcraftAPI.getHerbRegistry()
         .forEach(
            (item, definition) -> {
               String entryId = BuiltInRegistries.ITEM.getKey(item).getPath();
               KnowledgeAPI.registerEntry(
                  "herbal",
                  new KnowledgeAPI.Entry(
                     entryId,
                     classify(entryId),
                     Component.translatable(item.getDescriptionId()),
                     (player, state) -> lines(player, entryId, definition),
                     null,
                     null
                  )
               );
            }
         );
   }

   public static boolean hasEaten(ServerPlayer player, Item item) {
      return player.getStats().getValue(Stats.ITEM_USED.get(item)) > 0;
   }

   private static String classify(String path) {
      if (path.contains("crimson") || path.contains("warped") || path.contains("nether") || path.contains("weeping") || path.contains("twisting")) {
         return "nether";
      } else if (path.contains("snow") || path.contains("ice")) {
         return "ice";
      } else if (path.contains("kelp") || path.contains("seagrass") || path.contains("sea_pickle") || path.contains("lily_pad")) {
         return "sea";
      } else if (!path.contains("leaves")
         && !path.contains("sapling")
         && !path.contains("propagule")
         && !path.contains("fern")
         && !path.contains("grass")
         && !path.contains("vine")
         && !path.contains("bamboo")
         && !path.contains("sugar_cane")
         && !path.contains("cocoa")
         && !path.contains("seeds")
         && !path.contains("moss")
         && !path.contains("mushroom")
         && !path.contains("fungus")) {
         return !path.contains("dandelion")
               && !path.contains("poppy")
               && !path.contains("orchid")
               && !path.contains("bluet")
               && !path.contains("daisy")
               && !path.contains("cornflower")
               && !path.contains("lily_of_the_valley")
               && !path.contains("wither_rose")
               && !path.contains("sunflower")
               && !path.contains("lilac")
               && !path.contains("rose_bush")
               && !path.contains("peony")
               && !path.contains("pitcher")
               && !path.contains("torchflower")
               && !path.contains("eyeblossom")
               && !path.contains("allium")
               && !path.contains("azalea")
               && !path.contains("tulip")
               && !path.contains("petals")
               && !path.contains("spore_blossom")
               && !path.contains("cactus_flower")
               && !path.contains("flower")
            ? "curios"
            : "flowers";
      } else {
         return "foliage";
      }
   }

   private static List<Component> overviewLines() {
      return List.of(
         Component.translatable("book.herbcraft.entry.overview.line1").withStyle(ChatFormatting.GRAY),
         Component.translatable("book.herbcraft.entry.overview.line2"),
         Component.translatable("book.herbcraft.entry.overview.line3"),
         Component.translatable("book.herbcraft.entry.overview.line4").withStyle(ChatFormatting.DARK_GREEN)
      );
   }

   private static List<Component> lines(ServerPlayer player, String entryId, HerbDefinition definition) {
      List<Component> lines = new ArrayList<>();
      String key = "herbal/" + entryId;
      KnowledgeAPI.Entry entry = KnowledgeAPI.chapter("herbal").map(ch -> ch.entry(entryId)).orElse(null);
      KnowledgeDisplayState display = entry == null ? KnowledgeDisplayState.UNKNOWN : KnowledgeAPI.displayState(player, "herbal", entry);
      KnowledgeFlags flags = KnowledgeAPI.flags(player, key);
      NatureProfile nature = HerbNatureAPI.getNatureOrFallback(new net.minecraft.world.item.ItemStack(definition.item()));
      if (display == KnowledgeDisplayState.ENCOUNTERED) {
         lines.add(Component.translatable("knowledge.herbcraft.encountered.description").withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.nature_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.traits_unknown").withStyle(ChatFormatting.DARK_GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.line.effects_unknown").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.HEARD) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.reported").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
         appendClaimLines(player, key, lines);
         lines.add(Component.translatable("knowledge.herbcraft.heard.not_tasted").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }
      if (display == KnowledgeDisplayState.TASTED) {
         lines.add(HerbNatureAPI.natureLine(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).append(Component.translatable("knowledge.herbcraft.claim.verified_by_taste").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
         lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
         appendExperienced(player, key, definition, lines, false);
         lines.add(Component.translatable("knowledge.herbcraft.tasted.precise_unknown").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
         return lines;
      }
      if (display == KnowledgeDisplayState.MASTERED) {
         lines.add(HerbNatureAPI.natureLine(nature).withStyle(ChatFormatting.DARK_AQUA));
         lines.add(HerbNatureAPI.traitHint(nature).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         List<String> usageKeys = HerbUsageAPI.usageKeys(definition.item());
         if (!usageKeys.isEmpty()) lines.add(HerbUsageAPI.usageLine(usageKeys).withStyle(ChatFormatting.GOLD));
         lines.add(Component.translatable("book.herbcraft.entry.nutrition", definition.nutrition(), String.format("%.1f", definition.saturation())).withStyle(ChatFormatting.GRAY));
         for (HerbEffectEntry effect : definition.effects()) lines.add(preciseEffectLine(effect));
         if (flags.tasted()) {
            lines.add(Component.translatable("knowledge.herbcraft.taste.line", taste(nature)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("knowledge.herbcraft.feeling.line", Component.translatable("knowledge.herbcraft.feeling." + nature.temperature())).withStyle(ChatFormatting.GRAY));
            appendExperienced(player, key, definition, lines, true);
         }
         for (String route : com.herbcraft.api.KnowledgePracticeRegistry.routes(key)) lines.add(Component.translatable("knowledge.herbcraft.practice.route", route).withStyle(ChatFormatting.GOLD));
         appendClaimCorrections(player, key, lines);
      }
      return lines;
   }

   private static void appendClaimLines(ServerPlayer player, String key, List<Component> lines) {
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(key)) {
         PlayerClaimMark mark = KnowledgeAPI.claimMark(player, key, claim.id());
         MutableComponent line = Component.literal(mark.symbol() + " ").append(Component.translatable(claim.textKey()));
         line.append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.source." + claim.sourceTier().getSerializedName()).withStyle(ChatFormatting.DARK_GRAY));
         line.append(Component.literal(" ")).append(Component.translatable(claim.suspicious() ? "knowledge.herbcraft.claim.suspicious" : "knowledge.herbcraft.claim.reported").withStyle(claim.suspicious() ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
         lines.add(line.withStyle(ChatFormatting.GRAY));
      }
   }

   private static void appendClaimCorrections(ServerPlayer player, String key, List<Component> lines) {
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(key)) {
         if (!claim.truth()) {
            lines.add(Component.translatable("knowledge.herbcraft.mastered.false_claim", Component.translatable(claim.textKey())).withStyle(ChatFormatting.DARK_RED));
            PlayerClaimMark mark = KnowledgeAPI.claimMark(player, key, claim.id());
            if (mark == PlayerClaimMark.THINK_FALSE) lines.add(Component.translatable("knowledge.herbcraft.errata.player_correct").withStyle(ChatFormatting.GREEN));
            if (mark == PlayerClaimMark.THINK_TRUE) lines.add(Component.translatable("knowledge.herbcraft.errata.player_wrong").withStyle(ChatFormatting.RED));
         }
      }
   }

   private static MutableComponent taste(NatureProfile nature) {
      if (nature.tasteFlavors().contains("sweet") && nature.tasteFlavors().contains("bitter")) return Component.translatable("knowledge.herbcraft.taste.sweet_bitter");
      if (nature.tasteFlavors().contains("pungent") && nature.tasteFlavors().contains("sweet")) return Component.translatable("knowledge.herbcraft.taste.pungent_sweet");
      if (nature.tasteFlavors().contains("sour") && nature.tasteFlavors().contains("sweet")) return Component.translatable("knowledge.herbcraft.taste.sour_sweet");
      MutableComponent out = Component.empty(); boolean first = true;
      for (String flavor : nature.tasteFlavors()) { if (!first) out.append(Component.literal("，")); out.append(Component.translatable("knowledge.herbcraft.taste." + flavor)); first = false; }
      return out;
   }

   private static void appendExperienced(ServerPlayer player, String key, HerbDefinition definition, List<Component> lines, boolean precise) {
      java.util.Set<String> experienced = KnowledgeAPI.experiencedEffects(player).getOrDefault(key, java.util.Set.of());
      if (experienced.isEmpty()) { lines.add(Component.translatable("knowledge.herbcraft.tasted.no_effect_observed").withStyle(ChatFormatting.DARK_GRAY)); return; }
      for (HerbEffectEntry effect : definition.effects()) {
         net.minecraft.resources.Identifier id = BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)effect.effect().value());
         if (id != null && experienced.contains(id.toString())) {
            MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
            lines.add(Component.translatable("knowledge.herbcraft.tasted.experienced_format", name, precise ? Component.literal((effect.durationTicks()/20) + "s") : durationFuzzy(effect.durationTicks())).withStyle(ChatFormatting.BLUE));
         }
      }
   }

   private static MutableComponent durationFuzzy(int ticks) {
      if (ticks <= 60) return Component.translatable("knowledge.herbcraft.duration.instant");
      if (ticks <= 200) return Component.translatable("knowledge.herbcraft.duration.seconds");
      if (ticks <= 600) return Component.translatable("knowledge.herbcraft.duration.moment");
      return Component.translatable("knowledge.herbcraft.duration.long");
   }

   private static Component preciseEffectLine(HerbEffectEntry effect) {
      MutableComponent name = Component.translatable(((MobEffect)effect.effect().value()).getDescriptionId());
      if (effect.amplifier() > 0) name = Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.amplifier()));
      int seconds = effect.durationTicks() / 20;
      MutableComponent line = Component.translatable("potion.withDuration", name, String.format("%d:%02d", seconds / 60, seconds % 60));
      if (effect.chance() < 1.0F) line = line.append(Component.literal(" " + Math.round(effect.chance() * 100.0F) + "%"));
      return line.withStyle(effect.negative() ? ChatFormatting.RED : ChatFormatting.BLUE);
   }

}
