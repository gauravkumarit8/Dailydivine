#!/usr/bin/env python3
"""Builds app/src/main/assets/content/islam_en.json (content version 2) from Marmaduke Pickthall's
"The Meaning of the Glorious Koran" (1930).

Licence: published 1930, so in the US public domain since 1 January 2026; Pickthall died in 1936, so
also public domain in India. The text is read BY REFERENCE from the machine-readable edition in
github.com/Kristories/quran (MIT for the repository; fixtures/en/pickthall/source.json, 114 chapters,
6,236 verses), never typed from memory; the script aborts if a reference does not exist.

Usage: git clone --depth 1 https://github.com/Kristories/quran.git /tmp/quran
       python3 tools/build_islam_en.py [/tmp/quran/fixtures/en/pickthall/source.json]

Format (kept identical to version 1, so the two read the same): the closing punctuation of each verse is
dropped and a multi-verse passage puts each verse on its own line.

Version 1 (34 passages, ids 3001-3034, round-robin across the four categories) is FROZEN: R_V1 below is
copied byte for byte from the version-1 script and the script aborts if the first 34 passages it builds
differ from the ones already in the JSON file. Version 2 appends R_V2, spread evenly across the
categories so consecutive days vary.

Not included: the Arabic (Tanzil's Arabic text is CC BY 3.0 with a no-modification clause and
link/attribution requirements; decide deliberately before adding it, and the app would need
right-to-left layout in the card, the share image and the wallpaper).

SELECTION IS EDITORIAL. Rules used: whole verses or 1-3 consecutive verses; no legal rulings; no battle
context lifted out of context; no hell or punishment clause; no verse that needs commentary to be read
safely; no text hostile to another faith; no sentence fragment. Have a Muslim reviewer vet the list
before release.
"""
import json, os, random, re, sys
SRC = sys.argv[1] if len(sys.argv) > 1 else '/tmp/quran/fixtures/en/pickthall/source.json'
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'content', 'islam_en.json')
CATS = {301: ('Mercy & Forgiveness', 'book_quran'), 302: ('Patience & Trust', 'book_quran'),
        303: ('Guidance & Knowledge', 'book_quran'), 304: ('Character & Kindness', 'book_quran')}
