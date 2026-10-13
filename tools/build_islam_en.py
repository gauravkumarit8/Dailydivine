#!/usr/bin/env python3
"""Builds app/src/main/assets/content/islam_en.json from Marmaduke Pickthall's
"The Meaning of the Glorious Koran" (1930), public domain (Project Gutenberg #16955; also
stated as public domain by Tanzil.net). The text is read BY REFERENCE from the machine-readable
edition in github.com/fawazahmed0/quran-api (edition eng-mohammedmarmadu, sourced from Tanzil.net),
never typed from memory; the script aborts if a reference does not exist.

Usage: curl -o /tmp/pick.json https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/eng-mohammedmarmadu.json
       python3 tools/build_islam_en.py [/tmp/pick.json]

Notes: (1) Only the English translation is included, NOT the Arabic: Tanzil's Arabic text is
CC BY 3.0 with a strict no-modification clause and attribution/link requirements, which should be
handled deliberately if the Arabic is added later. (2) The dataset omits the punctuation at the end
of each verse; this is NOT patched. Multi-verse passages put each verse on its own line.
(3) The SELECTION of verses is editorial: have a Muslim reviewer vet it before release.
"""
import json, os, sys
SRC = sys.argv[1] if len(sys.argv) > 1 else '/tmp/pick.json'
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'content', 'islam_en.json')
CATS = {301: ('Mercy & Forgiveness', 'book_quran'), 302: ('Patience & Trust', 'book_quran'),
        303: ('Guidance & Knowledge', 'book_quran'), 304: ('Character & Kindness', 'book_quran')}
# (category, surah, first ayah, last ayah, tags)
# Editorial notes from review of the printed output: 65:2-3 was dropped (verse 2 opens with divorce
# rulings), 7:156 was replaced by 6:54 (it opens with Moses's prayer and a punishment clause), and
# 40:60 was not used (contains a hell clause). Read the output of every passage before shipping.
R = [
 (301, 39, 53, 53, 'mercy,hope,forgiveness'), (301, 6, 54, 54, 'mercy,forgiveness'), (301, 2, 186, 186, 'nearness,prayer'),
 (301, 4, 110, 110, 'forgiveness,repentance'), (301, 15, 49, 49, 'forgiveness,mercy'), (301, 20, 82, 82, 'repentance,forgiveness'),
 (302, 2, 153, 153, 'patience,prayer'), (302, 39, 10, 10, 'patience,reward'), (302, 94, 5, 6, 'hardship,hope,ease'),
 (302, 93, 3, 5, 'comfort,hope'), (302, 25, 58, 58, 'trust'), (302, 3, 173, 173, 'trust,protection'), (302, 9, 51, 51, 'trust,acceptance'),
 (302, 2, 286, 286, 'burdens,mercy'), (302, 14, 7, 7, 'gratitude'), (302, 2, 152, 152, 'remembrance'),
 (302, 13, 28, 28, 'peace,remembrance'), (302, 3, 200, 200, 'patience,perseverance'),
 (303, 1, 1, 7, 'guidance,prayer'), (303, 2, 255, 255, 'faith,protection'), (303, 20, 114, 114, 'knowledge,prayer'),
 (303, 39, 9, 9, 'knowledge'), (303, 112, 1, 4, 'unity,faith'), (303, 58, 11, 11, 'knowledge,rank'),
 (303, 96, 1, 5, 'knowledge,reading'),
 (304, 16, 90, 90, 'justice,kindness'), (304, 49, 13, 13, 'humanity,piety'), (304, 41, 34, 34, 'kindness,forgiveness'),
 (304, 17, 23, 23, 'parents,kindness'), (304, 25, 63, 63, 'humility,peace'), (304, 5, 8, 8, 'justice'),
 (304, 3, 159, 159, 'gentleness,leadership'), (304, 31, 18, 19, 'humility,manners'), (304, 30, 21, 21, 'love,family'),
]
idx = {(v['chapter'], v['verse']): v['text'].strip() for v in json.load(open(SRC, encoding='utf-8'))['quran']}
def passage(s, a, b): return '\n'.join(idx[(s, n)] for n in range(a, b + 1))  # KeyError = missing reference
buckets = {k: [r for r in R if r[0] == k] for k in CATS}; order = []
while any(buckets.values()):
    for k in CATS:
        if buckets[k]: order.append(buckets[k].pop(0))
vs = []
for i, (cat, s, a, b, tags) in enumerate(order, 1):
    ref = f'Quran {s}:{a}' + (f'-{b}' if b != a else '') + ' (Pickthall, 1930)'
    vs.append({'id': 3000 + i, 'categoryId': cat, 'dayNumber': i, 'originalText': '',
               'translatedText': passage(s, a, b), 'sourceReference': ref, 'tags': tags})
counts = {k: sum(1 for v in vs if v['categoryId'] == k) for k in CATS}
doc = {'religion': 'Islam', 'language': 'en', 'version': 1,
       'source': 'The Meaning of the Glorious Koran, tr. Marmaduke Pickthall (1930). Public domain; text via Tanzil.net / github.com/fawazahmed0/quran-api.',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f: json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'passages', counts)
