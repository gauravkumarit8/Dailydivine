#!/usr/bin/env python3
"""Builds app/src/main/assets/gita/gita_reader.json: the whole Bhagavad Gita for the in-app reader.

Per passage: the Sanskrit, Annie Besant's English (1922, public domain), and, for the verses that have
them, DailyDivine's own plain-words meanings in English and Hindi (reviewed by the project owner).

Inputs
  tools/gita/besant_1922.json  (tools/gita/parse_besant.py, from English Wikisource, CC BY-SA 4.0)
  tools/gita/gita_pilot.json   (our own reviewed meanings)
  gita/gita data/verse.json    (github.com/gita/gita, Unlicense): only the Sanskrit text is used
Usage: python3 tools/build_gita_reader.py /path/to/gita/data/verse.json

Numbering: the standard 700-verse numbering of printed Gitas. Besant and the dataset both count Arjuna's
question as chapter 13 verse 1, so their chapter-13 numbers are one higher; the passage that holds
that question is labelled "Introduction" (id 13.0) and later chapter-13 verses are numbered n-1.
"""
import json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', 'app', 'src', 'main', 'assets', 'gita', 'gita_reader.json')
besant = json.load(open(os.path.join(HERE, 'gita', 'besant_1922.json'), encoding='utf-8'))
pilot = {r['ref']: r for r in json.load(open(os.path.join(HERE, 'gita', 'gita_pilot.json'), encoding='utf-8'))}
dataset = {(int(x['chapter_number']), int(x['verse_number'])): x['text']
           for x in json.load(open(sys.argv[1], encoding='utf-8'))}
NO_OWN_MEANING = {'3.35'}  # "dharma" = one's own duty can be misread as "stay in your own religion"

# Transcription typos in Wikisource's text that are certain (a non-word or nonsense in context). Every other
# disagreement with a second copy stays as Wikisource has it and is on besant_proofread_list.md for a scan check.
FIXES = [
    (3, 20, 'welfare of he world', 'welfare of the world'),
    (6, 43, 'belonging to this former body', 'belonging to his former body'),
    (9, 16, 'I the ablation;', 'I the oblation;'),
    (10, 26, 'of all tree;', 'of all trees;'),
    (11, 25, 'Mercy, O God! re of all the worlds!', 'Mercy, O God! Refuge of all the worlds!'),
]
TITLE_FIX = {'INDESCTRUCTIBLE': 'INDESTRUCTIBLE'}
SMALL = {'of', 'the', 'and', 'by', 'in', 'between', 'from', 'to', 'for', 'a', 'an'}
SPEAKERS = r'(श्री ?भगवानुवाच|अर्जुन उवाच|धृतराष्ट्र उवाच|सञ्जय उवाच|संजय उवाच)'
JOIN_FIXES = {(2, 20): ('कदाचि न्नायं', 'कदाचिन्नायं'), (2, 22): ('जीर्णा न्यन्यानि', 'जीर्णान्यन्यानि')}


def title_case(t):
    words = [TITLE_FIX.get(w, w).lower() for w in t.split()]
    return ' '.join(w if (i and w in SMALL) else w[:1].upper() + w[1:] for i, w in enumerate(words)) \
        .replace('Sankhya', 'Sankhya')


def std(ch, n):
    """Besant/dataset number -> standard number; 0 means Arjuna's question that opens chapter 13."""
    return n - 1 if ch == 13 else n


def nbsp_danda(t):
    return t.replace(' \u0964', '\u00a0\u0964').replace(' \u0965', '\u00a0\u0965')


def spelling_key(t):
    """Besant writes the nasal conjuncts in full (सङ्ख्ये), the dataset uses the anusvara (संख्ये), and the
    avagraha is sometimes omitted: compare with those differences removed."""
    return re.sub(r'[ङञणनम]्(?=[क-ह])', 'ं', t.replace('ऽ', ''))


