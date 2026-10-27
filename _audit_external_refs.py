import json
from pathlib import Path
base=Path('src/main/resources/data'); allfiles=list(base.rglob('*.json'))
for typ in ['configured_feature','placed_feature']:
 files=list((base/'tfcflorae'/'worldgen'/typ).rglob('*.json')); ids=['tfcflorae:'+p.relative_to(base/'tfcflorae'/'worldgen'/typ).with_suffix('').as_posix() for p in files]
 ext={i:[] for i in ids}
 for p in allfiles:
  rel=p.relative_to(base).as_posix()
  if f'/worldgen/{typ}/' in rel: continue
  t=p.read_text(encoding='utf-8')
  for i in ids:
   if '"'+i+'"' in t: ext[i].append(rel)
 print(typ,'external referenced',sum(bool(v) for v in ext.values()),'none',sum(not v for v in ext.values()))
 print('by path',{})
 print('none sample',[i for i,v in ext.items() if not v][:30])
 from collections import Counter
 print(Counter(x.split('/')[0] for vs in ext.values() for x in vs))
