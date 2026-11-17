#!/usr/bin/env python3
"""Writes tools/gita/arnold_sanskrit.json: for each of the 46 Arnold passages in hinduism_en.json, the
Bhagavad Gita verse(s) it translates and their Sanskrit.

Usage: python3 tools/gita/extract_arnold_sanskrit.py /path/to/gita/data/verse.json
(verse.json from github.com/gita/gita, Unlicense. Only the Sanskrit text is used.)

The passage -> verse mapping below was made by reading each Arnold passage against its Sanskrit
(every verse was also checked to be the verse it claims to be) and is for the project owner to review:
see the review file delivered with Round AA. References use the standard 700-verse numbering found in
printed Gitas. The dataset counts Arjuna's question at the start of chapter 13 as an extra verse, so its
chapter-13 numbers are one higher; ds() converts.
"""
import json, os, re, sys

MAP = {
    1101: ['2.20'], 1102: ['2.47', '2.48'], 1103: ['2.62', '2.63'], 1104: ['6.30', '6.31'], 1105: ['2.22'],
    1106: ['2.49'], 1107: ['2.64', '2.65'], 1108: ['9.26', '9.27'], 1109: ['2.23', '2.24', '2.25'], 1110: ['3.8'],
    1111: ['2.56'], 1112: ['9.29'], 1113: ['2.12'], 1114: ['3.35'], 1115: ['2.70'], 1116: ['9.30', '9.31'],
    1117: ['7.3'], 1118: ['3.21'], 1119: ['3.42'], 1120: ['9.31'], 1121: ['10.20'], 1122: ['3.26'],
    1123: ['5.19'], 1124: ['12.13', '12.14'], 1125: ['13.17'], 1126: ['18.47'], 1127: ['5.20'], 1128: ['12.15'],
    1129: ['18.20'], 1130: ['5.10'], 1131: ['6.5'], 1132: ['12.12'], 1133: ['4.7', '4.8'], 1134: ['4.33', '4.34'],
    1135: ['6.19'], 1136: ['13.7'], 1137: ['4.38'], 1138: ['17.20'], 1139: ['6.16', '6.17'], 1140: ['16.1', '16.2'],
    1141: ['4.36'], 1142: ['6.35'], 1143: ['16.21'], 1144: ['6.40'], 1145: ['17.15'], 1146: ['17.16'],
}
SPEAKERS = ('श्री भगवानुवाच', 'श्रीभगवानुवाच', 'अर्जुन उवाच', 'धृतराष्ट्र उवाच', 'सञ्जय उवाच', 'संजय उवाच')
# The dataset breaks these two verses in the middle of a word at a line end (checked by hand).
JOIN_FIXES = {'2.20': ('कदाचि न्नायं', 'कदाचिन्नायं'), '2.22': ('जीर्णा न्यन्यानि', 'जीर्णान्यन्यानि')}


def ds(ref):
    c, n = ref.split('.')
    return f"{c}.{int(n) + 1 if c == '13' else int(n)}"


def verse_sanskrit(verses, ref):
    t = re.sub(r'।।\s*\d+\.\d+\s*।+', '', verses[ds(ref)]['text'])
    lines = [l.strip() for l in t.split('\n') if l.strip()]
    if lines and lines[0] in SPEAKERS:
        lines = lines[1:]
    s = ' '.join(lines)
    if ref in JOIN_FIXES:
        old, new = JOIN_FIXES[ref]
        assert s.count(old) == 1, (ref, old)
        s = s.replace(old, new)
    halves = [h.strip() for h in re.sub(r'\s+', ' ', s).split('।') if h.strip()]
    assert len(halves) == 2, (ref, halves)
    return halves[0] + ' ।\n' + halves[1] + ' ॥'


def main(path):
    verses = {f"{x['chapter_number']}.{x['verse_number']}": x for x in json.load(open(path, encoding='utf-8'))}
    out = {str(pid): {'refs': refs, 'sanskrit': '\n'.join(verse_sanskrit(verses, r) for r in refs)}
           for pid, refs in MAP.items()}
    dest = os.path.join(os.path.dirname(__file__), 'arnold_sanskrit.json')
    json.dump(out, open(dest, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    print(len(out), 'passages,', len({r for refs in MAP.values() for r in refs}), 'distinct verses ->', dest)


if __name__ == '__main__':
    main(sys.argv[1])
