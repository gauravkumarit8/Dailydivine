# Content expansion plan (written 2026-11-19)

Why this exists: a daily-verse app lives or dies on its content, and content is now the weakest part of DailyDivine (see the strict review). This plan says what to add, from where, in what order, who checks it, and what we deliberately do not do.

Legal notes below are my understanding of copyright, not legal advice; each source is re-checked when its round starts, and recorded in `licenses/CONTENT_SOURCES.md`.

## 1. Where we are

| Tradition | Passages now | Days before the first repeat | Notes |
|---|---|---|---|
| Hinduism | 95 (46 Arnold + 49 Gita) | 95 (Hindi readers: 49) | Sanskrit on every passage; Hindi meanings for 49 |
| Christianity | 357 (KJV, by reference) | 357 | About a year |
| Islam | 34 (Pickthall) | 34 | Under 5 weeks: the biggest gap |
| Buddhism | 57 (Müller) | 57 | Hand-copied, never spot-checked |
| Sikhism, Judaism, Spiritual | 0 | n/a | Shown in onboarding as "Coming soon" |

## 2. Decisions this plan rests on
1. **Launch with four traditions, not seven.** Three "Coming soon" rows make a new app look unfinished. Hide empty traditions at launch; add them later as content updates (an app update brings new verses; the migration code already supports that).
2. **Targets.** Before the closed test: Hinduism >= 200, Christianity 365, Islam >= 180, Buddhism >= 200. After launch, top every tradition up to 365 so a year never repeats.
3. **Pull by reference from a full public-domain text, never type scripture.** Same method as the KJV round: reference list -> builder script -> asserted checks (missing reference, duplicate or overlapping passage, unexpected capitals, double spaces, overlong text) -> frozen hash for already-shipped verses. Hand-copied text (Buddhism today) is replaced by this method.
4. **Nothing ships without a human reviewer from the tradition.** I choose and format; a person who practises it signs off. The owner reviewed the Hindi and the Arnold mapping; Muslim, Buddhist, Christian (and later Jewish and Sikh) reviewers are still needed.
5. **No AI-written scripture.** Only the Gita meanings (English and Hindi) are our own writing, because the Sanskrit is ancient and the owner reads Hindi to review. We do not write our own Quran, Bible, Dhammapada, Torah or Guru Granth translations.

## 3. Sources, legal status and whether I can fetch them

| Tradition | Source | Status (my understanding) | Fetchable from my sandbox? |
|---|---|---|---|
| Islam | Pickthall, *The Meaning of the Glorious Koran* (1930), full text, all 6,236 verses | Published 1930: US public domain since 1 Jan 2026; Pickthall died 1936, so also public domain in India. Dataset github.com/Kristories/quran (MIT for the repository; the translation text is the public-domain part) | **Yes** (cloned and checked: all 114 chapters in `fixtures/en/pickthall`) |
| Islam (optional) | Arabic text above the meaning, like the Sanskrit | Needs its own licence check (Tanzil terms: attribution, text unchanged) and right-to-left layout in the app, the share image and the wallpaper | Not checked |
| Buddhism | Müller, *Dhammapada* (SBE 10, 1881): 423 verses; also Fausböll, *Sutta Nipata* (SBE 10, 1881) | Public domain | Project Gutenberg is blocked for me. **You download** the text once (script provided in the round) and commit it to `tools/sources/` |
| Hinduism | The Gita's 700 Sanskrit verses (already have), meanings written by us; optionally Upanishads (Müller, SBE 1879/1884) and Yoga Sutras (Johnston, 1912) | Public domain | Gita: yes. Others: user download |
| Christianity | KJV (have it) | Public domain in the US | Done; +8 passages tops up to 365 |
| Judaism | JPS 1917 *Holy Scriptures* | Public domain | Sefaria's export is not in git and the file host is blocked for me. **You download** via a script I write, or I use another mirror if one is on GitHub |
| Sikhism | Macauliffe, *The Sikh Religion* (1909), for Japji, Sukhmani and similar | Public domain (Macauliffe died 1913) | User download. Modern translations (Khalis Foundation, SikhNet, Dr Sant Singh Khalsa) need **written permission**; we do not use them without it |
| Spiritual | Marcus Aurelius (Long, 1862), Epictetus, Tao Te Ching (Legge, 1891), Gibran's *The Prophet* (1923, US public domain), Emerson; Rumi (Nicholson) volume by volume | Public domain; Rumi/Nicholson volumes differ by year, so each is checked | User download |

## 4. Order of work (each item is one verified, logged round with a zip)

1. **Islam +150 to 184** (sandbox only, no downloads). Existing categories (Mercy & Forgiveness, Patience & Trust, Guidance & Knowledge, Character & Kindness) plus enough new ones to stay readable. Selection rules: whole verses or 1-3 consecutive verses; no legal rulings; no battle-context verses lifted out of context; nothing that needs a commentary to be read safely. **Reviewer needed before release.**
2. **Hide empty traditions at launch** (small code change; `ReligionMeta.hasContent` already knows which are empty).
3. **Buddhism rebuild from a downloaded full text**: replace the 57 hand-copied passages (keeping their ids and day numbers, frozen by hash) and extend to >= 200 from the Dhammapada and the Sutta Nipata.
4. **Christianity +8 to 365**, plus the reviewer pass.
5. **Hinduism: more Gita verses in batches of about 45**, each with Sanskrit, English and Hindi, each sent to the owner for review (as with the first 50). Four batches reach about 275 verses.
6. After launch: **Judaism, Spiritual, Sikhism** (in that order, by availability of sources and reviewers).

## 5. Quality bar for every passage
- 1-5 sentences, at most about 400 characters; stands alone; no cut-off mid-sentence.
- Mixed tone across a week: not only comfort, not only duty.
- Passes `ContentIntegrityTest` (unique ids, consecutive day numbers, categories and counts match, no empty text), plus a frozen-hash test over everything already shipped.
- Credited in `licenses/CONTENT_SOURCES.md`, the in-app credits and the website.
- Verse numbering checked against a printed or authoritative edition (the Gita chapter-13 numbering error was found this way).

## 6. Risks, stated plainly
- **Archaic English.** Pickthall, KJV, Arnold and Müller read as old-fashioned ("Lo!", "thee"). It is the price of public-domain text. We do not modernise scripture; a short plain-English "meaning" line of our own (as the Gita has) is possible only where a reviewer from the tradition agrees.
- **Sensitive selection.** Choosing Quran or Torah verses is an editorial act. Reviewer sign-off is not optional.
- **Reviewer availability is the bottleneck**, not my speed. Finding one reviewer per tradition is the owner's task.
- **Content updates reach users only through app updates** (no internet permission by design).
- **Volume of review work.** Roughly 700 new passages is a lot to check; batches of 40-50 keep it humane.

## 7. Decisions needed from the owner
1. OK to hide the three empty traditions at launch?
2. Start with Islam now (it needs nothing from you)?
3. Can you get one reviewer each for Islam, Buddhism and Christianity? (Names are not needed in the app; a credit is optional.)
4. Do you want Arabic shown above the Quran meaning, as the Sanskrit is for the Gita? (Better product, more work: right-to-left text everywhere.)
