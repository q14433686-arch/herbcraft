package com.herbcraft.knowledge;

import com.herbcraft.api.KnowledgeAPI;
import com.herbcraft.api.KnowledgeDisplayState;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record CodexSnapshotPayload(List<CodexSnapshotPayload.ChapterSnap> chapters) implements CustomPacketPayload {
   public static final Type<CodexSnapshotPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("herbcraft", "codex_snapshot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CodexSnapshotPayload> STREAM_CODEC = CustomPacketPayload.codec(CodexSnapshotPayload::write, CodexSnapshotPayload::read);
   public Type<CodexSnapshotPayload> type() { return TYPE; }
   public static CodexSnapshotPayload build(ServerPlayer player) {
      List<ChapterSnap> chapters = new ArrayList<>();
      for (KnowledgeAPI.Chapter chapter : KnowledgeAPI.chapters()) {
         Map<String,List<EntrySnap>> bySection = new LinkedHashMap<>(); chapter.sections().keySet().forEach(s->bySection.put(s,new ArrayList<>())); bySection.put("",new ArrayList<>());
         for (KnowledgeAPI.Entry entry : chapter.entries()) {
            KnowledgeDisplayState display = KnowledgeAPI.displayState(player, chapter.id(), entry);
            List<Component> lines = display == KnowledgeDisplayState.UNKNOWN ? List.of() : entry.lines().apply(player, KnowledgeAPI.state(player, chapter.id(), entry));
            String key = chapter.id()+"/"+entry.id();
            List<ClaimSnap> claims = claimSnaps(player, key, lines);
            bySection.computeIfAbsent(entry.section()==null?"":entry.section(), k->new ArrayList<>()).add(new EntrySnap(key, entry.title(), display.ordinal(), lines, claims));
         }
         List<SectionSnap> sections=new ArrayList<>(); chapter.sections().forEach((sid,title)->{List<EntrySnap> es=bySection.getOrDefault(sid,List.of()); if(!es.isEmpty()) sections.add(new SectionSnap(title,es));}); List<EntrySnap> un=bySection.getOrDefault("",List.of()); if(!un.isEmpty()) sections.add(new SectionSnap(Component.translatable("book.herbcraft.section.general"),un)); chapters.add(new ChapterSnap(chapter.title(),sections));
      }
      return new CodexSnapshotPayload(chapters);
   }

   private static List<ClaimSnap> claimSnaps(ServerPlayer player, String entryKey, List<Component> lines) {
      List<ClaimSnap> result = new ArrayList<>();
      List<KnowledgeClaim> claims = KnowledgeRumorRegistry.claims(entryKey);
      int searchFrom = 0;
      for (KnowledgeClaim claim : claims) {
         int mark = KnowledgeAPI.claimMark(player, entryKey, claim.id()).ordinal();
         int lineIndex = findClaimLine(lines, searchFrom);
         if (lineIndex >= 0) {
            result.add(new ClaimSnap(entryKey, claim.id(), mark, lineIndex));
            searchFrom = lineIndex + 1;
         }
      }
      return result;
   }

   private static int findClaimLine(List<Component> lines, int start) {
      for (int i = Math.max(0, start); i < lines.size(); i++) {
         String text = lines.get(i).getString().trim();
         if (text.startsWith("○") || text.startsWith("✓") || text.startsWith("✕")) {
            return i;
         }
      }
      return -1;
   }

   private void write(RegistryFriendlyByteBuf buf) { buf.writeVarInt(chapters.size()); for(ChapterSnap c:chapters){ ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf,c.title()); buf.writeVarInt(c.sections().size()); for(SectionSnap s:c.sections()){ ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf,s.title()); buf.writeVarInt(s.entries().size()); for(EntrySnap e:s.entries()){ buf.writeUtf(e.entryKey()); ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf,e.title()); buf.writeVarInt(e.state()); buf.writeVarInt(e.lines().size()); for(Component l:e.lines()) ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf,l); buf.writeVarInt(e.claims().size()); for(ClaimSnap cl:e.claims()){buf.writeUtf(cl.entryKey());buf.writeUtf(cl.claimId());buf.writeVarInt(cl.mark());buf.writeVarInt(cl.lineIndex());} } } } }
   private static CodexSnapshotPayload read(RegistryFriendlyByteBuf buf) { int cc=buf.readVarInt(); List<ChapterSnap> ch=new ArrayList<>(cc); for(int i=0;i<cc;i++){ Component ct=ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf); int sc=buf.readVarInt(); List<SectionSnap> sections=new ArrayList<>(sc); for(int j=0;j<sc;j++){ Component st=ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf); int ec=buf.readVarInt(); List<EntrySnap> entries=new ArrayList<>(ec); for(int k=0;k<ec;k++){ String key=buf.readUtf(); Component et=ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf); int state=buf.readVarInt(); int lc=buf.readVarInt(); List<Component> lines=new ArrayList<>(lc); for(int l=0;l<lc;l++) lines.add(ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf)); int clc=buf.readVarInt(); List<ClaimSnap> claims=new ArrayList<>(clc); for(int m=0;m<clc;m++) claims.add(new ClaimSnap(buf.readUtf(),buf.readUtf(),buf.readVarInt(),buf.readVarInt())); entries.add(new EntrySnap(key,et,state,lines,claims)); } sections.add(new SectionSnap(st,entries)); } ch.add(new ChapterSnap(ct,sections)); } return new CodexSnapshotPayload(ch); }
   public record ChapterSnap(Component title, List<SectionSnap> sections) {}
   public record EntrySnap(String entryKey, Component title, int state, List<Component> lines, List<ClaimSnap> claims) {}
   public record SectionSnap(Component title, List<EntrySnap> entries) {}
   public record ClaimSnap(String entryKey, String claimId, int mark, int lineIndex) {}
}
