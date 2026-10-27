from pathlib import Path
from collections import Counter,defaultdict
base=Path('src/main/resources/data'); files=list((base/'tfcflorae/worldgen/placed_feature').rglob('*.json')); ids=['tfcflorae:'+p.relative_to(base/'tfcflorae/worldgen/placed_feature').with_suffix('').as_posix() for p in files]; refs=defaultdict(list)
for p in base.rglob('*.json'):
 rel=p.relative_to(base).as_posix()
 if rel.startswith('tfcflorae/worldgen/placed_feature/'):continue
 t=p.read_text(encoding='utf8')
 for i in ids:
  if '"'+i+'"' in t:refs[i].append(rel)
for cat,pred in [('vein',lambda i:i.startswith('tfcflorae:vein/')),('tree',lambda i:i.startswith('tfcflorae:tree/'))]:
 vals=[i for i in ids if pred(i)]
 print(cat,len(vals),'referenced',sum(bool(refs[i]) for i in vals),'orphan',sum(not refs[i] for i in vals))
 print('orphans',[i for i in vals if not refs[i]])
 print('ref locations',Counter(v for i in vals for v in refs[i] if '/tags/' in v))
