import json,re
from pathlib import Path
base=Path('src/main/resources/data')
allfiles=list(base.rglob('*.json'))
texts={p:p.read_text(encoding='utf-8') for p in allfiles}
for typ in ['configured_feature','placed_feature']:
 files=list((base/'tfcflorae'/'worldgen'/typ).rglob('*.json'))
 ids=['tfcflorae:'+p.relative_to(base/'tfcflorae'/'worldgen'/typ).with_suffix('').as_posix() for p in files]
 refs={i:[] for i in ids}
 # exact string search per file
 for p,t in texts.items():
  for i in ids:
   if '"'+i+'"' in t: refs[i].append(p.relative_to(base).as_posix())
 print(typ,'total',len(ids),'unreferenced',sum(not v for v in refs.values()))
 print('unreferenced ids:', [i for i,v in refs.items() if not v][:100])
 from collections import Counter
 print('bins',Counter(len(v) for v in refs.values()))
 # refs by top category
 c=Counter()
 for v in refs.values():
  for x in v:
   c[x.split('/')[0:2][0] if x else 'none']+=1
 print('ref file namespaces',c)