# (category, surah, first ayah, last ayah, tags)
# Editorial notes from review of the printed output (version 1): 65:2-3 was dropped (verse 2 opens with divorce
# rulings), 7:156 was replaced by 6:54 (it opens with Moses's prayer and a punishment clause), and
# 40:60 was not used (contains a hell clause).
R_V1 = [
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
# Version 2. Dropped after reading the printed text (not used): 3:139, 3:146, 3:186, 8:46, 9:40, 3:147 (battle
# context); 40:7 (hell clause); 2:83 (a reproach of another community); 4:36 (slavery wording); 2:222, 65:2-3,
# 5:2, 2:177 (rulings or long ritual lists); 42:37, 41:35, 73:9, 23:8 (fragments); 64:14 (can be misread);
# 71:28, 6:122, 20:124 (curse or blind-and-doomed wording); 7:172, 2:260, 2:249, 24:35, 57:20 (length or context).
R_V2 = [
    # 301
 (301, 3, 31, 31, 'love,following'), (301, 3, 135, 135, 'repentance,forgiveness'), (301, 4, 147, 147, 'gratitude,faith'),
 (301, 4, 149, 149, 'forgiveness,kindness'), (301, 5, 39, 39, 'repentance,mercy'), (301, 7, 23, 23, 'repentance,prayer'),
 (301, 11, 90, 90, 'forgiveness,mercy'), (301, 12, 92, 92, 'forgiveness,mercy'), (301, 15, 56, 56, 'hope,mercy'),
 (301, 21, 83, 84, 'hardship,mercy'), (301, 21, 87, 87, 'prayer,repentance'), (301, 24, 22, 22, 'forgiveness,generosity'),
 (301, 42, 25, 25, 'repentance,mercy'), (301, 42, 40, 40, 'forgiveness,reconciliation'), (301, 71, 10, 12, 'forgiveness,abundance'),
 (301, 110, 1, 3, 'help,forgiveness'), (301, 12, 87, 87, 'hope,perseverance'), (301, 28, 16, 16, 'repentance,prayer'),
 (301, 25, 70, 70, 'repentance,mercy'), (301, 6, 160, 160, 'reward,kindness'), (301, 17, 25, 25, 'repentance,intention'),
 (301, 57, 28, 28, 'light,forgiveness'), (301, 7, 151, 151, 'prayer,mercy'), (301, 14, 41, 41, 'prayer,family'),
 (301, 23, 118, 118, 'prayer,mercy'), (301, 2, 128, 128, 'prayer,repentance'), (301, 17, 24, 24, 'parents,mercy'),
 (301, 18, 10, 10, 'prayer,mercy'), (301, 25, 74, 74, 'prayer,family'), (301, 26, 83, 85, 'prayer,wisdom'),
 (301, 59, 10, 10, 'prayer,community'),
    # 302
 (302, 2, 45, 46, 'patience,prayer'), (302, 2, 155, 156, 'trial,patience'), (302, 12, 18, 18, 'patience,beauty'),
 (302, 12, 86, 86, 'sorrow,trust'), (302, 16, 96, 96, 'patience,reward'), (302, 16, 127, 127, 'patience,kindness'),
 (302, 20, 25, 28, 'prayer,ease'), (302, 29, 69, 69, 'striving,guidance'), (302, 31, 17, 17, 'patience,prayer'),
 (302, 41, 30, 30, 'steadfast,peace'), (302, 57, 22, 23, 'calamity,peace'), (302, 64, 11, 11, 'trust,heart'),
 (302, 103, 1, 3, 'time,patience'), (302, 94, 1, 4, 'ease,comfort'), (302, 8, 2, 2, 'faith,trust'),
 (302, 14, 12, 12, 'trust,patience'), (302, 3, 160, 160, 'trust,help'), (302, 11, 115, 115, 'patience,reward'),
 (302, 20, 130, 130, 'patience,praise'), (302, 33, 3, 3, 'trust,sufficiency'), (302, 10, 62, 63, 'fearlessness,faith'),
 (302, 42, 43, 43, 'patience,forgiveness'), (302, 29, 58, 59, 'patience,trust'), (302, 7, 128, 128, 'patience,help'),
 (302, 18, 23, 24, 'humility,future'), (302, 26, 62, 62, 'trust,guidance'), (302, 28, 24, 24, 'need,prayer'),
 (302, 40, 55, 55, 'patience,forgiveness'), (302, 52, 48, 48, 'patience,trust'), (302, 70, 5, 5, 'patience'),
 (302, 73, 10, 10, 'patience,dignity'), (302, 8, 61, 61, 'peace,trust'),
    # 303
 (303, 2, 269, 269, 'wisdom,gift'), (303, 2, 285, 285, 'faith,hearing'), (303, 3, 8, 8, 'guidance,prayer'),
 (303, 3, 190, 191, 'creation,reflection'), (303, 13, 11, 11, 'change,effort'), (303, 16, 78, 78, 'gratitude,senses'),
 (303, 16, 125, 125, 'wisdom,invitation'), (303, 17, 36, 36, 'knowledge,honesty'), (303, 17, 85, 85, 'knowledge,humility'),
 (303, 27, 19, 19, 'gratitude,prayer'), (303, 39, 18, 18, 'listening,best'), (303, 6, 162, 163, 'devotion,life'),
 (303, 114, 1, 6, 'refuge,protection'), (303, 107, 1, 3, 'kindness,prayer'), (303, 97, 1, 5, 'night,peace'),
 (303, 55, 1, 4, 'mercy,teaching'), (303, 6, 59, 59, 'knowledge,unseen'), (303, 10, 57, 57, 'guidance,healing'),
 (303, 22, 46, 46, 'sight,heart'), (303, 2, 2, 2, 'guidance,reverence'), (303, 3, 18, 18, 'witness,knowledge'),
 (303, 29, 43, 43, 'knowledge,parables'), (303, 13, 19, 19, 'knowledge,truth'), (303, 59, 22, 22, 'unseen,mercy'),
 (303, 67, 2, 2, 'life,deeds'), (303, 53, 39, 39, 'effort'), (303, 2, 256, 256, 'freedom,faith'),
 (303, 17, 9, 9, 'guidance'), (303, 18, 109, 109, 'knowledge,wonder'), (303, 31, 27, 27, 'wonder,knowledge'),
 (303, 67, 23, 23, 'gratitude,senses'), (303, 87, 1, 3, 'praise,guidance'), (303, 88, 17, 20, 'creation,reflection'),
 (303, 91, 7, 10, 'soul,growth'),
    # 304
 (304, 2, 195, 195, 'goodness,generosity'), (304, 2, 263, 263, 'kind word,charity'), (304, 3, 92, 92, 'generosity,piety'),
 (304, 3, 134, 134, 'anger,generosity'), (304, 4, 86, 86, 'greeting,courtesy'), (304, 4, 114, 114, 'peace,charity'),
 (304, 7, 199, 199, 'forgiveness,kindness'), (304, 17, 53, 53, 'speech,kindness'), (304, 25, 67, 67, 'balance,generosity'),
 (304, 28, 77, 77, 'balance,kindness'), (304, 31, 14, 14, 'parents,gratitude'), (304, 41, 33, 33, 'speech,goodness'),
 (304, 49, 10, 10, 'brotherhood,peace'), (304, 49, 11, 11, 'respect,speech'), (304, 49, 12, 12, 'suspicion,backbiting'),
 (304, 59, 9, 9, 'generosity,selflessness'), (304, 76, 8, 9, 'feeding,selflessness'), (304, 93, 9, 11, 'orphans,gratitude'),
 (304, 33, 70, 70, 'speech,truth'), (304, 2, 148, 148, 'goodness,effort'), (304, 2, 215, 215, 'generosity,family'),
 (304, 9, 71, 71, 'brotherhood,justice'), (304, 2, 267, 267, 'generosity,quality'), (304, 2, 261, 261, 'generosity,growth'),
 (304, 17, 37, 37, 'humility,pride'), (304, 6, 152, 152, 'justice,honesty'), (304, 23, 1, 4, 'prayer,humility'),
 (304, 24, 27, 27, 'manners,peace'), (304, 3, 104, 104, 'community,goodness'), (304, 4, 58, 58, 'trust,justice'),
 (304, 4, 135, 135, 'justice,honesty'), (304, 7, 31, 31, 'moderation'), (304, 9, 119, 119, 'truthfulness'),
 (304, 16, 128, 128, 'goodness,duty'), (304, 22, 77, 77, 'worship,goodness'), (304, 30, 22, 22, 'diversity,wonder'),
 (304, 51, 19, 19, 'generosity,poor'), (304, 60, 8, 8, 'kindness,justice'), (304, 17, 34, 34, 'promises,honesty'),
 (304, 2, 110, 110, 'goodness,reward'), (304, 57, 7, 7, 'generosity,faith'), (304, 92, 5, 7, 'giving,ease'),
 (304, 17, 26, 26, 'generosity,balance'), (304, 17, 29, 29, 'generosity,balance'),
]

S = {}
for sura in json.load(open(SRC, encoding='utf-8')):
    S[sura['number']] = {v['number']: v['translation'] for v in sura['verses']}

def strip_end(t):
    return re.sub(r'[\s.,;:!?]+$', '', t.strip())

def passage(s, a, b):
    try:
        return '\n'.join(strip_end(S[s][n]) for n in range(a, b + 1))
    except KeyError:
        sys.exit(f'MISSING reference {s}:{a}-{b}')

def order_round_robin(rs):
    buckets = {k: [r for r in rs if r[0] == k] for k in CATS}
    out = []
    while any(buckets.values()):
        for k in CATS:
            if buckets[k]:
                out.append(buckets[k].pop(0))
    return out

# V2: spread each category evenly (shuffled with a fixed seed), then interleave
keyed = []
for cat in CATS:
    items = [r for r in R_V2 if r[0] == cat]
    random.Random(21 + cat).shuffle(items)
    for i, it in enumerate(items):
        keyed.append(((i + 0.5) / len(items), cat, it))
keyed.sort(key=lambda x: (x[0], x[1]))
order = order_round_robin(R_V1) + [it for _, _, it in keyed]

# no duplicate or overlapping verses anywhere
seen = {}
for r in order:
    for n in range(r[2], r[3] + 1):
        if (r[1], n) in seen:
            sys.exit(f'DUPLICATE/OVERLAP {r[1]}:{n} in {r[1]}:{r[2]}-{r[3]} and {seen[(r[1], n)]}')
        seen[(r[1], n)] = f'{r[1]}:{r[2]}-{r[3]}'

vs = []
for i, (cat, s, a, b, tags) in enumerate(order, 1):
    ref = f'Quran {s}:{a}' + (f'-{b}' if b != a else '') + ' (Pickthall, 1930)'
    text = passage(s, a, b)
    # the 34 version-1 passages are frozen (compared below); the quality limits apply to what is added
    if not text or (i > 34 and ('  ' in text or len(text) > 460 or re.search(r'[A-Z]{2,}', text))):
        sys.exit(f'BAD TEXT {ref} len={len(text)}: {text[:80]}')
    vs.append({'id': 3000 + i, 'categoryId': cat, 'dayNumber': i, 'originalText': '',
               'translatedText': text, 'sourceReference': ref, 'tags': tags})

# version 1 must be unchanged
if os.path.exists(OUT):
    old = json.load(open(OUT, encoding='utf-8'))['verses'][:34]
    key = lambda v: (v['id'], v['dayNumber'], v['categoryId'], v['sourceReference'], v['translatedText'])
    assert [key(v) for v in old] == [key(v) for v in vs[:34]], 'VERSION 1 PASSAGES CHANGED'

counts = {k: sum(1 for v in vs if v['categoryId'] == k) for k in CATS}
doc = {'religion': 'Islam', 'language': 'en', 'version': 2,
       'source': 'The Meaning of the Glorious Koran, tr. Marmaduke Pickthall (1930). Public domain; text via github.com/Kristories/quran (Tanzil.net edition).',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f:
    json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'passages', counts, 'longest', max(len(v['translatedText']) for v in vs))
