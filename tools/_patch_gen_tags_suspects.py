import io

p = "tools/gen_tags.py"
s = io.open(p, encoding="utf-8").read()

# 1) load the suspect list next to the whitelist
anchor = 'WHITELIST = os.path.join(ROOT, "reports", "registered-ids-full.json")'
add = '''WHITELIST = os.path.join(ROOT, "reports", "registered-ids-full.json")
# ids that have an asset on disk but were never registered (detected by family
# consistency in tools/find_unregistered_assets.py).  Putting one of these in a tag
# kills the whole tag - sandstone/layered/slab/gray did exactly that to
# minecraft:slabs.
SUSPECTS_FILE = os.path.join(ROOT, "reports", "suspect-unregistered.json")
try:
    UNREGISTERED = set(json.load(io.open(SUSPECTS_FILE, encoding="utf-8-sig")))
except Exception:
    UNREGISTERED = set()'''
assert anchor in s
s = s.replace(anchor, add, 1)

# io is not imported in gen_tags - it uses json/open; switch to open()
s = s.replace('json.load(io.open(SUSPECTS_FILE, encoding="utf-8-sig"))',
              'json.load(open(SUSPECTS_FILE, encoding="utf-8-sig"))')

# 2) drop suspects from the whitelist
old = '''def load_whitelist():
    d = json.load(open(WHITELIST, encoding="utf-8"))
    blocks = {b for b in d["blocks"]
              if b.split("/")[0] not in REMOVED_SOILS and b not in REMOVED_IDS}
    items = {i for i in d["items"]
             if i.split("/")[0] not in REMOVED_SOILS and i not in REMOVED_IDS}
    return blocks, items'''
new = '''def load_whitelist():
    d = json.load(open(WHITELIST, encoding="utf-8"))
    blocks = {b for b in d["blocks"]
              if b.split("/")[0] not in REMOVED_SOILS and b not in REMOVED_IDS
              and b not in UNREGISTERED}
    items = {i for i in d["items"]
             if i.split("/")[0] not in REMOVED_SOILS and i not in REMOVED_IDS
             and i not in UNREGISTERED}
    return blocks, items'''
assert old in s
s = s.replace(old, new, 1)

# 3) asset fallback must not rescue a suspect
old2 = '''    rel = our_id.replace("/", os.sep) + ".json"
    if os.path.isfile(os.path.join(ASSETS, "blockstates", rel)):
        return True
    return os.path.isfile(os.path.join(ASSETS, "models", "item", rel))'''
new2 = '''    if our_id in UNREGISTERED:
        return False
    rel = our_id.replace("/", os.sep) + ".json"
    if os.path.isfile(os.path.join(ASSETS, "blockstates", rel)):
        return True
    return os.path.isfile(os.path.join(ASSETS, "models", "item", rel))'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("gen_tags patched: UNREGISTERED list is now excluded everywhere")
