"""Read-only registry <-> asset audit for TFC Florae (source expander).

The mod registers most blocks/items through helper loops
(`Helpers.mapOfKeys(<Enum>.class, ...)`, `for (T : <Enum>.values())` mappers).
This tool expands those loops from the Java source so every produced registry id
can be checked against the resources the client needs:

    blockstate    assets/tfcflorae/blockstates/<path>.json   -> block rendering
    block model   assets/tfcflorae/models/block/<path>.json  -> block rendering
    item model    assets/tfcflorae/models/item/<path>.json   -> inventory / in hand
    lang          assets/tfcflorae/lang/{zh_cn,en_us}.json

Classification
    A = registered, but neither lang nor any model  -> dead registration (purple/black)
    B = has a model, missing lang                   -> needs a translation
    C = has lang, missing model                     -> needs a model

An independent source of truth is used to keep the report honest:
`reports/runtime-registries.json` is a real Forge registry dump produced by the
`_audit` harness, so ids present there are provably registered.  The report keeps
expander-only / dump-only differences visible instead of silently trusting either
side.

Usage
    python tools/audit_register_assets.py
    python tools/audit_register_assets.py --debug-site TFCFBlocks.java:333
    python tools/audit_register_assets.py --list A --limit 40
"""
from __future__ import annotations

import argparse
import bisect
import collections
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA_ROOT = os.path.join(ROOT, "src", "main", "java")
ASSET_ROOT = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
DUMP_PATH = os.path.join(ROOT, "reports", "runtime-registries.json")
TFC_SRC = os.path.normpath(os.path.join(ROOT, "..", "_refs", "tfc", "src", "main", "java"))

NS = "tfcflorae"
TARGET_FILES = ("TFCFBlocks.java", "TFCFItems.java")

VANILLA_ENUMS = {
    "DyeColor": ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                 "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"],
}

# ---------------------------------------------------------------------------
# Java text handling
# ---------------------------------------------------------------------------

STR_RE = re.compile(r'"(?:[^"\\]|\\.)*"')
CHAR_RE = re.compile(r"'(?:[^'\\]|\\.)'")
OPEN, CLOSE = "([{", ")]}"


def strip_comments(text: str) -> str:
    """Replace comments with spaces (offsets are preserved)."""
    out = []
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        if c == '"':
            j = i + 1
            while j < n:
                if text[j] == "\\":
                    j += 2
                    continue
                if text[j] == '"':
                    j += 1
                    break
                j += 1
            out.append(text[i:j])
            i = j
        elif c == "'":
            j = i + 1
            while j < n and text[j] != "'":
                j += 2 if text[j] == "\\" else 1
            j = min(j + 1, n)
            out.append(text[i:j])
            i = j
        elif text.startswith("//", i):
            while i < n and text[i] != "\n":
                out.append(" ")
                i += 1
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            end = n if end < 0 else end + 2
            chunk = text[i:end]
            out.append("".join("\n" if ch == "\n" else " " for ch in chunk))
            i = end
        else:
            out.append(c)
            i += 1
    return "".join(out)


class JavaFile:
    __slots__ = ("path", "name", "text", "mask", "_stmt", "_chain", "_lines",
                 "pair_opens", "pairs", "close_of", "_lambdas")

    def __init__(self, path: str):
        self.path = path
        self.name = os.path.basename(path)
        self.text = strip_comments(open(path, encoding="utf-8", errors="ignore").read())
        mask = bytearray(len(self.text))
        for rx in (STR_RE, CHAR_RE):
            for m in rx.finditer(self.text):
                mask[m.start() : m.end()] = b"\x01" * (m.end() - m.start())
        self.mask = mask
        self._stmt = {}
        self._chain = {}
        self._lines = None
        self._lambdas = None
        self._build_parens()

    def _build_parens(self):
        stack = []
        pairs = []
        text, mask = self.text, self.mask
        for i, ch in enumerate(text):
            if mask[i]:
                continue
            if ch == "(":
                stack.append(i)
            elif ch == ")" and stack:
                pairs.append((stack.pop(), i))
        pairs.sort(key=lambda p: p[0])
        self.pairs = pairs
        self.pair_opens = [p[0] for p in pairs]
        self.close_of = {op: cl for op, cl in pairs}

    def in_str(self, i: int) -> bool:
        return self.mask[i] == 1

    def line_of(self, pos: int) -> int:
        if self._lines is None:
            self._lines = [i for i, ch in enumerate(self.text) if ch == "\n"]
        return bisect.bisect_left(self._lines, pos) + 1

    def match_bracket(self, open_idx: int) -> int:
        if self.text[open_idx] == "(":
            cl = self.close_of.get(open_idx)
            if cl is not None:
                return cl
        depth = 0
        text, mask = self.text, self.mask
        i, n = open_idx, len(text)
        while i < n:
            if mask[i]:
                i += 1
                continue
            c = text[i]
            if c in OPEN:
                depth += 1
            elif c in CLOSE:
                depth -= 1
                if depth == 0:
                    return i
            i += 1
        return n

    def enclosing_open_paren(self, pos: int) -> int:
        """Innermost '(' whose matching ')' lies after pos."""
        i = bisect.bisect_left(self.pair_opens, pos) - 1
        while i >= 0:
            op, cl = self.pairs[i]
            if cl > pos:
                return op
            i -= 1
        return -1

    def lambdas(self):
        """[(arrow_pos, call_close, param, call_open)] for single-parameter lambdas."""
        if self._lambdas is not None:
            return self._lambdas
        out = []
        for lm in LAMBDA_RE.finditer(self.text):
            if lm.group(1):
                continue
            arrow = lm.start() + lm.group(0).rfind("->")
            op = self.enclosing_open_paren(arrow)
            if op < 0:
                continue
            cl = self.close_of.get(op)
            if cl is None or not (op < arrow < cl):
                continue
            out.append((arrow, cl, lm.group(2), op))
        self._lambdas = out
        return out

    def callee_before(self, open_paren: int) -> str:
        j = open_paren - 1
        while j >= 0 and (self.text[j].isalnum() or self.text[j] in "_.$"):
            j -= 1
        return self.text[j + 1 : open_paren]

    def statement_bounds(self, pos: int):
        """[start, end) of the Java statement containing pos.

        Left: walk back over balanced brackets until a statement terminator.
        Right: same.  Brackets are balanced so an enclosing call such as
        `Map.put(k, register(...))` or a field initialiser stays inside the span.
        """
        if pos in self._stmt:
            return self._stmt[pos]
        text, mask = self.text, self.mask
        depth = 0
        i = pos
        start = 0
        while i > 0:
            i -= 1
            if mask[i]:
                continue
            c = text[i]
            if c in ")]":
                depth += 1
            elif c in "([":
                if depth > 0:
                    depth -= 1
            elif c in ";{}" and depth == 0:
                if c == "{":
                    # a lambda body (`-> {`) is not a statement boundary
                    k = i - 1
                    while k >= 0 and text[k] in " \t\r\n":
                        k -= 1
                    if k >= 1 and text[k] == ">" and text[k - 1] == "-":
                        continue
                start = i + 1
                break
        depth = 0
        j = pos
        end = len(text)
        while j < len(text):
            if mask[j]:
                j += 1
                continue
            c = text[j]
            if c in "([":
                depth += 1
            elif c in ")]":
                if depth == 0:
                    end = j
                    break
                depth -= 1
            elif c in ";{}" and depth == 0:
                end = j
                break
            j += 1
        self._stmt[pos] = (start, end)
        return start, end

    def block_chain(self, pos: int):
        """[(header_text, brace_idx)] innermost -> outermost."""
        if pos in self._chain:
            return self._chain[pos]
        text, mask = self.text, self.mask
        out = []
        depth = 0
        i = pos
        while i > 0:
            i -= 1
            if mask[i]:
                continue
            c = text[i]
            if c in ")]":
                depth += 1
            elif c in "([":
                if depth > 0:
                    depth -= 1
            elif c == "}":
                depth += 1
            elif c == "{":
                if depth > 0:
                    depth -= 1
                    continue
                j = i - 1
                hdepth = 0
                start = 0
                while j >= 0:
                    if mask[j]:
                        j -= 1
                        continue
                    d = text[j]
                    if d in ")]":
                        hdepth += 1
                    elif d in "([":
                        if hdepth > 0:
                            hdepth -= 1
                    elif d in ";{}" and hdepth == 0:
                        start = j + 1
                        break
                    j -= 1
                out.append((text[start:i], i))
        self._chain[pos] = out
        return out


