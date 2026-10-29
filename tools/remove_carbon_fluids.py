import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
RES = os.path.join(ROOT, "src", "main", "resources")

FLUID_IDS = ["anthracite", "bituminous_coal", "charcoal", "coke", "lignite"]

# 1) stop registering the fluids
p = os.path.join(JAVA, "common", "fluid", "TFCFFluids.java")
s = io.open(p, encoding="utf-8").read()
start = s.index("public static final Map<SimpleFluids, FluidRegistryObject<ForgeFlowingFluid>> SIMPLE_FLUIDS = Helpers.mapOfKeys(")
end = s.index("    public static int dyeColorToInt", start)
s = s[:start] + (
    "public static final Map<SimpleFluids, FluidRegistryObject<ForgeFlowingFluid>> SIMPLE_FLUIDS = new java.util.HashMap<>();\n\n"
) + s[end:]
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("TFCFFluids: SIMPLE_FLUIDS emptied")

# 2) stop registering their liquid blocks
p = os.path.join(JAVA, "common", "blocks", "TFCFBlocks.java")
s = io.open(p, encoding="utf-8").read()
start = s.index("public static final Map<SimpleFluids, RegistryObject<LiquidBlock>> SIMPLE_FLUIDS = Helpers.mapOfKeys(")
end = s.index("\n   );", start) + len("\n   );")
s = s[:start] + (
    "public static final Map<SimpleFluids, RegistryObject<LiquidBlock>> SIMPLE_FLUIDS = new HashMap<>();"
) + s[end:]
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("TFCFBlocks: liquid blocks emptied")

# 3) drop the tag file that only listed those fluids
tag = os.path.join(RES, "data", "tfc", "tags", "blocks", "all_fluids.json")
if os.path.isfile(tag):
    os.remove(tag)
    print("removed", os.path.relpath(tag, ROOT))

# 4) remove the assets
removed = 0
for sub in ("blockstates/fluid/ore", "models/item/fluid/ore", "models/block/fluid/ore", "textures/fluid/ore"):
    d = os.path.join(RES, "assets", "tfcflorae", *sub.split("/"))
    if os.path.isdir(d):
        n = len(os.listdir(d))
        shutil.rmtree(d)
        removed += n
        print("removed dir %s (%d files)" % (sub, n))
print("asset files removed:", removed)

# 5) strip lang entries
for lang in ("en_us.json", "zh_cn.json"):
    lp = os.path.join(RES, "assets", "tfcflorae", "lang", lang)
    if not os.path.isfile(lp):
        continue
    d = json.load(io.open(lp, encoding="utf-8-sig"))
    before = len(d)
    for k in list(d):
        if k.startswith("fluid.tfcflorae.ore.") or k.startswith("block.tfcflorae.fluid.ore."):
            del d[k]
    if len(d) != before:
        io.open(lp, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: removed %d lang keys" % (lang, before - len(d)))
