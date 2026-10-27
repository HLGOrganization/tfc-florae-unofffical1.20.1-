import json,re
from pathlib import Path
from collections import Counter,defaultdict
base=Path('src/main/resources/data'); allfiles=list(base.rglob('*.json')); texts={p:p.read_text(encoding='utf-8') for p in allfiles}
for typ in ['configured_feature','placed_feature']:
 files=list((base/'tfcflorae'/'worldgen'/typ).rglob('*.json')); ids=['tfcflorae:'+p.relative_to(base/'tfcflorae'/'worldgen'/typ).with_suffix('').as_posix() for p in files]
 refs=defaultdict(list)
 for p,t in texts.items():
  for i in ids:
   if '"'+i+'"' in t: refs[i].append(p.relative_to(base).as_posix())
 print('\n',typ)
 cats=Counter()
 for i,vs in refs.items():
  for v in vs:
   seg=v.split('/')
   if seg[0]=='tfcflorae' and seg[1]=='worldgen': cat='own-worldgen'
   elif seg[0]=='tfcflorae' and seg[1]=='tags': cat='tfcflorae-tags'
   elif seg[0]=='tfc' and seg[1]=='tags': cat='tfc-tags'
   else: cat='/'.join(seg[:2])
   cats[cat]+=1
 print(cats)
 # print sample refs for IDs with no external ref
 for i in ids[:5]: print(i,refs[i])
 # detect refs only own-worldgen
 only=[i for i in ids if all(('worldgen/' in v) for v in refs[i])]
 print('only worldgen refs',len(only),only[:10])