def split_top(text: str, sep: str = ",", angle: bool = False):
    """Split `text` on `sep` at bracket depth 0.

    With `angle=True` angle brackets also open a level, which is required when
    splitting Java parameter lists holding generics such as
    `BiFunction<TFCFSoil, TFCFSoil.TFCFVariant, Block>`.
    """
    parts, depth, cur = [], 0, []
    i, n = 0, len(text)
    in_str = False
    while i < n:
        c = text[i]
        if in_str:
            cur.append(c)
            if c == "\\" and i + 1 < n:
                cur.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
            cur.append(c)
            i += 1
            continue
        if c in OPEN or (angle and c == "<"):
            depth += 1
        elif c in CLOSE or (angle and c == ">"):
            depth -= 1
        if depth == 0 and text.startswith(sep, i):
            parts.append("".join(cur))
            cur = []
            i += len(sep)
            continue
        cur.append(c)
        i += 1
    parts.append("".join(cur))
    return parts


def match_bracket_sub(text: str, open_idx: int) -> int:
    depth = 0
    in_str = False
    i, n = open_idx, len(text)
    while i < n:
        c = text[i]
        if in_str:
            if c == "\\":
                i += 1
            elif c == '"':
                in_str = False
            i += 1
            continue
        if c == '"':
            in_str = True
        elif c in OPEN:
            depth += 1
        elif c in CLOSE:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return n


def norm(s: str) -> str:
    return " ".join(s.split())


def call_args(text: str, open_paren: int):
    cl = match_bracket_sub(text, open_paren)
    return [a.strip() for a in split_top(text[open_paren + 1 : cl], ",")]


# ---------------------------------------------------------------------------
# Enum index
# ---------------------------------------------------------------------------

IDENT_RE = re.compile(r"[A-Za-z_$][A-Za-z0-9_$]*")
ENUM_RE = re.compile(r"\benum\s+(\w+)")


class EnumInfo:
    __slots__ = ("name", "fqname", "constants", "args", "field_index", "field_default", "source")

    def __init__(self, name, fqname):
        self.name = name
        self.fqname = fqname
        self.constants = []
        self.args = {}
        self.field_index = {}
        self.field_default = {}
        self.source = ""


ENUMS_BY_FQ: dict[str, EnumInfo] = {}
ENUMS_BY_SHORT: dict[str, list[EnumInfo]] = collections.defaultdict(list)


