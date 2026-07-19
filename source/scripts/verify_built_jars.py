#!/usr/bin/env python3
import json, struct, sys, zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
JARS = {
    'core/build/libs/herbcraft-1.0.0.jar': {
        'id': 'herbcraft',
        'classes': ['com/herbcraft/Herbcraft.class', 'com/herbcraft/knowledge/client/HerbcraftCoreClient.class'],
        'resources': ['fabric.mod.json', 'herbcraft.mixins.json', 'data/herbcraft/herbs.json', 'data/herbcraft/tags/item/herb.json'],
    },
    'alchemy/build/libs/herbcraft-alchemy-1.0.0.jar': {
        'id': 'herbcraft_alchemy',
        'classes': ['com/herbcraft/alchemy/HerbcraftAlchemy.class', 'com/herbcraft/alchemy/client/HerbcraftAlchemyClient.class', 'com/herbcraft/alchemy/mixin/ItemCostAdvancedPotionMixin.class'],
        'resources': ['fabric.mod.json', 'herbcraft_alchemy.mixins.json', 'data/herbcraft/recipe/blank_adhesive.json'],
    },
    'cuisine/build/libs/herbcraft-cuisine-1.0.0.jar': {
        'id': 'herbcraft_cuisine',
        'classes': ['com/herbcraft/cuisine/HerbcraftCuisine.class', 'com/herbcraft/cuisine/client/HerbcraftCuisineClient.class', 'com/herbcraft/cuisine/recipe/HodgepodgeRecipe.class'],
        'resources': ['fabric.mod.json', 'data/herbcraft_cuisine/dishes.json', 'data/herbcraft_cuisine/recipe/hodgepodge.json'],
    },
}

def json_load(zf, n):
    with zf.open(n) as f: return json.load(f)

def major(b):
    assert b[:4] == b'\xca\xfe\xba\xbe'
    return struct.unpack('>H', b[6:8])[0]

errs=[]
for rel, spec in JARS.items():
    p=ROOT/rel
    if not p.exists():
        errs.append(f'missing {rel}'); continue
    with zipfile.ZipFile(p) as zf:
        names=set(zf.namelist())
        mod=json_load(zf,'fabric.mod.json')
        if mod.get('id') != spec['id']:
            errs.append(f'{rel}: id {mod.get("id")} != {spec["id"]}')
        for dep in ['fabricloader','minecraft','java','fabric-api']:
            if dep not in mod.get('depends',{}): errs.append(f'{rel}: missing dep {dep}')
        for r in spec['resources']:
            if r not in names: errs.append(f'{rel}: missing {r}')
            elif r.endswith('.json'): json_load(zf,r)
        for c in spec['classes']:
            if c not in names: errs.append(f'{rel}: missing {c}')
            elif major(zf.read(c)) != 69: errs.append(f'{rel}: {c} is not Java 25 class version')
if errs:
    print('VERIFY FAILED')
    print('\n'.join('- '+e for e in errs))
    sys.exit(1)
print('VERIFY OK: all built jars contain expected metadata/resources/classes and Java 25 bytecode.')
