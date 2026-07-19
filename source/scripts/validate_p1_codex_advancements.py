#!/usr/bin/env python3
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
errors=[]
ADV=[('herbcraft',ROOT/'core/src/main/resources/data/herbcraft/advancement'),('herbcraft_alchemy',ROOT/'alchemy/src/main/resources/data/herbcraft_alchemy/advancement'),('herbcraft_cuisine',ROOT/'cuisine/src/main/resources/data/herbcraft_cuisine/advancement')]
LANG={'herbcraft':[ROOT/'core/src/main/resources/assets/herbcraft/lang/zh_cn.json',ROOT/'core/src/main/resources/assets/herbcraft/lang/en_us.json'],'herbcraft_alchemy':[ROOT/'alchemy/src/main/resources/assets/herbcraft/lang/zh_cn.json',ROOT/'alchemy/src/main/resources/assets/herbcraft/lang/en_us.json'],'herbcraft_cuisine':[ROOT/'cuisine/src/main/resources/assets/herbcraft_cuisine/lang/zh_cn.json',ROOT/'cuisine/src/main/resources/assets/herbcraft_cuisine/lang/en_us.json']}
all={}; roots=[]; keys=[]
for ns,d in ADV:
 for p in sorted(d.glob('*.json')):
  data=json.loads(p.read_text(encoding='utf-8')); aid=f'{ns}:{p.stem}'; all[aid]=data
  if 'parent' not in data: roots.append(aid)
  if data.get('criteria',{}).get('trigger',{}).get('trigger')!='minecraft:impossible': errors.append(f'{aid} criterion not impossible')
  if data.get('requirements') != [['trigger']]: errors.append(f'{aid} bad requirements')
  for f in ['title','description']:
   k=data.get('display',{}).get(f,{}).get('translate')
   if not k: errors.append(f'{aid} missing {f}')
   else:
    keys.append((ns,k,aid))
    if k.startswith('advancement.'): errors.append(f'{aid} uses old singular {k}')
    if not k.startswith('advancements.'): errors.append(f'{aid} bad key {k}')
if roots != ['herbcraft:root']: errors.append(f'expected one root herbcraft:root got {roots}')
for aid,data in all.items():
 if aid=='herbcraft:root': continue
 cur=data.get('parent'); seen={aid}
 while cur and cur!='herbcraft:root':
  if cur in seen: errors.append(f'{aid} cycle'); break
  seen.add(cur); pd=all.get(cur)
  if not pd: errors.append(f'{aid} missing parent {cur}'); break
  cur=pd.get('parent')
 if not cur: errors.append(f'{aid} chain missing root')
for ns,k,aid in keys:
 for lp in LANG[ns]:
  if k not in json.loads(lp.read_text(encoding='utf-8')): errors.append(f'{aid} missing lang {k} in {lp}')
if len(all)!=31: errors.append(f'expected 31 advancements got {len(all)}')
if 'herbcraft:gui/advancements/backgrounds/herbcraft' != all.get('herbcraft:root',{}).get('display',{}).get('background'): errors.append('root background must use 1.21.5+ texture-id format herbcraft:gui/advancements/backgrounds/herbcraft')
for aid,data in all.items():
 bg=data.get('display',{}).get('background')
 if bg and ('textures/' in bg or bg.endswith('.png')): errors.append(f'{aid} background uses pre-1.21.5 format (textures/ prefix or .png suffix): {bg}')
if not (ROOT/'core/src/main/resources/assets/herbcraft/textures/gui/advancements/backgrounds/herbcraft.png').exists(): errors.append('root background png missing')
for rel, needles in {'core/src/main/java/com/herbcraft/knowledge/HerbalChapter.java':['"overview"','overviewLines()','book.herbcraft.entry.overview.title'],'core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancements.java':['player.getAdvancements().award'],'core/src/main/java/com/herbcraft/advancement/HerbcraftAdvancementHooks.java':['KnowledgeInventoryScanner.registerHandler','first_bite']}.items():
 p=ROOT/rel
 if not p.exists(): errors.append(f'missing {rel}'); continue
 t=p.read_text(encoding='utf-8')
 for n in needles:
  if n not in t: errors.append(f'{rel} missing {n}')
if errors:
 print('FAIL'); [print(' -',e) for e in errors]; sys.exit(1)
print('PASS - P1 codex overview and unified advancement tree validation passed.')
