# Public-API research (public-apis/public-apis and related), 2026-10-12

Question: can the free APIs listed at github.com/public-apis/public-apis help DailyDivine?
Limits of this research: read from the list, dataset files and documentation. The sandbox cannot call live
endpoints (network allow-list), so uptime, rate limits and exact terms must be re-checked at integration time.

## Finding 1: the list is a directory of APIs, not of content licences
No "Religion" category; religion-related entries are scattered (Bible, Quran, Bhagavad Gita, Gurbani, calendars).
The aggregated Quran dataset (fawazahmed0/quran-api, 492 editions, 52 English) has **no licence field** for any
edition and mixes modern copyrighted translations (Abdel Haleem, Khattab, Itani...) with public-domain ones.
Using it blindly would be a copyright risk. Sefaria is the opposite: it records a licence per text version.

## Finding 2: scripture should not be a runtime dependency
DailyDivine's core features (alarm, daily verse, wallpaper, notifications) must work offline at 5 AM in airplane
mode; public APIs change, rate-limit and disappear; and translation rights are per version. Approach already
used for KJV, Arnold, Muller and Pickthall: use APIs/datasets at **build time**, with a **licence gate**
(only Public Domain / CC0, or CC-BY with attribution), keep source + licence in `CONTENT_SOURCES.md`, ship JSON.

## Candidates
| Source | Useful for | Licence / terms finding | Verdict |
|---|---|---|---|
| fawazahmed0/quran-api (Pickthall edition) | Islam content | Pickthall (1930) public domain; dataset itself has no licence data | **Used** (build time, English only) |
| Sefaria (export / API) | Judaism content | Per-version licence. JPS 1917 = Public Domain; JPS 1985 and 2023 = CC-BY-NC (non-commercial: not usable) | **Use JPS 1917**. Data lives in a Google Cloud bucket the sandbox cannot reach: allow `storage.googleapis.com` to proceed |
| BaniDB / GurbaniNow (Sikh) | Sikhism | Software open source; the common English translation (Dr Sant Singh Khalsa) is a copyrighted work; Manmohan Singh's is SGPC-copyrighted | **Blocked on permission**: ask Khalis Foundation / SikhNet in writing, or use PD Macauliffe (1909) excerpts |
| bible-api.com, api.bible | Christianity | KJV/WEB are public domain; api.bible needs a key and has per-translation terms | Not needed (KJV already shipped) |
| Bhagavad Gita APIs (bhagavadgitaapi.in etc.) | Hinduism | Multi-author translations, mostly modern/copyrighted | Not needed (Arnold 1885 shipped) |
| Hebcal | Jewish holidays | Free JSON | Optional; see Finding 3 |
| Church calendar APIs | Christian feasts | Some are http-only | Prefer computing locally |
| Quote APIs (ZenQuotes etc.) | "Quote of the day" | Attribution required, rate-limited; quote provenance unclear | **Avoid** for scripture; for a Spiritual category curate PD classics (Marcus Aurelius, Tao Te Ching, Rumi/Nicholson) |

## Finding 3: most "helpful" non-content features can be computed offline
- Islamic calendar (Ramadan, Eid): `java.time.chrono.HijrahChronology` (built in).
- Jewish calendar and holidays: `android.icu.util.HebrewCalendar` (built in, API 24+).
- Easter and Christian feasts: the Computus algorithm (pure code).
- Prayer times (Fajr..Isha) and sunrise/sunset: open-source libraries (e.g. Adhan, MIT; commons-suncalc, Apache 2.0). Needs location permission.
- Moon phase (Purnima/Amavasya/Uposatha): pure maths.
- Hindu festivals follow a lunisolar panchang: the one place an API or dataset is genuinely needed.
Offline computation keeps the app working with no network and adds no data-sharing.
