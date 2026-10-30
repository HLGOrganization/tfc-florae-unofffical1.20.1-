"""Remove the enum constants for the woods and rocks being dropped.

Woods: YELLOW_JACARANDA, WHITE_MANGROVE, WHITE_CHERRY_BLOSSOM, WHITEBEAM,
       KAURI, HAZEL, BRAZILWOOD, BEECH
Rocks: ARKOSE, CATACLASITE, SANDSTONE

Enum constants are multi-line (some carry Lifecycle arrays and berry suppliers), so each
one is located by name and cut with paren matching, together with its trailing comma.
Deletion runs back-to-front so earlier offsets stay valid.

TFCFRock also has a name->enum switch (`case "arkose":`); those cases are removed too.
"""
import io
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WOODS = ["YELLOW_JACARANDA", "WHITE_MANGROVE", "WHITE_CHERRY_BLOSSOM", "WHITEBEAM",
         "KAURI", "HAZEL", "BRAZILWOOD", "BEECH"]
ROCKS = ["ARKOSE", "CATACLASITE", "SANDSTONE"]


def cut_constants(text, names, label):
    """Remove `<NAME>(...)` constants including the trailing comma."""
    removed = []
    for name in names:
        while True:
            m = re.search(r'(?<![\w])' + re.escape(name) + r'\s*\(', text)
            if not m:
                break
            # make sure this is a constant at statement level (preceded by newline + spaces,
            # possibly after a previous constant's comma)
            line_start = text.rfind("\n", 0, m.start()) + 1
            prefix = text[line_start:m.start()]
            if prefix.strip():
                break                     # it is a use, not a declaration
            paren = text.index("(", m.start())
            depth = 0
            i = paren
            in_str = False
            esc = False
            while i < len(text):
                c = text[i]
                if in_str:
                    if esc:
                        esc = False
                    elif c == "\\":
                        esc = True
                    elif c == '"':
                        in_str = False
                else:
                    if c == '"':
                        in_str = True
                    elif c == "(":
                        depth += 1
                    elif c == ")":
                        depth -= 1
                        if depth == 0:
                            break
                i += 1
            end = i + 1
            # swallow trailing comma / whitespace up to end of line
            j = end
            while j < len(text) and text[j] in " \t":
                j += 1
            if j < len(text) and text[j] == ",":
                j += 1
            while j < len(text) and text[j] in " \t":
                j += 1
            if j < len(text) and text[j] == "\n":
                j += 1
            seg = text[m.start():j]
            removed.append((name, seg.count("\n") + 1))
            text = text[:line_start] + text[j:]
    print("%s: removed %d constants" % (label, len(removed)))
    for n, l in removed:
        print("    %-24s %d lines" % (n, l))
    return text


# ---- TFCFWood ------------------------------------------------------------
p = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "wood", "TFCFWood.java")
s = io.open(p, encoding="utf-8").read()
before = s
s = cut_constants(s, WOODS, "TFCFWood")
if s != before:
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)

# ---- TFCFRock ------------------------------------------------------------
p = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "rock", "TFCFRock.java")
s = io.open(p, encoding="utf-8").read()
before = s
s = cut_constants(s, ROCKS, "TFCFRock")
# remove `case "arkose":` style cases together with their return statement
for name in ("arkose", "cataclasite", "sandstone"):
    pat = re.compile(r'[ \t]*case "' + name + r'":[ \t]*\n(?:[ \t]*return[^\n]*\n)?', re.M)
    s, n = pat.subn("", s)
    print('case "%s": removed %d' % (name, n))
if s != before:
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)

# ---- verify ------------------------------------------------------------
for path, names in ((os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "wood", "TFCFWood.java"), WOODS),
                    (os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "rock", "TFCFRock.java"), ROCKS)):
    t = io.open(path, encoding="utf-8").read()
    left = [n for n in names if re.search(r'(?<![\w])' + n + r'\s*\(', t)]
    print("%-16s leftovers: %s" % (os.path.basename(path), left if left else "none"))
