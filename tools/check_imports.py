#!/usr/bin/env python3
"""Cross-file import sweep (no Android SDK needed): every `import com.dailydivine.app.X`
must resolve to a top-level declaration in the tree. Catches the class of CI failure
"Unresolved reference: Foo" caused by a file that was never copied/committed.
Usage (from repo root): python3 tools/check_imports.py
Limits: matches top-level declarations by name only; it is a safety net, not a compiler."""
import re, glob, collections, sys
decl = collections.defaultdict(set); files = {}
for p in glob.glob('app/src/**/*.kt', recursive=True):
    s = open(p, encoding='utf-8').read(); files[p] = s
    m = re.search(r'^package\s+([\w.]+)', s, re.M)
    if not m: continue
    for mm in re.finditer(r'^(?:@\w+(?:\([^)]*\))?\s+)*(?:(?:private|internal|public|data|enum|sealed|abstract|open|inline|suspend|const)\s+)*(?:class|object|interface|fun|val|var|typealias)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)', s, re.M):
        decl[m.group(1)].add(mm.group(1))
bad = 0
for p, s in files.items():
    for m in re.finditer(r'^import\s+(com\.dailydivine\.app\.[\w.]+?)(\.\*)?\s*$', s, re.M):
        full = m.group(1)
        if full.endswith('.R') or '.R.' in full: continue
        if m.group(2):
            ok = full in decl
        else:
            pkg, _, name = full.rpartition('.')
            ok = name in decl.get(pkg, set()) or any(full.startswith(k + '.') and full.split(k + '.')[1].split('.')[0] in decl[k] for k in decl)
        if not ok: print('UNRESOLVED', p, '->', full); bad += 1
print(len(files), 'files checked,', bad, 'unresolved imports')
sys.exit(1 if bad else 0)