def sanskrit_of_verse(ch, n, reference_sa):
    """The dataset breaks a verse into its four quarter-lines. A line break is usually a word boundary, but
    sometimes falls inside a word (kadachi|nnayam); the spacing is then settled by Besant's own Sanskrit."""
    t = re.sub(r'।।\s*\d+\.\d+\s*।+', '', dataset[(ch, n)])
    t = re.sub(SPEAKERS, '\n', t)
    lines = [l.strip() for l in t.split('\n') if l.strip()]
    s = lines[0]
    ref_key = spelling_key(reference_sa)
    for nxt in lines[1:]:
        tail, head = s.rstrip('।॥ ')[-3:], nxt[:3]
        glued = spelling_key(tail + head) in ref_key and spelling_key(tail + ' ' + head) not in ref_key
        s += ('' if glued else ' ') + nxt
    if (ch, n) in JOIN_FIXES:
        old, new = JOIN_FIXES[(ch, n)]
        s = s.replace(old, new)
    halves = [h.strip() for h in re.sub(r'\s+', ' ', s).split('।') if h.strip()]
    if len(halves) < 2:
        sys.exit(f'Sanskrit of {ch}.{n} did not split into two halves: {s!r}')
    return '\n'.join(h + ' ।' for h in halves[:-1]) + '\n' + halves[-1] + ' ॥'


chapters = []
for ch in range(1, 19):
    title = title_case(besant['chapter_titles'][str(ch)])
    passages = []
    for p in (v for v in besant['verses'] if v['chapter'] == ch):
        first, last = p['nums'][0], p['nums'][-1]
        ref_sa = re.sub(r'[\s॥।\d०-९]+', lambda m: ' ' if ' ' in m.group(0) else '', p['sanskrit'])
        sa = '\n'.join(sanskrit_of_verse(ch, n, p['sanskrit'].replace('\n', ' ')) for n in p['nums'])
        en = p['en']
        for fch, fv, old, new in FIXES:
            if fch == ch and fv == first:
                if old not in en:
                    sys.exit(f'FIX not found in {ch}.{first}: {old!r}')
                en = en.replace(old, new)
        s_first, s_last = std(ch, first), std(ch, last)
        if s_first == 0 and len(p['nums']) == 1:
            pid, label = f'{ch}.0', 'Arjuna\u2019s question'
        else:
            s_first = max(s_first, 1)
            pid = f'{ch}.{s_first}'
            label = f'Verse {s_first}' if s_first == s_last else f'Verses {s_first}\u2013{s_last}'
        item = {'id': pid, 'label': label, 'sanskrit': nbsp_danda(sa), 'english': en}
        spk = (p.get('speaker_en') or '').strip()
        if spk:
            item['speaker'] = spk
        if len(p['nums']) == 1:
            row = pilot.get(f'{ch}.{first}')
            if row and f'{ch}.{first}' not in NO_OWN_MEANING:
                item['meaningEn'] = row['english']
                item['meaningHi'] = row['hindi']
        passages.append(item)
    ids = [x['id'] for x in passages]
    assert len(ids) == len(set(ids)), f'duplicate ids in chapter {ch}'
    chapters.append({'number': ch, 'title': title, 'passages': passages})

doc = {'version': 1,
       'source': 'Sanskrit: public domain (github.com/gita/gita, Unlicense). English: Annie Besant, The Bhagavad-Gita, 4th ed. (1922), '
                 'public domain; transcription English Wikisource "Bhagavad-Gita (Besant 4th)", CC BY-SA 4.0. '
                 'Plain-words meanings: written for DailyDivine and reviewed by its author.',
       'chapters': chapters}
os.makedirs(os.path.dirname(OUT), exist_ok=True)
with open(OUT, 'w', encoding='utf-8') as f:
    json.dump(doc, f, ensure_ascii=False, separators=(',', ':'))
n_pass = sum(len(c['passages']) for c in chapters)
n_own = sum(1 for c in chapters for p in c['passages'] if 'meaningEn' in p)
print(f'{n_pass} passages in 18 chapters; {n_own} with our own meanings; {os.path.getsize(OUT) // 1024} KB -> {OUT}')
