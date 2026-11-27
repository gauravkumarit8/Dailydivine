#!/usr/bin/env python3
"""Parses the 18 Wikisource pages of Annie Besant's *The Bhagavad-Gita* (4th ed., 1922) that
tools/fetch_besant_pages.ps1 downloads, into tools/gita/besant_1922.json.

Besant's translation is public domain (1922; she died in 1933). The Wikisource transcription is
CC BY-SA 4.0, credit: "English Wikisource, Bhagavad-Gita (Besant 4th)", with the revision ids recorded below.

Usage: python3 tools/gita/parse_besant.py /path/to/besant_raw [path/to/gita/data/verse.json]
Needs: pip install beautifulsoup4 lxml

Each verse (or group of verses Besant translates together) is read from the Sanskrit block that starts it.
The verse numbers come from the Devanagari numerals in that block (॥ १२ ॥), which are reliable; the
English "(12)" marker on the page is sometimes malformed in Wikisource's markup. If a verse.json from
github.com/gita/gita is given, Besant's Sanskrit is compared with it verse by verse as a cross-check.

Numbering is Besant's, which equals the dataset's: Arjuna's question opens chapter 13 as verse 1, so
chapter 13 has 35 verses and every later chapter-13 number is one higher than in printed 700-verse Gitas
(subtract one from chapter-13 verses 2 and up for the standard number).
"""
import json, os, re, sys, unicodedata
from bs4 import BeautifulSoup, Tag

DEV = str.maketrans('०१२३४५६७८९', '0123456789')
EXPECTED = [47, 72, 43, 42, 29, 47, 30, 28, 34, 42, 55, 20, 35, 27, 20, 24, 28, 78]  # 701 (chapter 13 = 35)


def clean_text(node):
    node = BeautifulSoup(str(node), 'lxml')
    for t in node.select('style, link, script, sup.reference, span.pagenum, span.ws-pagenum, span.wst-floatright'):
        t.decompose()
    for br in node.select('br'):
        br.replace_with(' ')
    # No separator between text nodes: a page break or a footnote mark can fall in the middle of a word
    # ("griev" + page break + "ed"), and the page's own spaces already separate the words.
    return re.sub(r'\s+', ' ', node.get_text('')).strip()


def sanskrit_lines(lang_div):
    """Lines of a Sanskrit block. In Wikisource's markup the first line is sometimes plain text inside the
    block and sometimes a <p>, so the block is flattened with line breaks at every <p> and paragraph break
    (reading only the <p> tags lost the first half of many verses)."""
    node = BeautifulSoup(str(lang_div), 'lxml')
    for t in node.select('style, link, script, sup.reference, span.pagenum, span.ws-pagenum'):
        t.decompose()
    for t in node.select('p, div.paragraphbreak, br'):
        t.insert_before('\n')
        t.insert_after('\n')
    return [re.sub(r'\s+', ' ', l).strip() for l in node.get_text().split('\n') if l.strip()]


def events(root):
    out = []

    def walk(node):
        for c in node.children:
            if not isinstance(c, Tag):
                continue
            cls = set(c.get('class') or [])
            if c.name in ('style', 'link', 'script'):
                continue
            for pg in c.select('span.pagenum'):
                out.append(('PG', pg.get('data-page-name')))
            if 'wst-block-center' in cls and c.select_one('.wst-lang'):
                out.append(('SA', sanskrit_lines(c.select_one('.wst-lang'))))
            elif c.name == 'p':
                marker = c.select_one('span.wst-floatright')
                out.append(('P', clean_text(c), None if marker is None else marker.get_text(strip=True)))
            elif 'wst-center' in cls:
                out.append(('SPK', clean_text(c)))
            elif c.name == 'ol' or 'references' in cls or 'mw-references-wrap' in cls:
                continue
            elif 'pagenum' in cls:
                continue  # already recorded just above
            else:
                walk(c)
    walk(root)
    return out


chapter_titles = {}