def parse_enum_body(enum_name: str, fqname: str, body: str) -> EnumInfo:
    info = EnumInfo(enum_name, fqname)
    depth = 0
    semi = -1
    i, n = 0, len(body)
    in_str = False
    while i < n:
        c = body[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == '"':
                in_str = False
        elif c == '"':
            in_str = True
        elif c in OPEN:
            depth += 1
        elif c in CLOSE:
            depth -= 1
        elif c == ";" and depth == 0:
            semi = i
            break
        i += 1
    section = body[: semi if semi >= 0 else len(body)]
    for raw in split_top(section, ","):
        s = raw.strip()
        if not s:
            continue
        s = re.sub(r"^@[\w.]+(\([^)]*\))?\s*", "", s)
        m = IDENT_RE.match(s)
        if not m:
            continue
        name = m.group(0)
        if name in ("public", "private", "protected", "static", "final"):
            continue
        info.constants.append(name)
        rest = s[m.end() :].lstrip()
        info.args[name] = []
        if rest.startswith("("):
            close = match_bracket_sub(rest, 0)
            info.args[name] = [a.strip() for a in split_top(rest[1:close], ",")]
    if semi >= 0:
        rest = body[semi + 1 :]
        cm = re.search(r"(?<![\w.])" + re.escape(enum_name) + r"\s*\(", rest)
        if cm:
            op = cm.end() - 1
            close = match_bracket_sub(rest, op)
            for idx, p in enumerate(split_top(rest[op + 1 : close], ",", angle=True)):
                toks = [t for t in re.split(r"[\s<>\[\],.?]+", p.strip()) if t]
                toks = [t for t in toks if t not in ("final",)]
                if toks:
                    info.field_index[toks[-1]] = idx
        for fm in re.finditer(
            r"(?:private|public|protected)\s+(?:static\s+)?(?:final\s+)?(boolean|Boolean|int|String)\s+(\w+)\s*=\s*([^;]+);",
            rest,
        ):
            info.field_default[fm.group(2)] = fm.group(3).strip()
    return info


def index_enums():
    for root in (JAVA_ROOT, TFC_SRC):
        if not os.path.isdir(root):
            continue
        for dirpath, _d, files in os.walk(root):
            for fn in files:
                if not fn.endswith(".java"):
                    continue
                p = os.path.join(dirpath, fn)
                try:
                    raw = open(p, encoding="utf-8", errors="ignore").read()
                except Exception:
                    continue
                if "enum " not in raw:
                    continue
                text = strip_comments(raw)
                pkg = ""
                pm = re.search(r"^package\s+([\w.]+);", text, re.M)
                if pm:
                    pkg = pm.group(1)
                # find enum declarations and their nesting chain
                for m in ENUM_RE.finditer(text):
                    name = m.group(1)
                    op = text.find("{", m.end())
                    if op < 0:
                        continue
                    # guard against a stray match far away
                    if text[m.end() : op].strip(" \t\r\n implements") and len(text[m.end() : op]) > 200:
                        continue
                    cl = match_bracket_sub(text, op)
                    body = text[op + 1 : cl]
                    if not body.strip():
                        continue
                    # nesting: nearest enclosing enum declared before this one
                    outer = None
                    for m2 in ENUM_RE.finditer(text, 0, m.start()):
                        op2 = text.find("{", m2.end())
                        if op2 >= 0 and match_bracket_sub(text, op2) > m.start():
                            outer = m2.group(1)
                    fq = (outer + ".") if outer else ""
                    fqname = (pkg + "." if pkg else "") + re.sub(r"\.[^.]+$", "", fq) + name
                    fqname = fqname.replace("..", ".")
                    info = parse_enum_body(name, fqname, body)
                    if not info.constants:
                        continue
                    info.source = text
                    ENUMS_BY_FQ[fqname] = info
                    ENUMS_BY_SHORT[name].append(info)


def resolve_enum(type_text: str, java: JavaFile | None):
    """Resolve a written type name to an EnumInfo using imports + suffix matching."""
    if not type_text:
        return None
    t = type_text.strip()
    if t in ENUMS_BY_SHORT and len(ENUMS_BY_SHORT[t]) == 1:
        return ENUMS_BY_SHORT[t][0]
    if java is not None:
        fq = JAVA_IMPORTS.get(java.path, {}).get(t.split(".")[0])
        if fq:
            cand = fq + "." + t if "." in t else fq
            if cand in ENUMS_BY_FQ:
                return ENUMS_BY_FQ[cand]
    # suffix match on the dotted name
    best = None
    for fqname, info in ENUMS_BY_FQ.items():
        if fqname.endswith("." + t) or fqname == t:
            if best is None or len(fqname) < len(best.fqname):
                best = info
    if best:
        return best
    cands = ENUMS_BY_SHORT.get(t.split(".")[-1], [])
    return cands[0] if len(cands) == 1 else None


JAVA_IMPORTS: dict[str, dict[str, str]] = {}


def collect_imports(paths):
    for p in paths:
        if p in JAVA_IMPORTS:
            continue
        text = open(p, encoding="utf-8", errors="ignore").read()
        d = {}
        for m in re.finditer(r"^import\s+(?:static\s+)?([\w.$]+);", text, re.M):
            fq = m.group(1)
            d[fq.split(".")[-1]] = fq
        JAVA_IMPORTS[p] = d


# ---------------------------------------------------------------------------
# Enum property evaluation
# ---------------------------------------------------------------------------

GETTER_ALIAS = {"isFruitTree": "isFruitTree"}
BOOL_METHODS = {
    "hasRock", "hasMold", "hasSandNew", "hasLeavesOnly", "isMangrove", "isJoshua",
    "hasFruitingLog", "isFruitTree", "hasVariants", "isSeasonalFruitPlant", "isGraded",
    "hasTFCFactory", "hasTFCFFactory",
}
METHOD_FIELD_CACHE: dict[tuple, str | None] = {}


def method_body_text(info: EnumInfo, method: str):
    key = ("body", info.fqname, method)
    if key in METHOD_FIELD_CACHE:
        return METHOD_FIELD_CACHE[key]
    body = None
    m = re.search(
        r"(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:boolean|Boolean|int|String|BiFunction|Function)"
        r"[\w<>,\s.]*\s+" + re.escape(method) + r"\s*\([^)]*\)\s*\{",
        info.source,
    )
    if m:
        op = m.end() - 1
        body = info.source[op + 1 : match_bracket_sub(info.source, op)]
    METHOD_FIELD_CACHE[key] = body
    return body


def raw_arg(info: EnumInfo, const: str, field: str):
    """Constructor argument text for `field` of one constant (None if unknown)."""
    idx = info.field_index.get(field)
    args = info.args.get(const)
    if idx is not None and args is not None:
        return args[idx].strip() if idx < len(args) else "null"
    return None


def method_field(info: EnumInfo, method: str):
    key = (info.fqname, method)
    if key in METHOD_FIELD_CACHE:
        return METHOD_FIELD_CACHE[key]
    field = None
    body = method_body_text(info, method)
    if body:
        for rm in re.finditer(r"return\s+(?:this\.)?(\w+)\s*;", body):
            cand = rm.group(1)
            if cand != "null":
                field = cand
                break
    METHOD_FIELD_CACHE[key] = field
    return field


def const_bool(info: EnumInfo, const: str, method: str):
    field = method_field(info, method)
    if field is None:
        # getters such as `return this.type == BlockType.STANDARD || ...;`
        return expression_bool(info, const, method)
    if field == "this":
        field = method
    idx = info.field_index.get(field)
    args = info.args.get(const)
    if idx is not None and args is not None:
        if idx < len(args):
            lit = args[idx].strip()
            if lit in ("true", "false"):
                return lit == "true"
            return None
        # omitted constructor argument -> Java default (false for primitives)
        return False
    default = info.field_default.get(field)
    if default in ("true", "false"):
        return default == "true"
    return None


def expression_bool(info: EnumInfo, const: str, method: str):
    """Evaluate a getter body such as `return this.type == X || this.type == Y;`."""
    body = method_body_text(info, method)
    if not body:
        return None
    rm = re.search(r"return\s+(.+?);", body, re.S)
    if not rm:
        return None
    expr = norm(rm.group(1))
    missing = []

    def repl(mm):
        arg = raw_arg(info, const, mm.group(1))
        if arg is None:
            missing.append(mm.group(1))
            return mm.group(0)
        return arg

    expr = re.sub(r"this\.(\w+)", repl, expr)
    if missing:
        return None
    if "this." in expr:
        return None
    return eval_cond(expr, {}, {})


FACTORY_NULLS: dict[tuple, set] = {}


def factory_null_constants(info: EnumInfo, method: str):
    key = (info.fqname, method)
    if key in FACTORY_NULLS:
        return FACTORY_NULLS[key]
    out = set()
    m = re.search(
        r"(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:boolean|Boolean|int|String|BiFunction|Function)"
        r"[\w<>,\s.]*\s+" + re.escape(method) + r"\s*\([^)]*\)\s*\{",
        info.source,
    )
    if m:
        op = m.end() - 1
        body = info.source[op + 1 : match_bracket_sub(info.source, op)]
        for im in re.finditer(r"if\s*\(([^)]*)\)\s*\{?\s*return\s+null\s*;", body):
            cond = im.group(1)
            for cm in re.finditer(r"this\s*==\s*([\w.]+)", cond):
                out.add(cm.group(1).split(".")[-1])
            for cm in re.finditer(r"([A-Z][A-Z0-9_]{2,})\s*==\s*this", cond):
                out.add(cm.group(1))
    FACTORY_NULLS[key] = out
    return out


# ---------------------------------------------------------------------------
# Condition / expression evaluation
# ---------------------------------------------------------------------------


def eval_name_expr(expr: str, values: dict, types: dict, depth: int = 0):
    """Evaluate a registry-name expression to a string, or None if not resolvable."""
    if depth > 6:
        return None
    e = norm(expr)
    low = False
    for suf in (".toLowerCase(Locale.ROOT)", ".toLowerCase(java.util.Locale.ROOT)"):
        if e.endswith(suf):
            e = e[: -len(suf)].strip()
            low = True
            break
    if e.startswith("(") and match_bracket_sub(e, 0) == len(e) - 1:
        e = e[1:-1].strip()
    out = []
    for raw in split_top(e, "+"):
        p = raw.strip()
        if not p:
            continue
        m = re.fullmatch(r'"((?:[^"\\]|\\.)*)"', p)
        if m:
            out.append(m.group(1))
            continue
        m = re.fullmatch(r"(\w+)\.name\(\)", p)
        if m and m.group(1) in values:
            out.append(values[m.group(1)])
            continue
        m = re.fullmatch(r"(\w+)\.getSerializedName\(\)(?:\.toLowerCase\(Locale\.ROOT\))?", p)
        if m and m.group(1) in values:
            out.append(values[m.group(1)].lower())
            continue
        if p in values:
            out.append(values[p])
            continue
        if "(" in p or "." in p:
            sub = eval_name_expr(p, values, types, depth + 1)
            if sub is not None:
                out.append(sub)
                continue
        return None
    s = "".join(out)
    return s.lower() if low else s


def const_arg_text(info: EnumInfo, const: str, method: str):
    """Raw constructor argument text backing `method()` for one enum constant."""
    field = method_field(info, method) or method
    idx = info.field_index.get(field)
    args = info.args.get(const)
    if idx is not None and args is not None:
        if idx < len(args):
            return args[idx].strip()
        # omitted constructor argument -> Java default (null for object types)
        return "null"
    return None


def eval_cond(cond: str, values: dict, types: dict):
    """True / False / None."""
    c = norm(cond)
    if not c:
        return None
    while c.startswith("(") and match_bracket_sub(c, 0) == len(c) - 1:
        c = norm(c[1:-1])
    if c == "true":
        return True
    if c == "false":
        return False
    parts = split_top(c, "||")
    if len(parts) > 1:
        vals = [eval_cond(p, values, types) for p in parts]
        if any(v is True for v in vals):
            return True
        return None if any(v is None for v in vals) else False
    parts = split_top(c, "&&")
    if len(parts) > 1:
        vals = [eval_cond(p, values, types) for p in parts]
        if any(v is False for v in vals):
            return False
        return None if any(v is None for v in vals) else True
    if c.startswith("!"):
        v = eval_cond(c[1:], values, types)
        return None if v is None else (not v)
    # null guards must be checked before the generic property comparison,
    # otherwise `x.getFactory() != null` is parsed as a property comparison.
    m = re.fullmatch(r"(\w+)\.(\w+)\(\)\s*(==|!=)\s*null", c)
    if m and m.group(1) in values:
        var = m.group(1)
        info = resolve_enum(types.get(var, ""), None)
        if info is not None:
            meth = m.group(2)
            is_null = None
            # decompiled guards: `if (this == X || this == Y) return null;`
            nulls = factory_null_constants(info, meth)
            if values[var] in nulls:
                is_null = True
            else:
                arg = const_arg_text(info, values[var], meth)
                if arg is not None:
                    is_null = norm(arg) == "null"
            if is_null is None:
                return None
            return is_null if m.group(3) == "==" else (not is_null)
        return None
    # enum property comparison: rock.category() == RockCategory.IGNEOUS_X
    m = re.fullmatch(r"(\w+)\.(\w+)\(\)\s*(==|!=)\s*([\w.$]+)", c)
    if m and m.group(1) in values:
        var, meth, op, rhs = m.group(1), m.group(2), m.group(3), m.group(4)
        info = resolve_enum(types.get(var, ""), None)
        if info is not None:
            arg = const_arg_text(info, values[var], meth)
            if arg is not None:
                is_eq = arg.split(".")[-1].strip() == rhs.split(".")[-1].strip()
                return is_eq if op == "==" else (not is_eq)
        return None
    m = re.fullmatch(r"(\w+)\s*(==|!=)\s*([\w.$]+)", c)
    if m and m.group(1) in values:
        is_eq = values[m.group(1)] == m.group(3).split(".")[-1]
        return is_eq if m.group(2) == "==" else (not is_eq)
    # constant-vs-constant comparison, e.g. TFCFPlant.BlockType.DRY == ...STANDARD
    m = re.fullmatch(r"([\w.$]+)\s*(==|!=)\s*([\w.$]+)", c)
    if m and m.group(1) not in values and not m.group(1)[0].isdigit():
        is_eq = m.group(1).split(".")[-1] == m.group(3).split(".")[-1]
        return is_eq if m.group(2) == "==" else (not is_eq)
    m = re.fullmatch(r"(\w+)\.name\(\)\.contains\(\"([^\"]*)\"\)", c)
    if m and m.group(1) in values:
        return m.group(2) in values[m.group(1)]
    # project idiom: `TFCFBlocks.WOODS.get(wood) != null` is exactly
    # `!wood.hasLeavesOnly()`, because woodMapper never inserts leaves-only woods.
    m = re.fullmatch(r"TFCFBlocks\.WOODS\.get\((\w+)\)\s*(==|!=)\s*null", c)
    if m and m.group(1) in values:
        var = m.group(1)
        info = resolve_enum("TFCFWood", None)
        if info is not None:
            leaves_only = const_bool(info, values[var], "hasLeavesOnly")
            if leaves_only is None:
                return None
            is_null = leaves_only
            return is_null if m.group(2) == "==" else (not is_null)
        return None
    m = re.fullmatch(r"(\w+)\.(\w+)\(\)", c)
    if m and m.group(1) in values:
        var, meth = m.group(1), m.group(2)
        info = resolve_enum(types.get(var, ""), None)
        if info is not None:
            v = const_bool(info, values[var], meth)
            if v is not None:
                return v
        return None
    return None


def eval_pred(pred: str, info: EnumInfo, const: str):
    """Evaluate a mapOfKeys key predicate for one constant."""
    p = norm(pred)
    m = re.fullmatch(r"([\w.$]+)::(\w+)", p)
    if m:
        return const_bool(info, const, m.group(2))
    m = re.fullmatch(r"(\w+)\s*->\s*(.+)", p)
    if m:
        var, body = m.group(1), m.group(2)
        return eval_cond(body, {var: const}, {var: info.name})
    return None


# ---------------------------------------------------------------------------
# Registration expansion
# ---------------------------------------------------------------------------

REG_RE = re.compile(r"(?<![\w.$])register\s*\(")
LAMBDA_RE = re.compile(r"(?:\(\s*([\w\s,]+?)\s*\)|(\w+))\s*->")
FOR_RE = re.compile(r"\bfor\s*\(\s*([\w.$]+)\s+(\w+)\s*:\s*(.+)\)")
METHOD_SIG_RE = re.compile(r"(?:public|private|protected)\s+[\w<>\[\],.\s]*?\s+(\w+)\s*\(([^)]*)\)")


def iterable_type(expr: str):
    e = norm(expr)
    m = re.fullmatch(r"([\w.$]+)\.values\(\)", e)
    if m:
        return m.group(1)
    m = re.fullmatch(r"([\w.$]+)\.class\.getEnumConstants\(\)", e)
    if m:
        return m.group(1)
    m = re.fullmatch(r"([\w.$]+)\.getEnumConstants\(\)", e)
    if m:
        return m.group(1)
    m = re.fullmatch(r"([\w.$]+)\.VALUES", e)
    if m:
        return m.group(1)
    return None


def method_param_types(sig: str):
    out = {}
    m = re.search(r"\(([^)]*)\)", sig)
    if not m:
        return out
    for p in split_top(m.group(1), ",", angle=True):
        p = p.strip()
        mm = re.fullmatch(r"Class\s*<\s*([\w.$]+)\s*>\s+(\w+)", p)
        if mm:
            out[mm.group(2)] = mm.group(1)
            continue
        mm = re.fullmatch(r"([\w.$<>\[\],\s]+?)\s+(\w+)", p)
        if mm:
            out[mm.group(2)] = mm.group(1).strip()
    return out


def lambda_owner(jf: JavaFile, arrow_pos: int, stmt_start: int):
    op = jf.enclosing_open_paren(arrow_pos)
    if op < stmt_start:
        return None, None
    callee = jf.callee_before(op)
    cl = jf.match_bracket(op)
    if cl <= arrow_pos:
        return None, None
    args = [a.strip() for a in split_top(jf.text[op + 1 : cl], ",")]
    if not args:
        return None, None
    m = re.search(r"([\w.$]+)\.class", args[0])
    if not m:
        return None, None
    pred = None
    if callee.endswith("mapOfKeys") and len(args) >= 3:
        pred = args[1]
    return m.group(1), pred


def call_owner(jf: JavaFile, op: int):
    """(enum type text, key predicate) for the call whose '(' is at op."""
    callee = jf.callee_before(op)
    cl = jf.match_bracket(op)
    args = [a.strip() for a in split_top(jf.text[op + 1 : cl], ",")]
    if not args:
        return None, None
    m = re.search(r"([\w.$]+)\.class", args[0])
    if not m:
        return None, None
    pred = None
    if callee.endswith("mapOfKeys") and len(args) >= 3:
        pred = args[1]
    return m.group(1), pred


class Site:
    __slots__ = ("file", "line", "kind", "block_item")

    def __init__(self, file, line, kind, block_item):
        self.file = file
        self.line = line
        self.kind = kind
        self.block_item = block_item

    def key(self):
        return "%s:%d" % (self.file, self.line)


EXPLAIN = None  # set by --explain to trace one register site


def expand_site(jf: JavaFile, m: re.Match):
    """Yield (name|None, Site) combos for one register(...) call."""
    pos = m.start()
    open_paren = m.end() - 1
    args = call_args(jf.text, open_paren)
    kind = "block" if jf.name.startswith("TFCFBlocks") else "item"
    block_item = kind == "block" and len(args) >= 3 and "null" not in norm(args[2])
    site = Site(jf.name, jf.line_of(pos), kind, block_item)
    name_expr = args[0] if args else ""
    if not name_expr:
        yield None, site
        return

    s_start, s_end = jf.statement_bounds(pos)
    chain = jf.block_chain(pos)

    binds: dict[str, tuple[EnumInfo, list[str]]] = {}
    aliases: dict[str, tuple[str, bool]] = {}
    guards = []

    # --- lambdas whose owning call encloses this registration
    # (`mapOfKeys(X.class, x -> mapOfKeys(Y.class, y -> register(...)))` binds both)
    for arrow, call_close, pname, call_open in jf.lambdas():
        inside = call_open < pos < call_close
        if EXPLAIN and site.key() == EXPLAIN:
            print("   lambda %-8s arrow_line=%d inside=%s before=%s" % (pname, jf.line_of(arrow), inside, arrow <= pos))
        if not inside or arrow > pos:
            continue
        etype, pred = call_owner(jf, call_open)
        if not etype:
            continue
        info = resolve_enum(etype, jf)
        if info is None:
            continue
        allowed = list(info.constants)
        if pred:
            keep = []
            for c in info.constants:
                if eval_pred(pred, info, c) is True:
                    keep.append(c)
            allowed = keep
        if not allowed:
            continue
        binds[pname] = (info, allowed)

    # --- method parameters: Class<T> (for `X.getEnumConstants()`) and plain types
    param_types: dict[str, str] = {}
    class_params: dict[str, str] = {}
    for header, _brace in chain:
        sig = METHOD_SIG_RE.search(header)
        if not sig:
            continue
        for pname, ptype in method_param_types(sig.group(0)).items():
            param_types[pname] = ptype
            mm = re.fullmatch(r"Class\s*<\s*([\w.$]+)\s*>", ptype)
            if mm:
                class_params[pname] = mm.group(1)
        break

    # --- enclosing for loops
    for header, _brace in chain:
        fm = FOR_RE.search(header)
        if not fm:
            continue
        var, expr = fm.group(2), fm.group(3)
        if var in binds:
            continue
        e = norm(expr)
        etype = None
        mm = re.fullmatch(r"(\w+)\.getEnumConstants\(\)", e)
        if mm and mm.group(1) in class_params:
            etype = class_params[mm.group(1)]
        elif mm and mm.group(1) in param_types:
            etype = param_types[mm.group(1)]
        else:
            etype = iterable_type(expr)
            if etype is None:
                mm2 = re.fullmatch(r"([\w.$]+)\.class\.getEnumConstants\(\)", e)
                if mm2:
                    etype = mm2.group(1)
        if not etype:
            continue
        info = resolve_enum(etype, jf)
        if info is None:
            continue
        binds[var] = (info, list(info.constants))

    # --- local String aliases: String x = src.getSerializedName() [.toLowerCase(..)];
    for var in re.findall(r"([A-Za-z_]\w*)\.(?:name|getSerializedName)\(\)", name_expr):
        if var in binds:
            continue
        mm = re.search(r"String\s+" + re.escape(var) + r"\s*=\s*([^;]+);", jf.text[max(0, s_start - 6000) : pos])
        if not mm:
            continue
        rhs = norm(mm.group(1))
        rm = re.fullmatch(r"(\w+)\.(name|getSerializedName)\(\)(?:\.toLowerCase\(Locale\.ROOT\))?", rhs)
        if rm and rm.group(1) in binds:
            aliases[var] = (rm.group(1), rm.group(2) == "getSerializedName")

    # --- guards from enclosing if blocks, plus `if (cond) { continue; }` skips
    neg_guards = []
    for header, brace in chain:
        h = norm(header)
        mm = re.search(r"\bif\s*\(", h)
        if mm:
            cstart = h.index("(", mm.start())
            cend = match_bracket_sub(h, cstart)
            guards.append(h[cstart + 1 : cend])
        if re.search(r"\bfor\s*\(", h):
            i = brace + 1
            while True:
                mm2 = re.match(r"\s*if\s*\(", jf.text[i:])
                if not mm2:
                    break
                gop = i + mm2.end() - 1
                gcl = jf.match_bracket(gop)
                cond = jf.text[gop + 1 : gcl]
                tail = jf.text[gcl + 1 : gcl + 120]
                m3 = re.match(r"\s*\{\s*continue\s*;\s*\}", tail)
                if not m3:
                    break
                neg_guards.append(cond)
                i = gcl + 1 + m3.end()

    cont_conds = []
    for header, brace in chain:
        if re.search(r"\bfor\s*\(", norm(header)):
            cont_conds.extend(collect_continue_conds(jf, brace))

    def evaluate(combo: dict):
        values, types = {}, {}
        for var, const in combo.items():
            info, _ = binds[var]
            values[var] = const
            types[var] = info.fqname
        for var, (src, serialized) in aliases.items():
            base = values.get(src)
            if base is None:
                return None, None
            values[var] = base.lower() if serialized else base
        return values, types

    ordered = list(binds.keys())

    def rec(i, cur):
        if i == len(ordered):
            yield dict(cur)
            return
        var = ordered[i]
        info, allowed = binds[var]
        for c in allowed:
            cur[var] = c
            yield from rec(i + 1, cur)
        cur.pop(var, None)

    produced = False
    for combo in rec(0, {}):
        values, types = evaluate(combo)
        if values is None:
            continue
        blocked = False
        for g in guards:
            gv = eval_cond(g, values, types)
            if EXPLAIN and site.key() == EXPLAIN:
                print("   guard %-45s %s" % (norm(g)[:45], gv))
            if gv is False:
                blocked = True
                break
        if not blocked:
            for g in neg_guards:
                if eval_cond(g, values, types) is True:
                    blocked = True
                    break
        if not blocked:
            for fpos, conds in cont_conds:
                if fpos >= pos:
                    continue
                vals = [eval_cond(c, values, types) for c in conds]
                if vals and all(v is True for v in vals):
                    if EXPLAIN and site.key() == EXPLAIN:
                        print("   continue-guard %s -> skip" % norm(" && ".join(conds))[:60])
                    blocked = True
                    break
        if blocked:
            continue
        name = eval_name_expr(name_expr, values, types)
        if EXPLAIN and site.key() == EXPLAIN:
            print("   values=%s -> %r" % (values, name))
        if name is None:
            yield None, site
            return
        produced = True
        yield name, site
    if not produced:
        yield None, site


MAPPER_CALL_RE = re.compile(r"[=.(]\s*(\w+)\s*\(\s*[\w.$]+\.class")


def reachable_mappers(jf: JavaFile):
    """Mapper methods that are actually invoked with a `X.class` argument.

    Decompiled leftovers such as `chiseledBookshelfMapperTFC` are defined but never
    called, so nothing they would register ever reaches the registry.
    """
    return {m.group(1) for m in MAPPER_CALL_RE.finditer(jf.text)}


CONT_CACHE: dict = {}


def collect_continue_conds(jf: JavaFile, for_brace: int):
    """[(pos, conds)] for `if (...) { continue; }` inside one for-loop body.

    `conds` is the chain of enclosing if-conditions, so a nested guard such as

        if (wood == BAMBOO) { if (type == WOOD) continue; }

    is only applied when both conditions hold.
    """
    key = (id(jf), for_brace)
    if key in CONT_CACHE:
        return CONT_CACHE[key]
    stop = jf.match_bracket(for_brace)
    out = []

    def walk(start, stop_at, conds):
        i = start
        while i < stop_at:
            m = re.search(r"(?<![\w.])if\s*\(", jf.text[i:stop_at])
            if not m:
                return
            cstart = i + m.end() - 1
            cend = jf.match_bracket(cstart)
            if cend >= stop_at:
                return
            cond = norm(jf.text[cstart + 1 : cend])
            j = cend + 1
            while j < stop_at and jf.text[j] in " \t\r\n":
                j += 1
            if j < stop_at and jf.text[j] == "{":
                bend = jf.match_bracket(j)
                inner = jf.text[j + 1 : bend].strip()
                if inner == "continue;" or inner.startswith("continue;"):
                    out.append((cstart, conds + [cond]))
                walk(j + 1, min(bend, stop_at), conds + [cond])
                i = bend + 1
            else:
                semi = jf.text.find(";", j, stop_at)
                if semi < 0:
                    return
                seg = jf.text[j : semi].strip()
                if seg.startswith("continue"):
                    out.append((cstart, conds + [cond]))
                i = semi + 1

    walk(for_brace + 1, stop, [])
    CONT_CACHE[key] = out
    return out


def collect_registrations(paths, site_stats=None):
    regs = []
    unresolved = []
    for path in paths:
        jf = JavaFile(path)
        called = reachable_mappers(jf)
        for m in REG_RE.finditer(jf.text):
            tail = jf.text[m.end() : m.end() + 60]
            if tail.lstrip().startswith(("String", "(")) and "RegistryObject" in jf.text[max(0, m.start() - 60) : m.start()]:
                continue
            # skip registrations inside a mapper that is never invoked
            owner = None
            for header, _brace in jf.block_chain(m.start()):
                sig = METHOD_SIG_RE.search(header)
                if sig:
                    owner = sig.group(1)
                    break
            if owner and owner != "register" and owner not in called:
                if site_stats is not None:
                    site_stats.append(
                        ("%s:%d" % (jf.name, jf.line_of(m.start())), jf.name, jf.line_of(m.start()), 0,
                         "[dead mapper %s] %s" % (owner, norm(jf.text[m.start() : m.start() + 70])))
                    )
                continue
            got = list(expand_site(jf, m))
            if site_stats is not None:
                site_stats.append(
                    ("%s:%d" % (jf.name, jf.line_of(m.start())), jf.name, jf.line_of(m.start()),
                     sum(1 for n, _s in got if n is not None),
                     norm(jf.text[m.start() : m.start() + 90]))
                )
            if not got:
                continue
            if all(n is None for n, _s in got):
                unresolved.append(
                    "%s:%d  %s" % (jf.name, jf.line_of(m.start()), norm(jf.text[m.start() : m.start() + 120]))
                )
                continue
            for n, s in got:
                if n is not None:
                    # ResourceLocation lowercases namespace/path, so registry ids are always
                    # lowercase even when the source builds them from `Type.name()`.
                    regs.append((n.lower(), s))
    return regs, unresolved


# ---------------------------------------------------------------------------
# Resource indexes
# ---------------------------------------------------------------------------


def index_dir(base: str, suffix=".json"):
    out = set()
    if not os.path.isdir(base):
        return out
    for dirpath, _d, files in os.walk(base):
        for fn in files:
            if not fn.endswith(suffix):
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), base).replace(os.sep, "/")
            out.add(rel[: -len(suffix)])
    return out


