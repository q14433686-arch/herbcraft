package com.herbcraft.knowledge;

import com.herbcraft.advancement.HerbcraftAdvancements;
import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeManager;
import com.herbcraft.api.PlayerClaimMark;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class TatteredPageItem extends Item {
   public TatteredPageItem(Properties properties) {
      super(properties);
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return InteractionResult.SUCCESS;
      } else {
         CodexComponents.PageEntry target = (CodexComponents.PageEntry)stack.get(CodexComponents.PAGE_ENTRY);
         String chapterId = target != null && !target.chapter().isEmpty() ? target.chapter() : anyChapter(serverPlayer);
         if (chapterId == null) {
            return InteractionResult.FAIL;
         } else {
            PageKind kind = (PageKind)stack.get(CodexComponents.PAGE_KIND);
            if (kind == null) kind = PageKind.COMMON;
            Component learnedTitle = null;
            String entryId = target != null && !target.entry().isEmpty() ? target.entry() : null;
            if (entryId == null) {
               Optional<KnowledgeAPI.Entry> random = KnowledgeAPI.hearRandomUnknown(serverPlayer, chapterId);
               if (random.isEmpty()) {
                  serverPlayer.sendOverlayMessage(Component.translatable("message.herbcraft.page.nothing_new"));
                  return InteractionResult.FAIL;
               }
               entryId = random.get().id();
               learnedTitle = random.get().title();
            } else {
               String finalEntryId = entryId;
               learnedTitle = KnowledgeAPI.chapter(chapterId).map(chapter -> chapter.entries().stream().filter(e -> e.id().equals(finalEntryId)).findFirst().map(KnowledgeAPI.Entry::title).orElse(null)).orElse(null);
            }
            if (kind == PageKind.ERRATA) {
               resolveErrata(serverPlayer, chapterId + "/" + entryId);
            } else {
               KnowledgeManager.markHeard(serverPlayer, chapterId, entryId, false);
            }

            HerbcraftAdvancements.grant(serverPlayer, "first_page");
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverPlayer.sendOverlayMessage(Component.translatable("message.herbcraft.page.learned", new Object[]{learnedTitle}));
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
         }
      }
   }


   private static void resolveErrata(ServerPlayer player, String entryKey) {
      KnowledgeClaim picked = null;
      for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(entryKey)) {
         boolean resolved = KnowledgeAPI.verifiedTrueClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id()) || KnowledgeAPI.verifiedFalseClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id());
         if (!resolved && claim.suspicious()) { picked = claim; break; }
      }
      if (picked == null) for (KnowledgeClaim claim : KnowledgeRumorRegistry.claims(entryKey)) {
         boolean resolved = KnowledgeAPI.verifiedTrueClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id()) || KnowledgeAPI.verifiedFalseClaims(player).getOrDefault(entryKey, java.util.Set.of()).contains(claim.id());
         if (!resolved) { picked = claim; break; }
      }
      if (picked == null) { player.sendOverlayMessage(Component.translatable("knowledge.herbcraft.errata.none")); return; }
      KnowledgeAPI.verifyClaim(player, entryKey, picked.id(), picked.truth());
      PlayerClaimMark mark = KnowledgeAPI.claimMark(player, entryKey, picked.id());
      Component verdict = Component.translatable(picked.truth() ? "knowledge.herbcraft.errata.true" : "knowledge.herbcraft.errata.false");
      if ((mark == PlayerClaimMark.THINK_TRUE && picked.truth()) || (mark == PlayerClaimMark.THINK_FALSE && !picked.truth())) verdict = verdict.copy().append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.errata.player_correct"));
      else if (mark != PlayerClaimMark.UNMARKED) verdict = verdict.copy().append(Component.literal(" ")).append(Component.translatable("knowledge.herbcraft.errata.player_wrong"));
      player.sendOverlayMessage(verdict);
   }

   private static String anyChapter(ServerPlayer player) {
      List<String> candidates = new ArrayList<>();

      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            if (KnowledgeAPI.state(player, chapter.id(), entry) == KnowledgeAPI.State.UNKNOWN) {
               candidates.add(chapter.id());
               break;
            }
         }
      }

      return candidates.isEmpty() ? null : candidates.get(player.getRandom().nextInt(candidates.size()));
   }
}
