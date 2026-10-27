from pathlib import Path
from collections import Counter
base=Path('src/main/resources/data'); allfiles=list(base.rglob('*.json'))
for typ in ['configured_feature','placed_feature']:
 files=list((base/'tfcflorae'/'worldgen'/typ).rglob('*.json')); ids=['tfcflorae:'+p.relative_to(base/'tfcflorae'/'worldgen'/typ).with_suffix('').as_posix() for p in files]; ext={i:[] for i in ids}
 for p in allfiles:
  rel=p.relative_to(base).as_posix()
  if rel.startswith(f'tfcflorae/worldgen/{typ}/'): continue
  t=p.read_text(encoding='utf8')
  for i in ids:
   if '"'+i+'"' in t: ext[i].append(rel)
 print(typ,'external',sum(bool(v) for v in ext.values()),'none',sum(not v for v in ext.values()))
 print('none sample',[i for i,v in ext.items() if not v][:30])
 print('paths',Counter(x.split('/')[0]+'/'+x.split('/')[1] for vs in ext.values() for x in vs))
