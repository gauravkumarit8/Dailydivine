#!/usr/bin/env python3
"""Catches the string-resource mistakes that only the Android resource compiler (aapt2) reports:
an unescaped apostrophe, an unescaped double quote, or a stray '@' / '?' at the start of a value.
The XML is valid in every one of these cases, so an XML parse does not see them.
Exit code 1 and a list of offenders if any is found."""
import glob, re, sys
import xml.etree.ElementTree as ET

bad = []
for path in sorted(glob.glob('app/src/main/res/values*/strings.xml')):
    for el in ET.parse(path).getroot():
        if el.tag != 'string':
            continue
        text = ''.join(el.itertext())
        name = el.get('name')
        # A value wrapped in double quotes may contain apostrophes; otherwise they must be \'
        if not (text.startswith('"') and text.endswith('"')):
            if re.search(r"(?<!\\)'", text):
                bad.append((path, name, "unescaped apostrophe: use \\' or the typographic \u2019"))
            if re.search(r'(?<!\\)"', text):
                bad.append((path, name, 'unescaped double quote: use \\"'))
        if text[:1] in '@?' and text[:2] not in ('@@', '??'):
            bad.append((path, name, 'starts with @ or ? (read as a resource reference)'))
for b in bad:
    print('%s: %s: %s' % b)
print('string resources: %d problem(s)' % len(bad))
sys.exit(1 if bad else 0)
