#!/usr/bin/env python3
"""Stricter 'Unresolved reference' safety net (no Android SDK needed).
Flags a file that USES a project class/object/interface (or a distinctively named
top-level function/constant) declared in ANOTHER package without importing it.
Complements tools/check_imports.py, which only validates import lines.
Usage (repo root): python3 tools/check_symbols.py     Exit code 1 if anything is flagged.
Limits: name-based, not a compiler; reports possibilities, so read each hit."""
import re, glob, collections, sys
def strip(s):
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S); s = re.sub(r'//[^\n]*', '', s)
    return re.sub(r'"(?:\\.|[^"\\\n])*"', '""', s)
types = collections.defaultdict(set); tops = collections.defaultdict(set); data = {}
for p in glob.glob('app/src/**/*.kt', recursive=True):
    s = strip(open(p, encoding='utf-8').read())
    pkg = re.search(r'^package\s+([\w.]+)', s, re.M).group(1); data[p] = (pkg, s)
    for m in re.finditer(r'^(?:(?:private|internal|public|data|enum|sealed|abstract|open|inline|suspend|const)\s+)*(?:class|object|interface)\s+(\w+)', s, re.M):
        types[m.group(1)].add(pkg)
    for m in re.finditer(r'^(?:(?:internal|public|inline|suspend|const)\s+)*(?:fun|val|var)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)', s, re.M):
        tops[m.group(1)].add(pkg)
bad = 0
for p, (pkg, s) in data.items():
    imports = set(re.findall(r'^import\s+([\w.]+)', s, re.M)); wild = set(re.findall(r'^import\s+([\w.]+)\.\*', s, re.M))
    local = set(re.findall(r'\b(?:class|object|interface)\s+(\w+)', s))
    body = re.sub(r'^(package|import)\s.*$', '', s, flags=re.M)
    def ok(name, pkgs): return pkg in pkgs or any(i.endswith('.' + name) for i in imports) or any(w in pkgs for w in wild)
    for name in sorted(set(re.findall(r'(?<![\w.])([A-Z][A-Za-z0-9_]+)\b', body))):
        if name in types and name not in local and not ok(name, types[name]):
            print('MISSING IMPORT (type)', p, '->', name, sorted(types[name])); bad += 1
    for name, pkgs in tops.items():
        if len(name) >= 8 and re.search(r'(?<![\w.])' + re.escape(name) + r'\b', body) and not ok(name, pkgs) and name not in local:
            if name[0].isupper() or re.search(r'\b' + re.escape(name) + r'\s*\(', body):
                print('CHECK (top-level)', p, '->', name, sorted(pkgs)); bad += 1
print('flagged:', bad); sys.exit(1 if bad else 0)
