from pathlib import Path
base=Path('src/main/resources/data'); allfiles=list(base.rglob('*.json'))
files=list((base/'tfcflorae'/'worldgen'/'placed_feature').rglob('*.json')); ids=['tfcflorae:'+p.relative_to(base/'tfcflorae'/'worldgen'/'placed_feature').with_suffix('').as_posix() for p in files]; ext={i:[] for i in ids}
for p in allfiles:
 rel=p.relative_to(base).as_posix()
 if '/worldgen/placed_feature/' in rel: continue
 t=p.read_text(encoding='utf8')
 for i in ids:
  if '"'+i+'"' in t: ext[i].append(rel)
from collections import Counter
for prefix in ['tree/','vein/']:
 vals=[i for i in ids if i.startswith('tfcflorae:'+prefix)]
 print(prefix,len(vals),'external',sum(bool(ext[i]) for i in vals),'none',sum(not ext[i] for i in vals))
 print('none', [i for i in vals if not ext[i]][:100])