def parse_chapter(ch, html):
    root = BeautifulSoup(html, 'lxml').select_one('div.mw-parser-output')
    verses, cur, speaker_en, problems, notes = [], None, None, [], []
    page = None
    for ev in events(root):
        if ev[0] == 'SA':
            if cur and cur['en_parts']:
                problems.append(f'{ch}: Sanskrit block started before the English of {cur["nums"]} ended')
            lines = ev[1]
            speaker_sa = None
            if lines and re.search(r'उवाच\s*[।|]*\s*$', lines[0]) and len(lines) > 1:
                speaker_sa, lines = lines[0], lines[1:]
            joined = ' '.join(lines)
            nums = [int(n.translate(DEV)) for n in re.findall(r'॥\s*([०-९]+)\s*॥', joined)]
            cur = {'chapter': ch, 'nums': nums, 'sanskrit': ' '.join(lines), 'speaker_sa': speaker_sa,
                   'speaker_en': speaker_en, 'en_parts': [], 'pages': [page] if page else []}
            speaker_en = None
        elif ev[0] == 'PG':
            page = ev[1]
            if cur is not None and page not in cur['pages']:
                cur['pages'].append(page)
        elif ev[0] == 'SPK':
            who = ev[1].rstrip(':').strip() if ev[1] else None
            if cur is not None and not cur['en_parts']:
                cur['speaker_en'] = who      # the English speaker line follows the Sanskrit block it belongs to
            else:
                speaker_en = who
        elif ev[0] == 'P' and cur is not None:
            if ev[1]:
                cur['en_parts'].append(ev[1])
            if ev[2] is not None:  # the verse marker closes this verse
                digits = [int(x) for x in re.findall(r'\d+', ev[2])]
                cur['marker'] = ev[2]
                if digits and cur['nums'] and digits[0] != cur['nums'][0] and digits[-1] != cur['nums'][-1]:
                    notes.append(f'{ch}: English marker {ev[2]!r} but the Sanskrit says {cur["nums"]}: Wikisource marker typo; the Sanskrit number is used')
                if not cur['nums']:
                    cur['nums'] = list(range(digits[0], (digits[-1] if len(digits) > 1 else digits[0]) + 1)) if digits else []
                cur['en'] = ' '.join(cur.pop('en_parts'))
                verses.append(cur)
                cur = None
    title = None
    evs = events(root)
    for i, ev in enumerate(evs):
        if ev[0] == 'P' and ev[1].startswith('Thus in the glorious Upanishads'):
            for nxt in evs[i + 1:i + 3]:
                if nxt[0] == 'SPK' and nxt[1]:
                    title = nxt[1].rstrip('.').strip()
                    break
    chapter_titles[ch] = title
    if cur and cur['en_parts'] and cur['nums']:
        problems.append(f'{ch}: last verse never closed {cur["nums"]}')
    return verses, problems, notes


def norm_sa(t):
    t = unicodedata.normalize('NFC', t)
    t = re.sub(r'[\u200b-\u200f\u2060\ufeff]', '', t)
    t = re.sub(r'[॥।|\d०-९\s\-–,.;:()\[\]]', '', t)
    return t.replace('ऽ', '')


def main(raw_dir, verse_json=None):
    ds = {}
    if verse_json:
        for x in json.load(open(verse_json, encoding='utf-8')):
            t = re.sub(r'।।\s*\d+\.\d+\s*।+', '', x['text'])
            ds[(int(x['chapter_number']), int(x['verse_number']))] = norm_sa(t)
    all_v, problems, revs, all_notes = [], [], {}, []
    for ch in range(1, 19):
        d = json.load(open(os.path.join(raw_dir, f'discourse_{ch:02d}.json'), encoding='utf-8'))['parse']
        revs[ch] = d['revid']
        vs, pr, nt = parse_chapter(ch, d['text'])
        all_v += vs
        problems += pr
        all_notes += nt
    # structure checks
    from collections import defaultdict
    per = defaultdict(list)
    for v in all_v:
        per[v['chapter']] += v['nums']
    for ch in range(1, 19):
        nums = sorted(per[ch])
        if nums != list(range(1, EXPECTED[ch - 1] + 1)):
            missing = sorted(set(range(1, EXPECTED[ch - 1] + 1)) - set(nums))
            extra = sorted({n for n in nums if nums.count(n) > 1} | (set(nums) - set(range(1, EXPECTED[ch - 1] + 1))))
            problems.append(f'chapter {ch}: expected 1..{EXPECTED[ch-1]}; missing {missing}; duplicated/extra {extra}')
    # cross-check Besant's Sanskrit against the dataset
    mism = []
    if ds:
        for v in all_v:
            want = ''.join(ds.get((v['chapter'], n), '') for n in v['nums'])
            if norm_sa(v['sanskrit']) != want:
                mism.append((v['chapter'], v['nums']))
    out = {'source': 'Annie Besant, The Bhagavad-Gita, 4th ed. (1922). Public domain. Text: English Wikisource, Bhagavad-Gita (Besant 4th), CC BY-SA 4.0.',
           'wikisource_revisions': revs, 'chapter_titles': chapter_titles, 'verses': all_v}
    dest = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'besant_1922.json')
    json.dump(out, open(dest, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    cov = sum(len(v['nums']) for v in all_v)
    print(f'{len(all_v)} passages covering {cov} verse numbers -> {dest}')
    print('problems:', len(problems))
    for n in all_notes:
        print('   note:', n)
    for p in problems[:25]:
        print('  ', p)
    if ds:
        print('Sanskrit differs from the dataset in', len(mism), 'passages', mism[:20])


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)
