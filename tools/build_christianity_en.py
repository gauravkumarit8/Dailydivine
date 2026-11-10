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
R_V1 = [
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
# New passages (version 2). Format: "Book chapter:first-last | tags". Pulled by reference only.
V2_TEXT = {
 201: """
Psalm 1:1-3|blessing,roots
Psalm 5:3|morning,prayer
Psalm 8:3-4|wonder,creation
Psalm 9:10|trust,name
Psalm 10:17|hope,listening
Psalm 13:5-6|trust,joy
Psalm 16:8|presence,steadfast
Psalm 18:1-2|strength,refuge
Psalm 19:14|words,meditation
Psalm 20:7|trust
Psalm 23:5-6|goodness,mercy
Psalm 24:1|creation
Psalm 25:4-5|guidance,teaching
Psalm 27:4|devotion,beauty
Psalm 27:14|patience,courage
Psalm 28:7|strength,joy
Psalm 29:11|strength,peace
Psalm 31:24|courage,hope
Psalm 32:8|guidance,care
Psalm 33:20-22|hope,trust
Psalm 34:1|praise,gratitude
Psalm 34:18|comfort,nearness
Psalm 36:5|faithfulness,love
Psalm 36:9|life,light
Psalm 37:7|patience,rest
Psalm 37:23-24|guidance,steadiness
Psalm 39:7|hope
Psalm 40:1-2|patience,rescue
Psalm 42:1-2|longing,thirst
Psalm 42:11|hope,hope
Psalm 43:3|light,truth
Psalm 50:15|prayer,help
Psalm 51:12|joy,renewal
Psalm 56:3-4|courage,trust
Psalm 57:7|steadfast,song
Psalm 61:2|refuge,prayer
Psalm 63:1|longing,morning
Psalm 65:11|abundance,gratitude
Psalm 67:1-2|blessing,grace
Psalm 68:19|gratitude,burdens
Psalm 71:5|hope,trust
Psalm 73:26|strength,heart
Psalm 84:1-2|longing,joy
Psalm 84:10|devotion
Psalm 86:11|teaching,heart
Psalm 89:1|faithfulness,song
Psalm 90:1-2|home,eternity
Psalm 91:11-12|protection,care
Psalm 92:1-2|gratitude,morning
Psalm 94:19|comfort,anxiety
Psalm 95:1-2|joy,praise
Psalm 96:1-2|song,praise
Psalm 98:1|song,joy
Psalm 100:4-5|gratitude,goodness
Psalm 103:8|mercy,patience
Psalm 103:11-12|mercy,forgiveness
Psalm 104:33|praise,life
Psalm 107:1|gratitude,love
Psalm 111:10|wisdom
Psalm 112:7|courage,trust
Psalm 113:3|praise,day
Psalm 116:1-2|prayer,love
Psalm 118:1|gratitude,love
Psalm 118:6|courage,fear
Psalm 119:11|word,heart
Psalm 119:18|wonder,teaching
Psalm 119:130|light,understanding
Psalm 121:7-8|protection,care
Psalm 122:1|joy,gathering
Psalm 125:1-2|steadfast,protection
Psalm 126:5-6|hope,joy
Psalm 127:1|work,trust
Psalm 130:5-6|patience,hope
Psalm 133:1|unity,kindness
Psalm 136:1|gratitude,love
Psalm 138:3|prayer,strength
Psalm 139:7-10|presence,nearness
Psalm 139:14|wonder,gratitude
Psalm 139:23-24|honesty,guidance
Psalm 141:3|speech,self-control
Psalm 145:3|praise,greatness
Psalm 145:8-9|mercy,goodness
Psalm 147:3|healing,comfort
Psalm 150:6|praise,breath
""",
 202: """
Proverbs 1:7|wisdom,learning
Proverbs 2:6|wisdom,gift
Proverbs 3:3-4|kindness,truth
Proverbs 3:27|kindness,generosity
Proverbs 4:7|wisdom,understanding
Proverbs 4:18|light,growth
Proverbs 6:6|work,diligence
Proverbs 8:11|wisdom,value
Proverbs 10:9|integrity
Proverbs 10:19|speech,restraint
Proverbs 11:2|humility
Proverbs 11:17|kindness
Proverbs 11:24|generosity
Proverbs 12:15|listening,advice
Proverbs 13:3|speech,restraint
Proverbs 13:12|hope,patience
Proverbs 14:30|peace,heart
Proverbs 14:34|justice,character
Proverbs 15:13|joy,heart
Proverbs 15:22|advice,planning
Proverbs 15:23|speech,joy
Proverbs 15:30|joy,encouragement
Proverbs 16:18|humility,pride
Proverbs 16:32|patience,self-control
Proverbs 17:1|peace,home
Proverbs 17:22|joy,health
Proverbs 18:13|listening,patience
Proverbs 18:21|speech,words
Proverbs 19:2|patience,haste
Proverbs 19:11|patience,forgiveness
Proverbs 20:5|wisdom,understanding
Proverbs 21:21|kindness,righteousness
Proverbs 22:1|character,name
Proverbs 22:4|humility,reverence
Proverbs 24:3-4|home,wisdom
Proverbs 24:10|courage,adversity
Proverbs 25:11|speech,kindness
Proverbs 25:21-22|kindness,enemies
Proverbs 27:1|today,humility
Proverbs 28:13|honesty,mercy
Proverbs 29:11|self-control,anger
Proverbs 29:18|vision,guidance
Proverbs 31:25-26|strength,wisdom
Proverbs 31:30|character,reverence
""",
 203: """
Matthew 5:3-5|humility,comfort,blessing
Matthew 5:6|longing,justice
Matthew 5:44-45|love,enemies
Matthew 6:9-13|prayer,trust
Matthew 6:19-21|priorities,heart
Matthew 6:25-26|anxiety,trust
Matthew 6:34|anxiety,today
Matthew 7:12|kindness,golden-rule
Matthew 14:27|courage,peace
Matthew 16:26|priorities,soul
Matthew 18:20|community,presence
Matthew 19:14|children,welcome
Matthew 19:26|hope,possibility
Matthew 20:26-28|service,humility
Matthew 25:40|kindness,service
Mark 9:23|faith,possibility
Mark 10:27|hope,possibility
Mark 11:24|prayer,faith
Mark 12:30-31|love,commandment
Mark 10:45|service
Mark 6:31|rest,renewal
Luke 1:37|hope,possibility
Luke 6:27-28|love,enemies
Luke 6:31|kindness,golden-rule
Luke 6:36-37|mercy,forgiveness
Luke 6:38|generosity
Luke 10:27|love,commandment
Luke 11:9-10|prayer,asking
Luke 12:15|priorities,contentment
Luke 12:34|heart,priorities
Luke 17:21|peace,presence
Luke 18:27|hope,possibility
John 1:1-4|word,light,life
John 1:5|light,hope
John 1:12|belonging,faith
John 4:24|worship,truth
John 6:35|nourishment,faith
John 8:32|truth,freedom
John 10:10|life,abundance
John 10:27-28|guidance,belonging
John 11:25-26|hope,life
John 12:46|light,faith
John 14:1|peace,trust
John 14:15|love,commandment
John 14:18|presence,comfort
John 15:11|joy
John 17:3|knowledge,life
""",
 204: """
Genesis 1:1|beginning,creation
Genesis 1:27|dignity,creation
Genesis 28:15|presence,promise
Exodus 14:14|stillness,trust
Exodus 33:14|presence,rest
Numbers 6:24-26|blessing,peace
Deuteronomy 6:4-5|love,devotion
Deuteronomy 31:8|courage,presence
Joshua 1:8|meditation,guidance
Joshua 24:15|choice,devotion
Ruth 1:16|loyalty,love
1Samuel 16:7|heart,character
2Chronicles 7:14|humility,prayer
2Chronicles 16:9|strength,heart
Job 19:25|hope,trust
Job 28:28|wisdom,reverence
Ecclesiastes 3:1-4|seasons,time
Ecclesiastes 4:9-10|friendship,community
Isaiah 9:6|hope,peace
Isaiah 12:2|trust,salvation
Isaiah 30:15|rest,quietness
Isaiah 30:21|guidance
Isaiah 40:28-29|strength,rest
Isaiah 43:1-2|courage,presence
Isaiah 43:18-19|renewal,hope
Isaiah 53:5|healing,peace
Isaiah 54:10|love,peace
Isaiah 55:8-9|humility,wisdom
Isaiah 55:12|joy,peace
Isaiah 60:1|light,morning
Jeremiah 17:7-8|trust,roots
Jeremiah 29:11|hope,purpose
Jeremiah 29:13|seeking,prayer
Jeremiah 31:3|love,faithfulness
Jeremiah 33:3|prayer,seeking
Ezekiel 36:26|renewal,heart
Hosea 6:6|mercy,devotion
Hosea 10:12|growth,justice
Habakkuk 3:17-18|joy,trust
Zephaniah 3:17|joy,love
Zechariah 4:6|strength,spirit
Acts 17:28|presence,life
Acts 20:35|generosity,giving
Romans 5:3-5|hope,patience
Romans 5:8|love,grace
Romans 8:18|hope,patience
Romans 8:31|courage,assurance
Romans 12:9-10|love,kindness
Romans 12:15|compassion,community
Romans 12:18|peace,kindness
Romans 12:21|goodness,courage
Romans 13:10|love,kindness
1Corinthians 10:13|endurance,hope
1Corinthians 13:8|love,faithfulness
1Corinthians 15:58|steadfast,work
1Corinthians 16:13-14|courage,love
2Corinthians 1:3-4|comfort,compassion
2Corinthians 4:16-18|hope,renewal
2Corinthians 5:7|faith,trust
2Corinthians 5:17|renewal,hope
2Corinthians 9:7|generosity,joy
Galatians 2:20|faith,love
Galatians 5:13|freedom,service
Galatians 6:2|compassion,community
Galatians 6:9|patience,perseverance
Ephesians 2:8-9|grace,gratitude
Ephesians 3:17-19|love,roots
Ephesians 4:2-3|patience,peace
Ephesians 4:29|speech,kindness
Ephesians 5:1-2|love,kindness
Ephesians 6:10|strength
Philippians 1:6|hope,trust
Philippians 2:3-4|humility,kindness
Philippians 3:13-14|perseverance,hope
Philippians 4:8|thoughts,mind
Philippians 4:19|trust,provision
Colossians 3:12-14|compassion,love,kindness
Colossians 3:15|peace,gratitude
Colossians 3:17|gratitude,work
Colossians 4:6|speech,kindness
1Thessalonians 5:11|encouragement,community
1Timothy 4:12|courage,example
1Timothy 6:6-8|contentment
2Timothy 1:7|courage,self-control
Hebrews 4:16|prayer,grace
Hebrews 10:23-24|hope,community
Hebrews 12:1-2|perseverance,faith
Hebrews 13:2|kindness,welcome
Hebrews 13:5-6|contentment,courage
Hebrews 13:8|steadfast,faith
James 1:2-4|patience,perseverance
James 1:19|listening,patience
James 1:22|action,faith
James 3:17|wisdom,peace
James 4:8|prayer,nearness
James 4:10|humility
1Peter 4:8|love,forgiveness
1John 3:18|love,action
1John 4:7-8|love
1John 4:16|love,presence
1John 4:18|love,fear
Revelation 3:20|welcome,presence
Revelation 21:4|hope,comfort
""",
}

import re, random
FILES = {'Psalm': 'Psalms', 'Song of Solomon': 'SongofSolomon'}
# exact fixes for source-text quirks (dataset prints these in capitals): asserted, not blind
CASE_FIXES = {
    ('Psalm', 92, 1, 2): [('IT IS A GOOD THING TO GIVE THANKS UNTO THE LORD, AND TO SING PRAISES UNTO THY NAME, O MOST HIGH:', 'It is a good thing to give thanks unto the LORD, and to sing praises unto thy name, O most High:')],
    ('Proverbs', 22, 1, 1): [('A GOOD name', 'A good name')],
}
DIVINE_CAPS = {'LORD', 'GOD', 'JEHOVAH'}

cache = {}
def raw_verses(fn, ch, a, b):
    if fn not in cache: cache[fn] = json.load(open(os.path.join(SRC, fn + '.json')))
    chap = next((c for c in cache[fn]['chapters'] if c['chapter'] == str(ch)), None)
    if chap is None: sys.exit(f'MISSING chapter {fn} {ch}')
    out = []
    for n in range(a, b + 1):
        t = next((v['text'] for v in chap['verses'] if v['verse'] == str(n)), None)
        if t is None: sys.exit(f'MISSING verse {fn} {ch}:{n}')
        out.append(t.strip())
    return ' '.join(out)

def clean(key, text):
    for old, new in CASE_FIXES.get(key, []):
        assert text.count(old) == 1, ('case fix not found', key, old)
        text = text.replace(old, new)
    text = re.sub(r'\s+', ' ', text.replace('\u00b6', '')).strip()
    text = text.replace('#', '').strip()
    return text

def entry(disp, ch, a, b):
    fn = FILES.get(disp, disp.replace(' ', ''))
    return clean((disp, ch, a, b), raw_verses(fn, ch, a, b))

# ---- V1 (frozen): round-robin across categories
buckets = {k: [r for r in R_V1 if r[0] == k] for k in CATS}
order = []
while any(buckets.values()):
    for k in CATS:
        if buckets[k]: order.append(buckets[k].pop(0))

# ---- V2: spread each category evenly so consecutive days vary
v2 = {}
for cat, block in V2_TEXT.items():
    items = []
    for line in block.strip().splitlines():
        ref, tags = line.split('|')
        m = re.match(r'^(.+?) (\d+):(\d+)(?:-(\d+))?$', ref.strip())
        assert m, line
        disp, ch, a = m.group(1), int(m.group(2)), int(m.group(3))
        b = int(m.group(4)) if m.group(4) else a
        file = FILES.get(disp, disp.replace(' ', ''))
        items.append((cat, file, disp, ch, a, b, tags.strip()))
    random.Random(7 + cat).shuffle(items)
    v2[cat] = items
keyed = []
for cat, items in v2.items():
    for i, it in enumerate(items): keyed.append(((i + 0.5) / len(items), cat, it))
keyed.sort(key=lambda x: (x[0], x[1]))
order2 = [it for _, _, it in keyed]

def disp_book(d):  # display names as used in V1 (Psalm / Proverbs / 1 Corinthians ...)
    return re.sub(r'^(\d)([A-Za-z])', r'\1 \2', d)

def canon(it): return (it[2].replace(' ', ''), it[3], it[4], it[5])
seen = []
for it in order + order2:
    c = canon(it)
    c = ('Psalm' if c[0] == 'Psalms' else c[0],) + c[1:]
    for p in seen:
        if p[0] == c[0] and p[1] == c[1] and not (c[3] < p[2] or c[2] > p[3]):
            sys.exit(f'DUPLICATE/OVERLAP {c} vs {p}')
    seen.append((c[0], c[1], c[2], c[3]))

vs = []
for i, (cat, fn, disp, ch, a, b, tags) in enumerate(order + order2, 1):
    ref = f'{disp_book(disp)} {ch}:{a}' + (f'-{b}' if b != a else '') + ' (KJV)'
    text = entry(disp, ch, a, b)
    for tok in re.findall(r'\b[A-Z]{2,}\b', text):
        if tok not in DIVINE_CAPS: sys.exit(f'UNEXPECTED ALL-CAPS {tok!r} in {ref}: {text}')
    if '  ' in text or not text or len(text) > 450: sys.exit(f'BAD TEXT {ref} len={len(text)}')
    vs.append({'id': 2000 + i, 'categoryId': cat, 'dayNumber': i, 'originalText': '',
               'translatedText': text, 'sourceReference': ref, 'tags': tags})

# v1 must be unchanged
if os.path.exists('/tmp/christianity_v1.json'):
    old = json.load(open('/tmp/christianity_v1.json'))['verses']
    assert [(v['id'], v['dayNumber'], v['sourceReference'], v['translatedText']) for v in old] == \
           [(v['id'], v['dayNumber'], v['sourceReference'], v['translatedText']) for v in vs[:len(old)]], 'V1 CHANGED'

counts = {k: sum(1 for v in vs if v['categoryId'] == k) for k in CATS}
doc = {'religion': 'Christianity', 'language': 'en', 'version': 2,
       'source': 'King James Version (public domain in the United States)',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f: json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'verses', counts, 'longest', max(len(v['translatedText']) for v in vs))