def load_lang(name: str):
    p = os.path.join(ASSET_ROOT, "lang", name)
    if not os.path.isfile(p):
        return {}
    raw = open(p, encoding="utf-8-sig").read()
    try:
        return json.loads(raw)
    except Exception:
        cleaned = re.sub(r",(\s*[}\]])", r"\1", raw)
        try:
            return json.loads(cleaned)
        except Exception:
            return {m.group(1): m.group(2) for m in re.finditer(r'"((?:[^"\\]|\\.)*)"\s*:\s*"((?:[^"\\]|\\.)*)"', cleaned)}


def load_dump():
    if not os.path.isfile(DUMP_PATH):
        return set(), set()
    with open(DUMP_PATH, encoding="utf-8") as fh:
        data = json.load(fh)
    reg = data.get("registries", {})
    blocks = {x.split(":", 1)[1] for x in reg.get("minecraft:block", []) if x.startswith(NS + ":")}
    items = {x.split(":", 1)[1] for x in reg.get("minecraft:item", []) if x.startswith(NS + ":")}
    return blocks, items


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--debug-site", help="e.g. TFCFBlocks.java:333")
    ap.add_argument("--list", default="", help="A|B|C to print that class")
    ap.add_argument("--sites", action="store_true", help="report per-register-site expansion counts")
    ap.add_argument("--explain", help="trace one site, e.g. TFCFBlocks.java:291")
    ap.add_argument("--limit", type=int, default=60)
    ap.add_argument("--out", default=os.path.join(ROOT, "reports", "registry-asset-audit.json"))
    args = ap.parse_args()

    if args.explain:
        global EXPLAIN
        EXPLAIN = args.explain

    paths = []
    for dirpath, _d, files in os.walk(JAVA_ROOT):
        for fn in files:
            if fn.endswith(".java"):
                paths.append(os.path.join(dirpath, fn))
    collect_imports(paths)

    index_enums()
    targets = [p for p in paths if os.path.basename(p) in TARGET_FILES]
    site_stats = []
    regs, unresolved = collect_registrations(targets, site_stats)

    if args.sites:
        empty = [s for s in site_stats if s[3] == 0]
        print("register sites: %d   producing 0 names: %d" % (len(site_stats), len(empty)))
        for key, fn, line, n, snippet in empty:
            print("  %-26s %-3d  %s" % (key, n, snippet[:100]))
        print("\ntop producers:")
        for key, fn, line, n, snippet in sorted(site_stats, key=lambda s: -s[3])[:15]:
            print("  %-26s %-5d %s" % (key, n, snippet[:90]))
        return 0

    if args.debug_site:
        want = args.debug_site.replace("\\", "/").split(":")[0]
        want_line = int(args.debug_site.split(":")[-1])
        hits = [(n, s) for n, s in regs if s.file == want.split("/")[-1] and s.line == want_line]
        for n, s in hits[:args.limit]:
            print("%-56s %-5s blockitem=%s %s" % (n, s.kind, s.block_item, s.key()))
        print("total %d names from that site" % len(hits))
        return 0

    blockstates = index_dir(os.path.join(ASSET_ROOT, "blockstates"))
    block_models = index_dir(os.path.join(ASSET_ROOT, "models", "block"))
    item_models = index_dir(os.path.join(ASSET_ROOT, "models", "item"))
    lang_zh = load_lang("zh_cn.json")
    lang_en = load_lang("en_us.json")
    dump_blocks, dump_items = load_dump()

    blocks: dict[str, Site] = {}
    items: dict[str, Site] = {}
    for name, site in regs:
        if site.kind == "block":
            blocks.setdefault(name, site)
            if site.block_item:
                items.setdefault(name, site)
        else:
            items.setdefault(name, site)

    def has_lang(path, kind):
        key = "%s.%s.%s" % (kind, NS, path.replace("/", "."))
        return key in lang_zh or key in lang_en

    rows = []
    for name in sorted(set(blocks) | set(items)):
        is_block = name in blocks
        is_item = name in items
        site = blocks.get(name) or items.get(name)
        has_bs = name in blockstates
        has_bm = name in block_models
        has_im = name in item_models
        l_block = has_lang(name, "block")
        l_item = has_lang(name, "item")
        lang_ok = l_block if not is_item else (l_item or l_block)
        model_ok = has_bs or has_bm or has_im
        if not lang_ok and not model_ok:
            cls = "A"
        elif not lang_ok:
            cls = "B"
        elif not model_ok:
            cls = "C"
        else:
            cls = "OK"
        rows.append(
            {
                "id": name,
                "block": is_block,
                "item": is_item,
                "blockstate": has_bs,
                "block_model": has_bm,
                "item_model": has_im,
                "lang_block": l_block,
                "lang_item": l_item,
                "class": cls,
                "site": site.key(),
                "in_dump": name in (dump_blocks if is_block else dump_items),
            }
        )

    counts = collections.Counter(r["class"] for r in rows)
    print("== expander ==")
    print("  blocks: %d   items: %d   unresolved sites: %d" % (len(blocks), len(items), len(unresolved)))
    print("== registry dump ==")
    print("  dump blocks: %d   dump items: %d" % (len(dump_blocks), len(dump_items)))
    only_src_b = sorted(set(blocks) - dump_blocks)
    only_dump_b = sorted(dump_blocks - set(blocks))
    print("  expander-only blocks: %d     dump-only blocks: %d" % (len(only_src_b), len(only_dump_b)))
    print("== classification ==")
    for k in ("A", "B", "C", "OK"):
        print("  %s: %d" % (k, counts.get(k, 0)))

    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as fh:
        json.dump(
            {
                "summary": dict(counts),
                "counts": {"blocks": len(blocks), "items": len(items), "unresolved": len(unresolved)},
                "expander_only_blocks": only_src_b,
                "dump_only_blocks": only_dump_b[:300],
                "dump_only_total": len(only_dump_b),
                # OK rows are omitted on purpose: they are thousands of lines and
                # carry no action.  Everything below is actionable.
                "rows": [r for r in rows if r["class"] != "OK"],
                "unresolved": unresolved,
            },
            fh,
            indent=1,
        )
    print("report -> %s" % args.out)

    # markdown summary next to the json report
    md_path = os.path.splitext(args.out)[0] + ".md"
    by_class = {k: [r for r in rows if r["class"] == k] for k in ("A", "B", "C")}
    groups = collections.OrderedDict()
    for r in by_class["A"]:
        key = "/".join(r["id"].split("/")[:2]) if "/" in r["id"] else r["id"]
        groups.setdefault(key, []).append(r)
    with open(md_path, "w", encoding="utf-8") as fh:
        fh.write("# 注册 / 资源审计（A/B/C）\n\n")
        fh.write("由 `tools/audit_register_assets.py` 生成。判定依据：展开源码里的 "
                 "`register(...)` 调用（含 `mapOfKeys` / `for` 循环变量），再对照 "
                 "`assets/tfcflorae/{blockstates,models/block,models/item}` 与 `lang/{zh_cn,en_us}.json`，"
                 "并用 `reports/runtime-registries.json`（真实注册表 dump）交叉验证。\n\n")
        fh.write("| 类别 | 数量 | 含义 | 处理 |\n|---|---|---|---|\n")
        fh.write("| A | %d | 注册了但无 lang、无任何模型 | 删除注册 |\n" % len(by_class["A"]))
        fh.write("| B | %d | 有模型、缺 lang | 补翻译 |\n" % len(by_class["B"]))
        fh.write("| C | %d | 有 lang、缺模型 | 补模型 |\n" % len(by_class["C"]))
        fh.write("\n统计：方块 %d、物品 %d；未解析注册点 %d；expander-only %d；dump-only %d。\n"
                 % (len(blocks), len(items), len(unresolved), len(only_src_b), len(only_dump_b)))
        fh.write("\n## A 类（删注册）\n\n")
        for key, items in groups.items():
            fh.write("### %s（%d）\n\n" % (key, len(items)))
            fh.write("| id | 代码位置 | 注册表 dump |\n|---|---|---|\n")
            for r in items[:200]:
                fh.write("| `%s` | %s | %s |\n" % (r["id"], r["site"], "有" if r["in_dump"] else "无"))
            fh.write("\n")
        fh.write("\n## B 类（补 lang）\n\n| id | 代码位置 |\n|---|---|\n")
        for r in by_class["B"]:
            fh.write("| `%s` | %s |\n" % (r["id"], r["site"]))
        fh.write("\n## C 类（补模型）\n\n| id | 代码位置 |\n|---|---|\n")
        for r in by_class["C"]:
            fh.write("| `%s` | %s |\n" % (r["id"], r["site"]))
    print("markdown -> %s" % md_path)

    if args.list:
        sel = [r for r in rows if r["class"] == args.list.upper()]
        print("\n== class %s (%d) ==" % (args.list.upper(), len(sel)))
        for r in sel[: args.limit]:
            print("  %-58s %s" % (r["id"], r["site"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
