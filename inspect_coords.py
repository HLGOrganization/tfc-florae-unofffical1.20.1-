import sys
sys.path.insert(0,'D:/学习资料/附属模组工作区')
import _tmp_parse_nbt as p
f='src/main/resources/data/tfcflorae/structures/common_oak/1.nbt'; t=p.parse(f)
print('size',t['size'],'palette',len(t['palette']),'blocks',len(t['blocks']))
print('first',t['blocks'][:20]); print('last',t['blocks'][-10:])
