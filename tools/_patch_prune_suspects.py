import io

p = "tools/prune_dead_tag_ids.py"
s = io.open(p, encoding="utf-8").read()

old = '''def have(ident, kind):
    if ":" not in ident:
        return True  # no namespace = minecraft default, not ours to judge
    ns, path = ident.split(":", 1)
    if ns != "tfcflorae":
        return True  # not ours to judge
    if path.split("/")[0] in REMOVED_PREFIXES or path in REMOVED_EXACT:
        return False'''
new = '''# ids with assets but no registration (family-consistency detection)
SUSPECTS_FILE = os.path.join(ROOT, "reports", "suspect-unregistered.json")
try:
    UNREGISTERED = set(json.load(open(SUSPECTS_FILE, encoding="utf-8-sig")))
except Exception:
    UNREGISTERED = set()


def have(ident, kind):
    if ":" not in ident:
        return True  # no namespace = minecraft default, not ours to judge
    ns, path = ident.split(":", 1)
    if ns != "tfcflorae":
        return True  # not ours to judge
    if path in UNREGISTERED:
        return False
    if path.split("/")[0] in REMOVED_PREFIXES or path in REMOVED_EXACT:
        return False'''
assert old in s, "have() anchor not found"
s = s.replace(old, new, 1)

# json import may be missing
if "\nimport json\n" not in s:
    s = s.replace("import argparse", "import argparse\nimport json", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("prune patched with UNREGISTERED")
