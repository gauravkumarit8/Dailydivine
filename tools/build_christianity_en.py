#!/usr/bin/env python3
"""Builds app/src/main/assets/content/christianity_en.json from the King James
Version (public domain in the US; data: github.com/aruljohn/Bible-kjv).
Every verse is pulled BY REFERENCE from the source text, never typed from
memory, and the script aborts if a reference does not exist.

Usage: git clone --depth 1 https://github.com/aruljohn/Bible-kjv.git /tmp/kjv
       python3 tools/build_christianity_en.py [/tmp/kjv]
"""
import json, os, sys
SRC = sys.argv[1] if len(sys.argv) > 1 else '/tmp/kjv'
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'content', 'christianity_en.json')

CATS = {201: ('Psalms', 'book_psalms'), 202: ('Proverbs', 'book_proverbs'),
        203: ('Gospels', 'book_gospels'), 204: ('Letters & Prophets', 'book_letters')}
# (category, file, display book, chapter, first verse, last verse, tags)
R = [
 (201,'Psalms','Psalm',23,1,4,'comfort,trust,shepherd'),(201,'Psalms','Psalm',27,1,1,'courage,light'),
 (201,'Psalms','Psalm',46,1,2,'refuge,strength'),(201,'Psalms','Psalm',46,10,10,'stillness,peace'),
 (201,'Psalms','Psalm',34,8,8,'goodness,trust'),(201,'Psalms','Psalm',91,1,2,'refuge,protection'),
 (201,'Psalms','Psalm',121,1,2,'help,trust'),(201,'Psalms','Psalm',19,1,1,'creation,praise'),
 (201,'Psalms','Psalm',100,1,2,'joy,gratitude'),(201,'Psalms','Psalm',118,24,24,'joy,today'),
 (201,'Psalms','Psalm',37,4,5,'trust,desire'),(201,'Psalms','Psalm',51,10,10,'renewal,heart'),
 (201,'Psalms','Psalm',90,12,12,'wisdom,time'),(201,'Psalms','Psalm',119,105,105,'guidance,word'),
 (201,'Psalms','Psalm',143,8,8,'morning,guidance'),(201,'Psalms','Psalm',55,22,22,'burdens,trust'),
 (201,'Psalms','Psalm',62,1,2,'rest,salvation'),(201,'Psalms','Psalm',103,1,2,'gratitude,praise'),
 (201,'Psalms','Psalm',145,18,18,'prayer,nearness'),(201,'Psalms','Psalm',16,11,11,'joy,path'),
 (201,'Psalms','Psalm',4,8,8,'peace,sleep'),(201,'Psalms','Psalm',30,5,5,'hope,joy'),
 (202,'Proverbs','Proverbs',3,5,6,'trust,guidance'),(202,'Proverbs','Proverbs',4,23,23,'heart,discipline'),
 (202,'Proverbs','Proverbs',15,1,1,'kindness,speech'),(202,'Proverbs','Proverbs',16,3,3,'work,trust'),
 (202,'Proverbs','Proverbs',16,9,9,'planning,guidance'),(202,'Proverbs','Proverbs',17,17,17,'friendship,love'),
 (202,'Proverbs','Proverbs',18,10,10,'refuge,strength'),(202,'Proverbs','Proverbs',22,6,6,'teaching,children'),
 (202,'Proverbs','Proverbs',11,25,25,'generosity'),(202,'Proverbs','Proverbs',19,21,21,'purpose,planning'),
 (202,'Proverbs','Proverbs',27,17,17,'friendship,growth'),(202,'Proverbs','Proverbs',12,25,25,'anxiety,kindness'),
 (202,'Proverbs','Proverbs',14,29,29,'patience,anger'),(202,'Proverbs','Proverbs',3,13,13,'wisdom'),
 (202,'Proverbs','Proverbs',9,10,10,'wisdom,reverence'),(202,'Proverbs','Proverbs',10,12,12,'love,forgiveness'),
 (202,'Proverbs','Proverbs',13,20,20,'friendship,wisdom'),(202,'Proverbs','Proverbs',16,24,24,'kindness,speech'),
 (203,'Matthew','Matthew',5,14,16,'light,example'),(203,'Matthew','Matthew',6,33,33,'priorities,trust'),
 (203,'Matthew','Matthew',7,7,8,'prayer,asking'),(203,'Matthew','Matthew',11,28,30,'rest,burdens'),
 (203,'Matthew','Matthew',22,37,39,'love,commandment'),(203,'Matthew','Matthew',28,20,20,'presence,hope'),
 (203,'John','John',3,16,16,'love,faith'),(203,'John','John',8,12,12,'light,guidance'),
 (203,'John','John',13,34,35,'love,community'),(203,'John','John',14,6,6,'way,truth'),
 (203,'John','John',14,27,27,'peace'),(203,'John','John',15,12,13,'love,sacrifice'),
 (203,'John','John',15,5,5,'abiding,growth'),(203,'John','John',16,33,33,'peace,courage'),
 (204,'Isaiah','Isaiah',40,31,31,'strength,hope'),(204,'Isaiah','Isaiah',41,10,10,'courage,presence'),
 (204,'Isaiah','Isaiah',26,3,3,'peace,trust'),(204,'Micah','Micah',6,8,8,'justice,humility'),
 (204,'Lamentations','Lamentations',3,22,23,'mercy,morning'),(204,'Joshua','Joshua',1,9,9,'courage'),
 (204,'Deuteronomy','Deuteronomy',31,6,6,'courage,presence'),(204,'Romans','Romans',8,28,28,'hope,purpose'),
 (204,'Romans','Romans',8,38,39,'love,assurance'),(204,'Romans','Romans',12,2,2,'renewal,mind'),
 (204,'Romans','Romans',12,12,12,'hope,patience,prayer'),(204,'Romans','Romans',15,13,13,'hope,joy,peace'),
 (204,'1Corinthians','1 Corinthians',13,4,7,'love,patience'),(204,'1Corinthians','1 Corinthians',13,13,13,'faith,hope,love'),
 (204,'2Corinthians','2 Corinthians',12,9,9,'grace,weakness'),(204,'Galatians','Galatians',5,22,23,'virtue,fruit'),
 (204,'Ephesians','Ephesians',4,32,32,'kindness,forgiveness'),(204,'Philippians','Philippians',4,6,7,'anxiety,peace,prayer'),
 (204,'Philippians','Philippians',4,13,13,'strength'),(204,'Colossians','Colossians',3,23,23,'work,purpose'),
 (204,'1Thessalonians','1 Thessalonians',5,16,18,'gratitude,joy,prayer'),(204,'Hebrews','Hebrews',11,1,1,'faith'),
 (204,'James','James',1,5,5,'wisdom,asking'),(204,'1Peter','1 Peter',5,7,7,'anxiety,care'),
 (204,'1John','1 John',4,19,19,'love'),
]
cache = {}
def verses(fn, ch, a, b):
    if fn not in cache: cache[fn] = json.load(open(os.path.join(SRC, fn + '.json')))
    chap = next(c for c in cache[fn]['chapters'] if c['chapter'] == str(ch))
    out = []
    for n in range(a, b + 1):
        out.append(next(v['text'] for v in chap['verses'] if v['verse'] == str(n)).strip())
    return ' '.join(out)

# round-robin across categories so consecutive days vary
buckets = {k: [r for r in R if r[0] == k] for k in CATS}
order = []
while any(buckets.values()):
    for k in CATS:
        if buckets[k]: order.append(buckets[k].pop(0))
vs = []
for i, (cat, fn, disp, ch, a, b, tags) in enumerate(order, 1):
    ref = f'{disp} {ch}:{a}' + (f'-{b}' if b != a else '') + ' (KJV)'
    vs.append({'id': 2000 + i, 'categoryId': cat, 'dayNumber': i, 'originalText': '',
               'translatedText': verses(fn, ch, a, b), 'sourceReference': ref, 'tags': tags})
counts = {k: sum(1 for v in vs if v['categoryId'] == k) for k in CATS}
doc = {'religion': 'Christianity', 'language': 'en', 'version': 1,
       'source': 'King James Version (public domain in the United States)',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f: json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'verses', counts)
