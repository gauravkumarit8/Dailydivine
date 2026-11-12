#!/usr/bin/env python3
"""Adds the reviewed Bhagavad Gita verses to hinduism_en.json (content version 3).

Input : tools/gita/gita_pilot.json  (Sanskrit from github.com/gita/gita verse.json, public-domain text;
        English and Hindi meanings written for DailyDivine and reviewed by the project owner)
Keeps the 46 Arnold passages (ids 1101-1146) exactly as they are, then appends the Gita verses with
ids 1147+, dayNumbers 47+, spread evenly across the four categories. English goes in translatedText,
Sanskrit in originalText. Hindi stays in tools/gita/gita_pilot.json until the app has a Hindi UI.
Idempotent: always rebuilds from the first 46 verses already in the file.
"""
import json, os, random
HERE = os.path.dirname(__file__)
OUT = os.path.join(HERE, '..', 'app', 'src', 'main', 'assets', 'content', 'hinduism_en.json')
EXCLUDED = {'3.35'}   # dharma = one's own duty can be misread as "stay in your own religion"
CAT = {111: ['2.13','2.14','2.20','2.22','4.38','13.28','15.15','6.30','7.14'],
       112: ['2.38','2.40','2.47','2.48','3.19','3.21','3.27','4.18','6.16','6.17','17.15','18.46'],
       113: ['2.56','2.62','2.63','2.66','2.70','2.71','5.18','6.5','6.6','6.35','12.15','12.18','16.21','18.63'],
       114: ['4.7','4.8','4.11','8.7','9.22','9.26','9.29','9.34','10.10','12.13','12.14','18.58','18.66','18.78']}
TAGS = {'2.47': 'action,detachment', '2.48': 'balance,action', '9.22': 'trust,care', '6.5': 'self,effort',
        '18.66': 'refuge,trust', '4.7': 'dharma,hope'}
def nbsp_danda(text):
    """Glue each danda (। ॥) to the word before it with a no-break space, so a line can never wrap
    and leave a danda alone on its own line (seen on a phone screenshot of Gita 9.29)."""
    return text.replace(' \u0964', '\u00a0\u0964').replace(' \u0965', '\u00a0\u0965')


doc = json.load(open(OUT, encoding='utf-8'))
base = [v for v in doc['verses'] if v['id'] <= 1146]
assert len(base) == 46 and [v['dayNumber'] for v in base] == list(range(1, 47)), 'Arnold block changed'
rows = {r['ref']: r for r in json.load(open(os.path.join(HERE, 'gita', 'gita_pilot.json'), encoding='utf-8'))}
used = [r for c in CAT.values() for r in c]
assert len(used) == len(set(used)), 'duplicate ref in category map'
assert set(used) == set(rows) - EXCLUDED, (set(used) ^ (set(rows) - EXCLUDED))
keyed = []
for cat, refs in CAT.items():
    refs = refs[:]; random.Random(11 + cat).shuffle(refs)
    for i, r in enumerate(refs): keyed.append(((i + 0.5) / len(refs), cat, r))
keyed.sort()
vs = list(base)
for n, (_, cat, r) in enumerate(keyed, 47):
    row = rows[r]
    assert row['english'].strip() and row['sanskrit'].strip()
    vs.append({'id': 1100 + n, 'categoryId': cat, 'dayNumber': n, 'originalText': nbsp_danda(row['sanskrit']),
               'translatedText': row['english'], 'sourceReference': f'Bhagavad Gita {r}',
               'tags': TAGS.get(r, 'gita')})
counts = {}
for v in vs: counts[v['categoryId']] = counts.get(v['categoryId'], 0) + 1
for c in doc['categories']: c['verseCount'] = counts[c['id']]
doc['verses'] = vs
doc['version'] = 3
doc['source'] = ('The Song Celestial (Bhagavad-Gita), tr. Sir Edwin Arnold, 1885 (public domain; Project Gutenberg #2388), '
                 'plus Bhagavad Gita verses: Sanskrit text (public domain) with English meanings written for DailyDivine.')
json.dump(doc, open(OUT, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
print(len(vs), 'verses', counts)

# ---- Hindi overlay: same verse ids, Hindi meaning in translatedText (see data/content/VerseLocalizer.kt).
# Kept OUT of assets/content so the content-integrity rules (own categories, dayNumbers 1..N) do not apply to it.
gita = [v for v in vs if v['id'] > 1146]
ov_dir = os.path.join(HERE, '..', 'app', 'src', 'main', 'assets', 'content_overlays')
os.makedirs(ov_dir, exist_ok=True)
by_ref = {f"Bhagavad Gita {r}": row for r, row in rows.items()}
ov = []
for v in gita:
    hi = by_ref[v['sourceReference']]['hindi'].strip()
    assert hi, v['sourceReference']
    ov.append({'id': v['id'], 'dayNumber': v['dayNumber'], 'translatedText': hi})
days = [x['dayNumber'] for x in ov]
assert days == list(range(days[0], days[0] + len(days))), 'Hindi overlay day numbers must be contiguous'
json.dump({'religion': 'Hinduism', 'language': 'hi',
           'source': 'Hindi meanings of Bhagavad Gita verses written for DailyDivine from the Sanskrit and reviewed by the project owner',
           'verses': ov}, open(os.path.join(ov_dir, 'hinduism_hi.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
print(len(ov), 'Hindi overlay verses, days', days[0], '-', days[-1])
