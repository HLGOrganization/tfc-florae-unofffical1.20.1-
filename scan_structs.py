import os,sys,json
sys.path.insert(0,'D:/学习资料/附属模组工作区')
import _tmp_parse_nbt as p
root='src/main/resources/data/tfcflorae/structures'
rows=[]
for dp,_,fs in os.walk(root):
 for fn in fs:
  if not fn.endswith('.nbt'): continue
  path=os.path.join(dp,fn)
  try:
   t=p.parse(path); pal=t.get('palette',[]); names=[x.get('Name','') for x in pal]; counts={}
   for b in t.get('blocks',[]):
    n=names[b.get('state',0)]; counts[n]=counts.get(n,0)+1
   wood=sum(v for k,v in counts.items() if ':wood/log/' in k or ':wood/wood/' in k)
   leaves=sum(v for k,v in counts.items() if ':wood/leaves/' in k)
   rows.append((wood,leaves,os.path.relpath(path,root)))
  except Exception as e: print('ERR',path,e)
rows.sort()
print('files',len(rows),'wood min/max',rows[0][0],rows[-1][0])
for x in rows[:150]: print(x)
