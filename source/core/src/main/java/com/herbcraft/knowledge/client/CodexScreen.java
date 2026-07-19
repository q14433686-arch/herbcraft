package com.herbcraft.knowledge.client;

import com.herbcraft.knowledge.ClaimMarkPayload;
import com.herbcraft.knowledge.CodexSnapshotPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class CodexScreen extends Screen {
   private static final int MIN_BOOK_WIDTH = 320;
   private static final int MAX_BOOK_WIDTH = 520;
   private static final int MIN_BOOK_HEIGHT = 210;
   private static final int MAX_BOOK_HEIGHT = 330;
   private static final int ROW_HEIGHT = 12;
   private static final int PADDING = 8;
   private static final int HEADER_HEIGHT = 20;
   private static final int LINE_HEIGHT = 10;
   private static String lastViewedKey = null;
   private static final Set<String> expandedNodes = new HashSet<>();
   private final CodexSnapshotPayload snapshot;
   private final List<CodexScreen.Row> rows = new ArrayList<>();
   private final List<CodexScreen.ClaimHitbox> claimHitboxes = new ArrayList<>();
   private double leftScroll = 0.0;
   private double rightScroll = 0.0;
   private CodexSnapshotPayload.EntrySnap selected;
   private String selectedKey;

   public CodexScreen(CodexSnapshotPayload snapshot) {
      super(Component.translatable("book.herbcraft.codex.title"));
      this.snapshot = snapshot;
      this.rebuildRows();
      if (lastViewedKey != null) {
         this.selectByKey(lastViewedKey);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void rebuildRows() {
      this.rows.clear();
      int chapterOrdinal = 0;

      for (CodexSnapshotPayload.ChapterSnap chapter : this.snapshot.chapters()) {
         String chapterKey = "c" + ++chapterOrdinal;
         Component header = Component.translatable("book.herbcraft.chapter_format", new Object[]{chapterOrdinal, chapter.title()})
            .withStyle(ChatFormatting.GOLD);
         this.rows.add(new CodexScreen.Row(chapterKey, 0, this.prefixed(chapterKey, header), true, null));
         if (expandedNodes.contains(chapterKey)) {
            int sectionIndex = 0;

            for (CodexSnapshotPayload.SectionSnap section : chapter.sections()) {
               String sectionKey = chapterKey + "/s" + ++sectionIndex;
               int known = (int)section.entries().stream().filter(e -> e.state() > 0).count();
               Component sectionLabel = section.title()
                  .copy()
                  .withStyle(ChatFormatting.YELLOW)
                  .append(Component.literal(" " + known + "/" + section.entries().size()).withStyle(ChatFormatting.DARK_GRAY));
               this.rows.add(new CodexScreen.Row(sectionKey, 1, this.prefixed(sectionKey, sectionLabel), true, null));
               if (expandedNodes.contains(sectionKey)) {
                  int entryIndex = 0;

                  for (CodexSnapshotPayload.EntrySnap entry : section.entries()) {
                     String entryKey = sectionKey + "/e" + ++entryIndex;
                     Component label = entry.state() == 0
                        ? Component.translatable("tooltip.herbcraft.page.unknown").withStyle(ChatFormatting.DARK_GRAY)
                        : entry.title().copy().withStyle(entry.state() == 2 ? ChatFormatting.WHITE : ChatFormatting.GRAY);
                     this.rows.add(new CodexScreen.Row(entryKey, 2, label, false, entry));
                  }
               }
            }
         }
      }
   }

   private Component prefixed(String key, Component label) {
      String arrow = expandedNodes.contains(key) ? "▼ " : "▶ ";
      return Component.literal(arrow).withStyle(ChatFormatting.DARK_GREEN).copy().append(label);
   }

   private void selectByKey(String key) {
      for (CodexScreen.Row row : this.rows) {
         if (row.key().equals(key) && row.entry() != null) {
            this.selected = row.entry();
            this.selectedKey = key;
            this.rightScroll = 0.0;
            return;
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(extractor, mouseX, mouseY, partialTick);
      CodexScreen.Layout layout = this.layout();
      this.leftScroll = clamp(this.leftScroll, this.maxLeftScroll(layout));
      this.rightScroll = clamp(this.rightScroll, this.maxRightScroll(layout));
      extractor.fill(0, 0, this.width, this.height, 0x80000000);
      extractor.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, 0x90181818);
      extractor.fill(layout.x, layout.y, layout.x + layout.width, layout.y + 20, 0xA0183018);
      extractor.fill(layout.leftX, layout.leftY, layout.leftX + layout.leftWidth, layout.bottom, 0xA0101010);
      extractor.fill(layout.rightX - 1, layout.leftY, layout.rightX, layout.bottom, 0xD0D0D0D0);
      this.drawTitleWrapped(
         extractor,
         this.title.copy().withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}),
         layout.x + 8,
         layout.y + 5,
         layout.width - 16,
         2,
         -1
      );
      this.renderLeftPanel(extractor, mouseX, mouseY, layout);
      this.renderRightPanel(extractor, layout);
   }

   private void renderLeftPanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, CodexScreen.Layout layout) {
      extractor.enableScissor(layout.leftX, layout.leftY, layout.leftX + layout.leftWidth, layout.bottom);
      int y = layout.leftY + 8 - (int)this.leftScroll;
      int textMaxWidth = layout.leftWidth - 16;

      for (CodexScreen.Row row : this.rows) {
         if (y > layout.leftY - 12 && y < layout.bottom) {
            boolean hover = mouseX >= layout.leftX && mouseX < layout.leftX + layout.leftWidth && mouseY >= y && mouseY < y + 12;
            boolean isSelected = row.key().equals(this.selectedKey);
            if (hover || isSelected) {
               extractor.fill(layout.leftX, y - 1, layout.leftX + layout.leftWidth, y + 12 - 1, isSelected ? 0xB0456045 : 0xB0303030);
            }

            int textX = layout.leftX + 8 + row.indent() * 9;
            int rowMaxWidth = Math.max(8, textMaxWidth - row.indent() * 9);
            Component display = CodexTextLayout.trimWithEllipsis(this.font, row.label(), rowMaxWidth);
            extractor.text(this.font, display, textX, y, -1);
            if (hover && row.entry() != null && row.entry().state() > 0 && CodexTextLayout.isTrimmed(this.font, row.label(), rowMaxWidth)) {
               extractor.setTooltipForNextFrame(this.font, row.label(), mouseX, mouseY);
            } else if (hover && row.isNode() && CodexTextLayout.isTrimmed(this.font, row.label(), rowMaxWidth)) {
               extractor.setTooltipForNextFrame(this.font, row.label(), mouseX, mouseY);
            }
         }

         y += 12;
      }

      extractor.disableScissor();
   }

   private void renderRightPanel(GuiGraphicsExtractor extractor, CodexScreen.Layout layout) {
      extractor.enableScissor(layout.rightX, layout.rightY, layout.rightX + layout.rightWidth, layout.bottom);
      int rx = layout.rightX + 8;
      int ry = layout.rightY + 8 - (int)this.rightScroll;
      int textWidth = layout.rightWidth - 16;
      this.claimHitboxes.clear();
      if (this.selected == null) {
         ry = this.drawWrapped(extractor, Component.translatable("book.herbcraft.codex.hint").withStyle(ChatFormatting.GRAY), rx, ry, textWidth, -1);
      } else if (this.selected.state() == 0) {
         ry = this.drawWrapped(
            extractor,
            Component.translatable("book.herbcraft.codex.unrecorded").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}),
            rx,
            ry,
            textWidth,
            -1
         );
      } else {
         Component header = this.selected
            .title()
            .copy()
            .withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.BOLD})
            .append(Component.literal("  "))
            .append(
               Component.translatable("knowledge.herbcraft.state." + stateKey(this.selected.state()))
                  .withStyle(this.selected.state() == 4 ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
            );
         ry = this.drawTitleWrapped(extractor, header, rx, ry, textWidth, 2, -1) + 3;
         extractor.fill(rx, ry, rx + textWidth, ry + 1, -12951750);
         ry += 6;

         java.util.Map<Integer, CodexSnapshotPayload.ClaimSnap> claimsByLine = new java.util.HashMap<>();
         for (CodexSnapshotPayload.ClaimSnap claim : this.selected.claims()) {
            claimsByLine.put(claim.lineIndex(), claim);
         }
         int lineIndex = 0;
         for (Component line : this.selected.lines()) {
            CodexSnapshotPayload.ClaimSnap claim = claimsByLine.get(lineIndex);
            if (claim != null) {
               this.claimHitboxes.add(new CodexScreen.ClaimHitbox(rx, ry, rx + 16, ry + 12, claim.entryKey(), claim.claimId()));
            }
            ry = this.drawWrapped(extractor, line, rx, ry, textWidth, -1) + 2;
            lineIndex++;
         }
      }

      extractor.disableScissor();
   }

   private int drawWrapped(GuiGraphicsExtractor extractor, Component text, int x, int y, int width, int color) {
      for (FormattedCharSequence wrapped : CodexTextLayout.wrap(this.font, text, width)) {
         extractor.text(this.font, wrapped, x, y, color);
         y += 10;
      }

      return y;
   }

   private int drawTitleWrapped(GuiGraphicsExtractor extractor, Component text, int x, int y, int width, int maxLines, int color) {
      List<FormattedCharSequence> lines = CodexTextLayout.wrap(this.font, text, width);
      int drawn = Math.min(maxLines, lines.size());

      for (int i = 0; i < drawn; i++) {
         extractor.text(this.font, lines.get(i), x, y, color);
         y += 10;
      }

      return y;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      CodexScreen.Layout layout = this.layout();
      if (event.button() == 0) {
         for (CodexScreen.ClaimHitbox hitbox : this.claimHitboxes) {
            if (event.x() >= hitbox.x1() && event.x() <= hitbox.x2() && event.y() >= hitbox.y1() && event.y() <= hitbox.y2()) {
               ClientPlayNetworking.send(new ClaimMarkPayload(hitbox.entryKey(), hitbox.claimId()));
               return true;
            }
         }
      }
      if (event.button() == 0 && event.x() >= layout.leftX && event.x() < layout.leftX + layout.leftWidth && event.y() >= layout.leftY) {
         int index = (int)((event.y() - layout.leftY - 8.0 + this.leftScroll) / 12.0);
         if (index >= 0 && index < this.rows.size()) {
            CodexScreen.Row row = this.rows.get(index);
            if (row.isNode()) {
               if (!expandedNodes.remove(row.key())) {
                  expandedNodes.add(row.key());
               }

               this.rebuildRows();
            } else {
               this.selected = row.entry();
               this.selectedKey = row.key();
               lastViewedKey = row.key();
               this.rightScroll = 0.0;
            }

            return true;
         }
      }

      return super.mouseClicked(event, doubled);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
      CodexScreen.Layout layout = this.layout();
      if (mouseX < layout.rightX) {
         this.leftScroll = clamp(this.leftScroll - deltaY * 12.0 * 2.0, this.maxLeftScroll(layout));
      } else {
         this.rightScroll = clamp(this.rightScroll - deltaY * 12.0 * 2.0, this.maxRightScroll(layout));
      }

      return true;
   }

   private int contentHeight(CodexScreen.Layout layout) {
      int textWidth = layout.rightWidth - 16;
      this.claimHitboxes.clear();
      if (this.selected == null) {
         return CodexTextLayout.wrappedHeight(this.font, Component.translatable("book.herbcraft.codex.hint"), textWidth, 10) + 16;
      } else if (this.selected.state() == 0) {
         return CodexTextLayout.wrappedHeight(this.font, Component.translatable("book.herbcraft.codex.unrecorded"), textWidth, 10) + 16;
      } else {
         int height = Math.min(2, CodexTextLayout.wrap(this.font, this.selected.title(), textWidth).size()) * 10 + 10;

         for (Component line : this.selected.lines()) {
            height += CodexTextLayout.wrappedHeight(this.font, line, textWidth, 10) + 2;
         }

         return height + 16;
      }
   }

   private int maxLeftScroll(CodexScreen.Layout layout) {
      return Math.max(0, this.rows.size() * 12 + 16 - (layout.height - 20));
   }

   private int maxRightScroll(CodexScreen.Layout layout) {
      return Math.max(0, this.contentHeight(layout) - (layout.height - 20));
   }

   private CodexScreen.Layout layout() {
      int bookWidth = clampInt(this.width - 32, 320, 520);
      int bookHeight = clampInt(this.height - 32, 210, 330);
      bookWidth = Math.min(bookWidth, this.width);
      bookHeight = Math.min(bookHeight, this.height);
      int x = (this.width - bookWidth) / 2;
      int y = (this.height - bookHeight) / 2;
      int leftWidth = clampInt((int)(bookWidth * 0.34F), 130, 190);
      if (leftWidth > bookWidth - 150) {
         leftWidth = Math.max(96, bookWidth - 150);
      }

      int rightX = x + leftWidth + 1;
      int rightWidth = bookWidth - leftWidth - 1;
      return new CodexScreen.Layout(x, y, bookWidth, bookHeight, x, y + 20, leftWidth, rightX, y + 20, rightWidth, y + bookHeight);
   }

   private static double clamp(double value, double max) {
      return Math.max(0.0, Math.min(value, max));
   }

   private static String stateKey(int state) {
      return switch (state) { case 1 -> "encountered"; case 2 -> "heard"; case 3 -> "tasted"; case 4 -> "mastered"; default -> "unknown"; };
   }

   private static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   private record Layout(int x, int y, int width, int height, int leftX, int leftY, int leftWidth, int rightX, int rightY, int rightWidth, int bottom) {
   }

   private record Row(String key, int indent, Component label, boolean isNode, CodexSnapshotPayload.EntrySnap entry) {
   }
   private record ClaimHitbox(int x1, int y1, int x2, int y2, String entryKey, String claimId) {
   }
}
