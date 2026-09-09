---
title: "DailyDivine — Product Requirements Document"
subtitle: "Daily Devotional & Prayer Alarm"
---

# DailyDivine — Product Requirements Document

**Platform:** Android (Native – Kotlin)
**IDE:** GitHub Codespaces
**Version:** 1.1.0
**Status:** Draft (pre-development review incorporated)

> **Version 1.1.0 note:** This revision incorporates five changes identified during pre-development review, covering alarm reliability, Android 12+ permission handling, content-update safety, EU ad consent sequencing, and an ASO workstream. Each change is marked **[UPDATED v1.1]** or **[NEW v1.1]** at the point it appears below.

## Table of Contents

1. Executive Summary
2. Product Vision & Goals
3. Target Audience
4. User Personas
5. Competitive Analysis
6. Feature Requirements (Detailed)
7. User Stories & Acceptance Criteria
8. Information Architecture
9. Screen-by-Screen Specifications
10. Database Design
11. Content Strategy
12. Audio System Architecture
13. Alarm System Architecture
14. Notification Strategy
15. Monetization Strategy
16. Tech Stack & Dependencies
17. GitHub Codespaces Setup
18. Project Structure
19. API Contracts (Internal)
20. UI/UX Design Guidelines
21. Accessibility Requirements
22. Performance Requirements
23. Security & Privacy
24. Testing Strategy
25. Release Plan
26. Analytics & KPIs
27. Post-Launch Maintenance Plan
28. Risk Assessment
29. Timeline & Milestones
30. Appendix

---

# 1. Executive Summary

## 1.1 Product Overview
DailyDivine is a multi-faith daily devotional and prayer alarm Android application that delivers personalized spiritual content to users based on their selected religion. The app combines daily verse delivery, audio playback, smart alarm functionality, and streak-based engagement to create a daily spiritual routine for users.

## 1.2 Problem Statement
Millions of people want to start their day with spiritual readings/prayers but:
- Forget to read daily verses consistently
- Cannot find a simple app for their specific faith
- Existing apps are either too complex or too generic
- No app combines alarm clock + devotional content seamlessly
- Most devotional apps require internet connectivity

## 1.3 Proposed Solution
A lightweight, offline-first Android app that:
- Supports 7 major religions/spiritual paths
- Delivers a unique verse/quote daily for 2+ years
- Functions as a smart alarm that wakes users with sacred audio
- Uses Text-to-Speech to read verses aloud
- Tracks streaks to build daily habits
- Requires ZERO internet after initial install
- Needs minimal updates (content is pre-bundled)

## 1.4 Key Differentiators

| Feature | Competitors | DailyDivine |
|---|---|---|
| Multi-faith support | Single religion | 7 religions |
| Alarm integration | No | Yes, deeply integrated |
| Offline audio | Needs internet | 100% offline |
| Streak system | Basic/None | Gamified with badges |
| TTS verse reading | Rare | Built-in |
| Home screen widget | Rare | Yes |
| Server dependency | Yes | No server needed |

## 1.5 Success Metrics (First Year)

| Metric | Target |
|---|---|
| Total Downloads | 100,000+ |
| Daily Active Users (DAU) | 15,000+ |
| Day 7 Retention | 40%+ |
| Day 30 Retention | 25%+ |
| Average Session Duration | 3+ minutes |
| Monthly Revenue | $500–$2,000 |
| Play Store Rating | 4.5+ stars |
| Premium Conversion Rate | 3–5% |

---

# 2. Product Vision & Goals

## 2.1 Vision Statement
"To be the first app people interact with every morning, starting their day with peace, purpose, and spiritual connection — regardless of their faith."

## 2.2 Mission Statement
"Deliver daily spiritual wisdom through a beautiful, simple, and reliable app that respects all faiths equally and works without internet connectivity."

## 2.3 Product Goals

**Primary Goals:**
- G1: Build a high-retention daily-use app
- G2: Generate consistent passive income through ads and IAP
- G3: Require minimal maintenance and updates post-launch
- G4: Achieve organic growth through word-of-mouth and ASO — see Section 25 for the concrete ASO workstream added in v1.1.

**Secondary Goals:**
- G5: Build a portfolio-worthy Android project
- G6: Learn and implement advanced Kotlin/Android concepts
- G7: Create a foundation for potential iOS expansion
- G8: Build a user base for future app launches

## 2.4 Non-Goals (What this app will NOT do)
- NG1: Will NOT be a social platform (no user-to-user interaction)
- NG2: Will NOT require user accounts or sign-up
- NG3: Will NOT stream audio from servers
- NG4: Will NOT provide live religious services
- NG5: Will NOT include community/forum features
- NG6: Will NOT collect personal data beyond basic preferences
- NG7: Will NOT require any backend server infrastructure

---

# 3. Target Audience

## 3.1 Primary Audience

| Attribute | Description |
|---|---|
| Age | 18–65 years |
| Gender | All genders |
| Location | India, USA, UK, Middle East, SEA |
| Tech Savviness | Basic to Intermediate smartphone user |
| Religious Affinity | Actively practicing or seeking |
| Pain Point | Want daily spiritual routine |
| Behavior | Uses phone first thing in morning |

## 3.2 Secondary Audience
- Parents wanting spiritual content for family
- Elderly users needing simple, large-text interfaces
- New converts exploring their faith
- Meditation/mindfulness enthusiasts
- People going through difficult life phases

## 3.3 Market Size Estimation

| Religion | Global Followers | Smartphone Users (est.) |
|---|---|---|
| Christianity | 2.4 billion | ~1.2 billion |
| Islam | 1.9 billion | ~800 million |
| Hinduism | 1.2 billion | ~500 million |
| Buddhism | 500 million | ~200 million |
| Sikhism | 30 million | ~15 million |
| Judaism | 15 million | ~10 million |
| Spiritual | 500+ million | ~300 million |

- Total Addressable Market: ~3 billion smartphone users
- Serviceable Market (English + Hindi + Arabic): ~500 million
- Target Market (Active seekers): ~50 million
- Realistic Target (Year 1): 100,000 downloads

---

# 4. User Personas

**Persona 1: "Morning Ritualist Rahul"**
Age 32, Male, Mumbai, India · Hinduism · Software Engineer. Wakes at 5:30 AM, wants to read Gita shloka. Pain point: forgets to read; gets distracted by phone. Need: auto-alarm that reads verse aloud. Quote: *"I want to start my day with God, not WhatsApp."*

**Persona 2: "Faithful Mother Maria"**
Age 45, Female, Texas, USA · Christianity · Homemaker. Reads Bible daily, shares verses with family. Pain point: current apps are cluttered with too many features. Need: simple daily verse + share button. Quote: *"I just want one verse a day, beautifully presented."*

**Persona 3: "Young Seeker Ahmed"**
Age 22, Male, London, UK · Islam · University Student. Wants to learn more about Quran. Pain point: intimidated by complex Islamic apps. Need: one Ayah/day with translation + audio. Quote: *"I want to understand my faith, one verse at a time."*

**Persona 4: "Spiritual Explorer Sarah"**
Age 28, Female, San Francisco, USA · Spiritual (non-religious) · Yoga Instructor. Meditates daily, loves wisdom quotes. Pain point: generic quote apps feel shallow. Need: deep spiritual quotes from multiple traditions. Quote: *"I find wisdom in all religions."*

**Persona 5: "Elderly Devotee Gurpreet"**
Age 65, Male, Punjab, India · Sikhism · Retired. Recites Japji Sahib every morning. Pain point: small text, complex navigation in apps. Need: large text, auto-play audio, simple interface. Quote: *"My grandson should set this up for me once."*

---

# 5. Competitive Analysis

## 5.1 Direct Competitors

**Bible Apps**

| App Name | Downloads | Rating | Weakness |
|---|---|---|---|
| YouVersion Bible | 500M+ | 4.8 | Too complex, needs internet |
| Bible Verse of Day | 10M+ | 4.6 | No alarm feature |
| Daily Bible | 5M+ | 4.5 | Ads heavy, slow |

**Hindu Apps**

| App Name | Downloads | Rating | Weakness |
|---|---|---|---|
| Bhagavad Gita App | 10M+ | 4.7 | Full text, not daily |
| Daily Shloka | 100K+ | 4.2 | Outdated UI, no audio |
| Hindu Calendar | 5M+ | 4.4 | Calendar focus, not verse |

**Islamic Apps**

| App Name | Downloads | Rating | Weakness |
|---|---|---|---|
| Muslim Pro | 100M+ | 4.7 | Too many features |
| Quran Majeed | 50M+ | 4.8 | Full Quran, not daily |
| Daily Ayah | 500K+ | 4.3 | Basic, no alarm |

**Multi-Faith Apps**

| App Name | Downloads | Rating | Weakness |
|---|---|---|---|
| Pray.com | 10M+ | 4.8 | Subscription heavy |
| Calm (meditation) | 100M+ | 4.4 | Not religious |

Few multi-faith daily verse apps exist = **OPPORTUNITY**.

## 5.2 Competitive Advantages of DailyDivine
1. ONLY multi-faith app with integrated alarm system
2. 100% offline (competitors need internet for audio)
3. Simple UX (competitors are feature-bloated)
4. Free with optional one-time purchase (competitors push subscriptions)
5. Streak system (most devotional apps lack gamification)
6. Home screen widget (rare in devotional apps)
7. TTS in local languages (very rare feature)

---

# 6. Feature Requirements (Detailed)

## 6.1 Feature Priority Matrix

| Priority | Feature | Effort | Impact | Phase |
|---|---|---|---|---|
| P0 | Religion selection | Low | High | MVP |
| P0 | Daily verse display | Medium | High | MVP |
| P0 | Streak tracking | Low | High | MVP |
| P0 | Basic alarm | High | High | MVP |
| P0 | TTS verse reading | Medium | High | MVP |
| P0 | Push notifications | Medium | High | MVP |
| P1 | Content library/browse | Medium | Medium | MVP |
| P1 | Bookmarks/Favorites | Low | Medium | MVP |
| P1 | Share verse as image | Medium | High | MVP |
| P1 | AdMob integration | Low | High | MVP |
| P1 | Dark/Light theme | Low | Medium | MVP |
| P2 | Home screen widget | High | High | v1.1 |
| P2 | Audio prayer playback | Medium | Medium | v1.1 |
| P2 | Custom playlists | Medium | Low | v1.1 |
| P2 | In-app purchase (premium) | Medium | High | v1.1 |
| P2 | Daily journal/reflection | Medium | Medium | v1.1 |
| P3 | Verse wallpaper generator | Medium | Medium | v1.2 |
| P3 | Multiple alarms | Medium | Low | v1.2 |
| P3 | Alarm dismiss with reading | Low | Medium | v1.2 |
| P3 | Annual reading plans | Medium | Medium | v1.2 |

## 6.2 Detailed Feature Specifications

### FEATURE F001: Religion Selection & Onboarding

**Description:** First-time users select their religion, preferred language, and set up their morning alarm. This data is stored locally and determines all content shown throughout the app.

**Requirements:**
- F001-R01: App MUST show onboarding flow on first launch only
- F001-R02: App MUST support these religions: Hinduism, Christianity, Islam, Buddhism, Sikhism, Judaism, Spiritual (non-religious)
- F001-R03: Each religion MUST have a unique icon and color theme
- F001-R04: User MUST be able to change religion later in Settings
- F001-R05: Language options MUST be filtered by selected religion
- F001-R06: App MUST NOT require internet during onboarding
- F001-R07: All selections MUST be stored in local SharedPreferences
- F001-R08: Onboarding MUST be completable in under 60 seconds

**Supported Languages per Religion:**
- Hinduism: English, Hindi, Sanskrit, Tamil, Telugu, Kannada
- Christianity: English, Spanish, Portuguese, French, Korean
- Islam: English, Arabic, Urdu, Turkish, Malay
- Buddhism: English, Thai, Japanese, Sinhala
- Sikhism: English, Punjabi, Hindi
- Judaism: English, Hebrew
- Spiritual: English, Hindi, Spanish

**Acceptance Criteria:**
AC1: User can select religion from visual grid of 7 options · AC2: Appropriate languages are shown based on religion selection · AC3: Alarm time picker defaults to 5:30 AM · AC4: User can skip alarm setup (can configure later) · AC5: After onboarding, user lands on Home screen with first verse · AC6: Onboarding data persists across app restarts · AC7: User can re-do onboarding from Settings

### FEATURE F002: Daily Verse System

**Description:** Every day, the app shows a new verse/quote from the user's selected religion. Verses are pre-loaded in the app and assigned to specific day numbers (Day 1 to Day 730).

**Requirements:**
- F002-R01: App MUST show exactly ONE new verse per calendar day
- F002-R02: Verse MUST change at midnight local time
- F002-R03: All verses MUST be stored locally in JSON asset files
- F002-R04: Each verse MUST include: original text (in original language), translation (in selected language), source reference (e.g., "Bhagavad Gita 2.20"), category name, day number (1–730)
- F002-R05: Verse of the day MUST be deterministic (same verse for same day number for all users of same religion)
- F002-R06: After Day 730, verses MUST cycle back to Day 1
- F002-R07: App MUST cache "last shown date" to detect day change
- F002-R08: App MUST work without internet for verse display

> **F002-R13 (NEW — v1.1): Content Migration Manager.** When the app updates, compare the `version` field in the new JSON to the version stored in the database. If the new version is higher, perform a **diff-based insertion of only the new/changed verses**. The existing Room database (streaks, bookmarks, journal entries, `user_profile`) MUST NOT be deleted or overwritten during this process. Rationale: re-inserting JSON wholesale on a content update risks wiping the local database and destroying a user's streak and bookmarks — an existential retention risk for a habit-tracking app. See Section 30-F for the migration task breakdown.

**Verse Selection Algorithm:**
```
dayNumber = (daysSinceUserInstalled % totalVersesForReligion) + 1
todayVerse = verses.find(v => v.dayNumber == dayNumber)
```

**Display Requirements:**
- F002-R09: Verse text MUST be displayed in large, readable font
- F002-R10: Source reference MUST be shown below verse text
- F002-R11: Background MUST use religion-appropriate color theme
- F002-R12: Four action buttons MUST be shown: Play/Listen (TTS), Copy to clipboard, Save/Bookmark, Share

**Acceptance Criteria:**
AC1: Opening app on Day 1 shows verse #1 for selected religion · AC2: Opening app next day shows verse #2 · AC3: Same verse shows all day regardless of how many times opened · AC4: Verse changes exactly at midnight · AC5: Verse displays correctly in offline mode · AC6: Changing religion shows Day 1 verse of new religion

### FEATURE F003: Streak Tracking System

**Description:** Track consecutive days the user opens the app and reads their daily verse. Gamified with milestones and badges.

**Requirements:**
- F003-R01: Streak increments when user opens app on a new calendar day
- F003-R02: Streak resets to 0 if user misses a full calendar day
- F003-R03: Streak data MUST persist in local database
- F003-R04: Current streak count MUST be visible on Home screen
- F003-R05: Progress bar MUST show progress to next milestone
- F003-R06: App MUST show celebration animation on milestones

**Milestone Badges:**

| Days | Badge Name | Description |
|---|---|---|
| 1 | First Step | Started the journey |
| 7 | Week Warrior | 7 consecutive days |
| 21 | Habit Formed | 21 days – habit formed |
| 30 | Monthly Devotee | Full month completed |
| 50 | Half Century | 50 days strong |
| 100 | Centurion | Triple digits! |
| 200 | Deep Roots | Deeply committed |
| 365 | Yearly Devotee | Full year completed |
| 500 | Enlightened | Legendary commitment |
| 730 | Divine Soul | 2 years of devotion |

**Streak Freeze (Premium Feature):**
- F003-R07: Premium users get 1 streak freeze per week
- F003-R08: Streak freeze auto-activates on missed day
- F003-R09: Free users get 1 streak freeze per month

**Acceptance Criteria:**
AC1: New user starts with streak = 0 · AC2: Opening app first time sets streak to 1 · AC3: Opening app next calendar day increments to 2 · AC4: Missing a day resets streak to 0 (free) or uses freeze (premium) · AC5: Badge animation shows on reaching milestone · AC6: Streak counter is visible on home screen and widget

### FEATURE F004: Smart Alarm System

**Description:** A full-featured alarm that wakes users with religious alarm tones and immediately presents the daily verse. Can optionally read the verse aloud via TTS.

**Basic Alarm:**
- F004-R01: User MUST be able to set at least 1 alarm (free)
- F004-R02: Premium users can set up to 5 alarms
- F004-R03: Alarm MUST work even when app is closed/killed
- F004-R04: Alarm MUST use Android AlarmManager with EXACT timing
- F004-R05: Alarm MUST survive device reboot (BroadcastReceiver)
- F004-R06: Alarm MUST respect Do Not Disturb settings

**Alarm Configuration:**
- F004-R07: Time picker (12/24 hour format based on device)
- F004-R08: Repeat days selector (individual days Mon–Sun)
- F004-R09: Alarm tone selector (10+ pre-loaded tones)
- F004-R10: Volume control (independent of device volume)
- F004-R11: Gradual volume increase option (30 seconds ramp)
- F004-R12: Snooze duration: 5 / 10 / 15 minutes
- F004-R13: Vibration toggle

**Alarm Tones (Pre-loaded, Offline):**

| ID | Tone Name | Religion | Duration |
|---|---|---|---|
| T01 | Temple Bell | Hinduism | 8 sec |
| T02 | Om Chanting | Hinduism | 10 sec |
| T03 | Flute (Krishna) | Hinduism | 10 sec |
| T04 | Church Bell | Christianity | 8 sec |
| T05 | Choir Hymn | Christianity | 10 sec |
| T06 | Azaan (Melodic) | Islam | 12 sec |
| T07 | Tibetan Bowl | Buddhism | 8 sec |
| T08 | Meditation Gong | Buddhism | 6 sec |
| T09 | Shabad Intro | Sikhism | 10 sec |
| T10 | Shofar | Judaism | 8 sec |
| T11 | Nature – Birds | Universal | 10 sec |
| T12 | Nature – Rain | Universal | 10 sec |
| T13 | Nature – Ocean | Universal | 10 sec |
| T14 | Peaceful Piano | Universal | 10 sec |
| T15 | Gentle Harp | Universal | 10 sec |

**Alarm Dismiss Behavior:**
- F004-R14: When alarm rings, full-screen alarm activity appears
- F004-R15: Alarm screen shows: time, "Good Morning", and verse preview
- F004-R16: Two buttons: "Snooze" and "Wake Up & Read"
- F004-R17: "Wake Up & Read" dismisses alarm and opens daily verse
- F004-R18: If TTS is enabled, verse is read aloud after dismissing alarm
- **F004-R19 (UPDATED — v1.1):** If no interaction occurs, the alarm tone plays for a **maximum of 30 minutes with escalating volume**. After 30 minutes, it **auto-snoozes for 5 minutes, up to 6 times**, then automatically stops to prevent battery drain. The app MUST log this as a **"Missed Alarm"** event in Firebase Analytics (`alarm_missed`, params: `alarm_id`, `snooze_cycles_used`).
  *~~Superseded: "Alarm auto-dismisses after 5 minutes if no interaction."~~ A flat 5-minute cutoff let a deep sleeper's alarm go silent — the single fastest path to a late wake-up, an angry user, and a 1-star review.*
- F004-R20: Swiping up/down as alternative dismiss gesture

**Technical Requirements:**
- F004-R21: Use `AlarmManager.setExactAndAllowWhileIdle()` for reliability, subject to the Android 12+ permission handling in F004-R25 below
- F004-R22: Register BootCompletedReceiver to reschedule after reboot
- F004-R23: Use Foreground Service for alarm playback
- F004-R24: Handle battery optimization exemption request
- **F004-R25 (NEW — v1.1):** On Android 12+ (API 31+), trigger a dedicated system permission screen for `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` during alarm setup. If the user denies it, the app MUST gracefully downgrade to `AlarmManager.setWindow()` (less precise, but crash-free) and show a persistent banner explaining how to enable exact alarms in system settings for full reliability. *Rationale: Android 12+ requires an explicit system-settings grant for exact alarms; without a graceful fallback, alarms silently degrade to inexact delivery (up to a 1-hour delay) with no explanation to the user.*
- F004-R25b: Support full-screen intent for lock screen alarm display *(renumbered from original F004-R25)*

**Acceptance Criteria:**
AC1: Alarm rings at exact set time (±1 minute tolerance, when exact-alarm permission is granted) · AC2: Alarm rings even when app is force-closed · AC3: Alarm rings after device reboot · AC4: Selected alarm tone plays correctly · AC5: Snooze reschedules alarm for selected duration · AC6: "Wake Up & Read" opens daily verse screen · AC7: TTS reads verse if enabled · AC8: Alarm works in Do Not Disturb if alarm exception is set · AC9: Alarm screen appears over lock screen · AC10: Volume gradually increases if option is enabled · **AC11 (NEW): Unattended alarm escalates for up to 30 minutes, then auto-snoozes up to 6 times before stopping, and logs `alarm_missed`** · **AC12 (NEW): When exact-alarm permission is denied on Android 12+, alarm still fires via `setWindow()` and the reliability banner is shown**

### FEATURE F005: Text-to-Speech (TTS) Audio System

**Description:** Uses Android's built-in TTS engine to read verses aloud in the user's selected language. Also supports pre-recorded audio prayers for premium users.

**TTS Engine:**
- F005-R01: Use Android's native TextToSpeech API
- F005-R02: Support languages: English, Hindi, Arabic, Spanish, Tamil, Telugu, Punjabi, and others based on device
- F005-R03: Adjustable speech rate: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x
- F005-R04: Adjustable pitch: Low, Normal, High
- F005-R05: TTS MUST work completely offline
- F005-R06: Show download prompt if TTS language data not installed

**Audio Playback:**
- F005-R07: Play button on every verse card
- F005-R08: Mini player bar at bottom during playback
- F005-R09: Mini player shows: verse title, play/pause, close
- F005-R10: Background playback support (continues when screen off)
- F005-R11: Audio focus management (pause when call comes in)
- F005-R12: Lock screen controls (play/pause/next via MediaSession)
- F005-R13: Notification with playback controls during background play

**Pre-recorded Audio (Premium):**
- F005-R14: 50–100 popular prayers per religion (bundled in app)
- F005-R15: Audio format: MP3, 64kbps, mono
- F005-R16: Average duration: 1–3 minutes per prayer
- F005-R17: Audio files stored in app's assets folder
- F005-R18: Premium unlock reveals full prayer audio library

**Playlist Feature (Premium):**
- F005-R19: "Morning Playlist" – auto-curated 5 verses + 1 prayer
- F005-R20: "Sleep Playlist" – calming verses, slow TTS speed
- F005-R21: Custom playlist – user picks and orders verses
- F005-R22: Auto-play next item in playlist
- F005-R23: Shuffle and repeat options

**Acceptance Criteria:**
AC1: Tapping play on any verse starts TTS reading · AC2: Audio continues when app goes to background · AC3: Lock screen shows playback controls · AC4: TTS works without internet · AC5: Speech rate adjustment takes effect immediately · AC6: Audio pauses when phone call received · AC7: Mini player appears during playback · AC8: Premium audio prayers play from local storage

### FEATURE F006: Content Library & Browse

**Requirements:**
- F006-R01: Library MUST be organized by categories
- F006-R02: Categories MUST be religion-specific
- F006-R03: Each category MUST show verse count
- F006-R04: Search functionality across all verses
- F006-R05: Search MUST support both original text and translation
- F006-R06: Filter by: Category, Read/Unread, Bookmarked
- F006-R07: Sort by: Day number, Alphabetical, Recently read
- F006-R08: Verse detail view same layout as daily verse
- F006-R09: "Random Verse" button for serendipity
- F006-R10: History tab showing past daily verses with dates

**Categories per Religion:**
- Hinduism: Bhagavad Gita (700), Upanishads (100), Ramayana Wisdom (100), Vedic Mantras (50), Hanuman Chalisa (40), Devotional Shlokas (100), Life Wisdom (100)
- Christianity: Old Testament (200), New Testament (200), Psalms (150), Proverbs (100), Words of Jesus (100), Prayers (50), Inspirational (100)
- Islam: Quran Ayahs (300), Hadith (200), Duas (100), 99 Names of Allah (99), Prophet's Wisdom (100), Daily Prayers (50)
- Buddhism: Dhammapada (423), Buddha's Teachings (150), Zen Koans (50), Meditation Guidance (50), Mindfulness Quotes (100)
- Sikhism: Guru Granth Sahib (300), Japji Sahib (38), Sukhmani Sahib (100), Guru's Teachings (100), Ardas Selections (30)
- Judaism: Torah Verses (200), Tehillim/Psalms (150), Mishlei/Proverbs (100), Wisdom Literature (100), Daily Prayers (50)
- Spiritual: Rumi's Poetry (100), Stoic Philosophy (100), Zen Wisdom (100), Sufi Quotes (100), Universal Wisdom (100), Mindfulness (100), Gratitude Quotes (100)

**Acceptance Criteria:**
AC1: Library shows only categories for selected religion · AC2: Tapping category shows list of verses · AC3: Search returns results within 200ms · AC4: Bookmarked verses appear in Favorites section · AC5: History shows correct dates for past daily verses · AC6: Random verse button shows different verse each tap

### FEATURE F007: Bookmarks & Favorites

**Requirements:**
- F007-R01: User can bookmark any verse with single tap
- F007-R02: Bookmark icon toggles (filled = bookmarked)
- F007-R03: Bookmarked verses accessible from Library > Favorites
- F007-R04: User can add personal notes to bookmarked verses
- F007-R05: Bookmarks stored in local Room database
- F007-R06: Bookmarks sortable by date added or source
- F007-R07: Swipe to remove bookmark with undo option
- F007-R08: Bookmark count shown on Favorites tab

**Acceptance Criteria:**
AC1: Tapping bookmark icon saves verse · AC2: Tapping again removes bookmark · AC3: Favorites screen shows all bookmarked verses · AC4: Notes can be added and edited · AC5: Bookmarks persist across app restarts

### FEATURE F008: Share Verse as Image

**Requirements:**
- F008-R01: "Share" button on every verse
- F008-R02: Generate image with verse text on beautiful background
- F008-R03: 10 pre-designed background templates per religion
- F008-R04: Image includes: verse text, source, app watermark
- F008-R05: Image resolution: 1080x1080 (Instagram square)
- F008-R06: Also generate 1080x1920 for Instagram stories
- F008-R07: Share via Android's native share sheet
- F008-R08: "Copy text" option as alternative to image share
- F008-R09: App watermark: small "DailyDivine" text at bottom
- F008-R10: Template auto-selected based on religion theme color

**Templates:** 5 photo backgrounds (nature, sky, temple, church, mosque); 5 gradient backgrounds (warm, cool, neutral, dark, light); each religion gets color-matched gradients; Premium unlocks 20 additional templates.

**Acceptance Criteria:**
AC1: Share button generates image within 2 seconds · AC2: Image displays verse text readably (proper font size) · AC3: Share sheet opens with image attached · AC4: WhatsApp, Instagram, Facebook share works correctly · AC5: Image saved to gallery option available

### FEATURE F009: Notifications & Reminders

**Daily Verse Notification:**
- F009-R01: Morning notification with verse preview
- F009-R02: Default time: same as alarm time (or 7:00 AM if no alarm)
- F009-R03: Notification shows first 100 characters of verse
- F009-R04: Tapping notification opens daily verse
- F009-R05: Notification includes "Read" action button

**Streak Reminder:**
- F009-R06: If user hasn't opened app by 8:00 PM, send reminder
- F009-R07: Reminder text: "Don't break your X-day streak!"
- F009-R08: No reminder if user already opened app today
- F009-R09: Reminder only sent if streak > 3 days

**Milestone Celebration:**
- F009-R10: Push notification on milestone achievement
- F009-R11: "Congratulations! You've reached X days!"
- F009-R12: Deep link to milestone badge screen

**Notification Channels (Android O+):** Daily Verse (Default), Alarm (High), Streak Reminders (Low), Milestones (Default)

**User Controls:**
- F009-R13: Each notification type independently toggleable
- F009-R14: Custom quiet hours setting
- F009-R15: Notification settings accessible from app Settings

**Acceptance Criteria:**
AC1: Morning notification arrives at configured time · AC2: Streak reminder only shows if app not opened today · AC3: Milestone notification shows on achievement · AC4: User can disable each notification type independently · AC5: Notifications respect Do Not Disturb settings

### FEATURE F010: Home Screen Widget

**Requirements:**
- F010-R01: Widget sizes: 4x2, 4x3, 4x4
- F010-R02: Widget displays: verse text (truncated) + source
- F010-R03: Widget updates daily at midnight
- F010-R04: Tapping widget opens app to daily verse
- F010-R05: Widget background matches app theme (light/dark)
- F010-R06: Widget includes streak counter
- F010-R07: Optional: "Play" button on widget to start TTS
- F010-R08: Widget works without opening app

**Technical:** F010-R09: Use AppWidgetProvider · F010-R10: Use WorkManager for daily widget update · F010-R11: Widget update broadcast at midnight

**Acceptance Criteria:**
AC1: Widget can be added from home screen long-press · AC2: Widget shows today's verse correctly · AC3: Widget updates at midnight to new verse · AC4: Tapping widget opens app · AC5: Widget displays correctly in light and dark mode

### FEATURE F011: Daily Journal / Reflection

**Requirements:**
- F011-R01: Below daily verse, show text input for reflection
- F011-R02: Placeholder: "What does this verse mean to you today?"
- F011-R03: Auto-save as user types (debounced, 500ms)
- F011-R04: Previous journal entries viewable in History
- F011-R05: Optional mood emoji selector
- F011-R06: Journal stored in local Room database
- F011-R07: Export all entries as text file (premium)
- F011-R08: Journal entry linked to verse and date

**Acceptance Criteria:**
AC1: User can type reflection and it auto-saves · AC2: Reopening app same day shows saved reflection · AC3: History shows past reflections with dates · AC4: Mood emoji can be selected and persists

### FEATURE F012: Settings

- **Profile:** F012-R01 Change religion · F012-R02 Change language · F012-R03 Change name/greeting
- **Alarm:** F012-R04 All alarm configurations (see F004)
- **Audio:** F012-R05 TTS voice selection · F012-R06 TTS speed and pitch · F012-R07 Auto-play verse on app open toggle · F012-R08 Background playback toggle
- **Appearance:** F012-R09 Theme: Light / Dark / System Default · F012-R10 Font size: Small / Medium / Large / Extra Large · F012-R11 Font family selection (3 options)
- **Notifications:** F012-R12 Toggle each notification type · F012-R13 Custom notification times
- **Data:** F012-R14 Export bookmarks · F012-R15 Export journal entries · F012-R16 Clear all data (with confirmation) · F012-R17 App storage usage display
- **Premium:** F012-R18 View premium features · F012-R19 Purchase premium · F012-R20 Restore purchase
- **About:** F012-R21 App version · F012-R22 Rate on Play Store · F012-R23 Share app link · F012-R24 Privacy policy · F012-R25 Terms of service · F012-R26 Contact email · F012-R27 Open source licenses

---

# 7. User Stories & Acceptance Criteria

**Epic 1: Onboarding**
- US-101: As a new user, I want to select my religion so that I receive relevant daily verses.
- US-102: As a new user, I want to set my preferred language so that verses are shown in my language.
- US-103: As a new user, I want to set a morning alarm during onboarding so that I start my daily routine immediately.
- US-104: As a returning user, I want to skip onboarding and go directly to my daily verse.

**Epic 2: Daily Verse**
- US-201: As a user, I want to see a new verse every day so that I have fresh spiritual content.
- US-202: As a user, I want to listen to the verse being read aloud so that I can absorb it without reading.
- US-203: As a user, I want to copy the verse text so that I can paste it elsewhere.
- US-204: As a user, I want to share the verse as a beautiful image so that I can share on social media.
- US-205: As a user, I want to bookmark a verse so that I can find it later easily.

**Epic 3: Streaks**
- US-301: As a user, I want to see my current streak so that I feel motivated to continue.
- US-302: As a user, I want to earn badges at milestones so that I feel rewarded for consistency.
- US-303: As a user, I want streak freeze protection so that one missed day doesn't erase my progress.

**Epic 4: Alarm**
- US-401: As a user, I want my alarm to play a religious tone so that I wake up peacefully.
- US-402: As a user, I want the alarm to read today's verse so that I hear it first thing in the morning.
- US-403: As a user, I want the alarm to work even when the app is closed so that it's reliable.
- US-404: As a user, I want to snooze the alarm so that I can get a few more minutes of sleep.
- US-405: As a user, I want the alarm to show on my lock screen so that I can dismiss it easily.
- **US-406 (NEW — v1.1): As a heavy sleeper, I want the alarm to keep escalating and auto-snoozing if I don't respond, so that I don't sleep through it and wake up late.**
- **US-407 (NEW — v1.1): As an Android 12+ user, I want to be guided to grant exact-alarm permission (or told my alarm may run a few minutes late if I don't), so that I'm never surprised by a silently degraded alarm.**

**Epic 5: Library**
- US-501: As a user, I want to browse all verses by category so that I can explore my scripture.
- US-502: As a user, I want to search for specific verses so that I can find relevant content.
- US-503: As a user, I want to see my reading history so that I can revisit past daily verses.
- US-504: As a user, I want a random verse button so that I can discover new content.

**Epic 6: Audio**
- US-601: As a user, I want to listen to prayers/chants so that I can do my daily devotion.
- US-602: As a user, I want audio to continue in the background so that I can listen while doing other things.
- US-603: As a user, I want playback controls on lock screen so that I can pause without unlocking.

**Epic 7: Settings & Personalization**
- US-701: As a user, I want to change my religion so that I can explore different faiths.
- US-702: As a user, I want dark mode so that I can use the app at night comfortably.
- US-703: As a user, I want large font option so that I can read verses easily (accessibility).

**Epic 8: Monetization**
- US-801: As a free user, I accept seeing ads in exchange for free content.
- US-802: As a user, I want to pay once to remove all ads permanently.
- US-803: As a premium user, I want access to all audio prayers and extra features.
- **US-804 (NEW — v1.1): As an EU/UK user, I want to be asked for ad-personalization consent before seeing ads, so the app complies with GDPR and I understand my choices.**

---

# 8. Information Architecture

**App Launch → First Launch → Onboarding Flow:** Welcome Screen → Select Religion → Select Language → Set Alarm → Notification Permission

**App Launch → Returning User → Main App**, with a bottom navigation bar of **Home | Library | Alarm | Settings**:

- **Home Screen:** Greeting Banner → Daily Verse Card (Verse Text, Source Reference, Action Buttons: Play/Copy/Save/Share) → Daily Reflection Input → Streak Counter & Progress → Alarm Status Banner
- **Library Screen:** Search Bar → Categories Grid (Category → Verse List → Verse Detail) → Tabs: All | Favorites | History → Random Verse FAB
- **Alarm Screen:** Alarm List → Add New Alarm → Alarm Configuration (Time Picker, Repeat Days, Tone Selector, TTS Toggle, Volume & Snooze) — **now also surfaces the Android 12+ exact-alarm permission prompt/banner per F004-R25**
- **Settings Screen:** Profile (Religion, Language, Name) → Notifications → Audio (TTS config) → Appearance (Theme, Font) → Data (Export, Clear) → Premium → About
- **Overlay Screens:** Alarm Ring Screen (full screen, over lock screen) · Milestone Badge Screen (celebration) · Share Image Preview · Audio Player (mini + expanded)

---

# 9. Screen-by-Screen Specifications

**S01 – Splash Screen:** 1.5s duration; app logo + name + tagline; fade-in/slide-up animation; religion-neutral gradient background; routes to Onboarding (first launch) or Home (returning).

**S02 – Welcome Screen (Onboarding 1/5):** Hero illustration (peaceful sunrise); title "Welcome to DailyDivine"; subtitle "Start your day with divine wisdom"; button "Begin Your Journey →"; no skip option.

**S03 – Religion Selection (Onboarding 2/5):** Title "Choose Your Path"; 2-column grid of 7 religion cards (icon + name + brief description each); single-select with highlight animation; "Continue →" enabled after selection.

**S04 – Language Selection (Onboarding 3/5):** Title "Select Your Language"; list filtered by religion; flag emoji + language name; English pre-selected; "Continue →".

**S05 – Alarm Setup (Onboarding 4/5):** Title "Set Your Morning Blessing"; large scrollable time picker (default 5:30 AM); tone preview with play button; horizontal tone selector pre-filtered by religion; toggle "Read verse aloud when alarm rings"; **[UPDATED v1.1] on Android 12+, this screen also triggers the exact-alarm permission request (F004-R25) before the alarm is scheduled**; button "Set Alarm →"; link "Skip, I'll set up later".

**S06 – Permission Request (Onboarding 5/5):** Title "Stay Connected to Your Faith"; notification bell illustration; explains why notifications help; button "Allow Notifications"; link "Maybe Later"; then navigates to Home.

**S07 – Home Screen:** Greeting header ("Good Morning, [Name]! · Day 47 of your journey") → Today's Verse card with text, source, and Play/Copy/Save/Share buttons → Today's Reflection input → Streak card (current streak, progress bar to next milestone) → Next Alarm banner → Ad banner (free tier only, gated by consent per SEC-11) → bottom nav (Home/Library/Alarm/Settings).

**S08 – Alarm Ring Screen (full-screen activity):** Appears over lock screen; animated religion-themed gradient background; large current time; "Time for your morning blessing"; verse preview (first line); buttons "Snooze – X min" (muted) and "Wake Up & Read" (primary); gentle pulse animation on buttons; selected alarm tone plays. **[UPDATED v1.1]** if undismissed, tone escalates in volume for up to 30 minutes, then auto-snoozes (5 min, up to 6 cycles) before stopping and logging `alarm_missed` (F004-R19). If TTS enabled, verse is read aloud after dismiss.

**S09 – Library Screen:** Search bar → tabs (All | Favorites | History) → 2-column categories grid (name + verse count per card) → Random Verse floating action button → bottom nav.

**S10 – Verse Detail Screen:** Back arrow; verse text (large, centered); original-language text if different from translation; source reference; action buttons (Play, Copy, Bookmark, Share); related verses (3 suggestions); journal entry for this verse if opened from history.

**S11 – Alarm Configuration Screen:** Back arrow + "Edit Alarm" title; time picker; 7 repeat-day buttons; tone selector with preview; volume slider; toggles (Vibration, Gradual volume increase, Read verse aloud, Play audio prayer after); snooze duration selector; Save/Delete buttons. **[UPDATED v1.1]** shows the exact-alarm permission banner (F004-R25) when the system permission has been denied.

**S12 – Settings Screen:** Standard preference-style list, grouped sections with headers; each item shows icon + title + current value/toggle; changing religion triggers a content refresh (now routed through the Content Migration Manager per F002-R13 so existing user data is preserved).

---

# 10. Database Design

**Database Engine:** Room (SQLite wrapper for Android)

### Entity Definitions

```kotlin
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user, always id=1
    val name: String,
    val religionId: Int,
    val languageCode: String,
    val themeMode: String,       // "light", "dark", "system"
    val fontSize: String,        // "small", "medium", "large", "xlarge"
    val isPremium: Boolean,
    val premiumPurchaseDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "religions")
data class Religion(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: String,
    val colorPrimary: String,
    val colorSecondary: String,
    val totalVerses: Int,
    val availableLanguages: String, // JSON array: ["en","hi","sa"]
    val description: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: Int,
    val religionId: Int,
    val name: String,
    val icon: String,
    val description: String,
    val verseCount: Int,
    val sortOrder: Int,
    val isPremium: Boolean
)

@Entity(
    tableName = "verses",
    indices = [
        Index(value = ["religionId", "dayNumber"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["religionId"])
    ]
)
data class Verse(
    @PrimaryKey val id: Int,
    val religionId: Int,
    val categoryId: Int,
    val dayNumber: Int,          // 1–730 for daily assignment
    val originalText: String,
    val translatedText: String,
    val sourceReference: String,
    val languageCode: String,
    val audioFilePath: String?,  // Nullable, for premium audio
    val isPremium: Boolean,
    val tags: String?            // Comma-separated tags for search
)

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val repeatDays: String,      // JSON: [1,2,3,4,5] (Mon-Fri)
    val alarmToneId: String,
    val volume: Int,
    val isGradualVolume: Boolean,
    val isVibrationEnabled: Boolean,
    val isTTSEnabled: Boolean,
    val playAudioAfter: Boolean,
    val snoozeDurationMinutes: Int,
    val label: String,
    val createdAt: Long
    // v1.1: escalation/auto-snooze cap (F004-R19) and exact-alarm fallback
    // state (F004-R25) are runtime AlarmService concerns, not persisted fields
)

@Entity(tableName = "streaks", indices = [Index(value = ["date"], unique = true)])
data class StreakEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,            // "2024-01-15" (ISO format)
    val verseId: Int,
    val wasRead: Boolean,
    val wasListened: Boolean,
    val journalText: String?,
    val moodEmoji: String?,
    val openedAt: Long
)

@Entity(tableName = "bookmarks", indices = [Index(value = ["verseId"], unique = true)])
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int,
    val notes: String?,
    val createdAt: Long
)

@Entity(tableName = "alarm_tones")
data class AlarmTone(
    @PrimaryKey val id: String,  // "temple_bell"
    val name: String,
    val fileName: String,
    val religionId: Int?,        // Null = universal
    val durationSeconds: Int,
    val isPremium: Boolean
)

// v1.1 (new): tracks the content JSON version already applied per religion,
// so the Content Migration Manager (F002-R13) can diff safely on update.
@Entity(tableName = "content_versions")
data class ContentVersion(
    @PrimaryKey val religionId: Int,
    val appliedVersion: Int,
    val lastMigratedAt: Long
)
```

### Data Access Objects (DAOs)

```kotlin
@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE religionId = :religionId AND dayNumber = :dayNumber LIMIT 1")
    suspend fun getDailyVerse(religionId: Int, dayNumber: Int): Verse?

    @Query("SELECT * FROM verses WHERE categoryId = :categoryId ORDER BY dayNumber")
    fun getVersesByCategory(categoryId: Int): Flow<List<Verse>>

    @Query("""SELECT * FROM verses WHERE religionId = :religionId
        AND (originalText LIKE '%' || :query || '%'
        OR translatedText LIKE '%' || :query || '%'
        OR sourceReference LIKE '%' || :query || '%')""")
    fun searchVerses(religionId: Int, query: String): Flow<List<Verse>>

    @Query("SELECT * FROM verses WHERE religionId = :religionId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(religionId: Int): Verse?

    @Query("SELECT COUNT(*) FROM verses WHERE religionId = :religionId")
    suspend fun getVerseCount(religionId: Int): Int

    // v1.1: used by the Content Migration Manager for diff-based inserts
    @Query("SELECT id FROM verses WHERE religionId = :religionId")
    suspend fun getExistingVerseIds(religionId: Int): List<Int>
}

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakEntry(entry: StreakEntry)

    @Query("SELECT * FROM streaks WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): StreakEntry?

    @Query("SELECT * FROM streaks ORDER BY date DESC")
    fun getAllEntries(): Flow<List<StreakEntry>>

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT :days")
    fun getRecentEntries(days: Int): Flow<List<StreakEntry>>

    @Query("SELECT COUNT(*) FROM streaks WHERE date >= :startDate")
    suspend fun getStreakCount(startDate: String): Int
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE verseId = :verseId")
    suspend fun removeBookmark(verseId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE verseId = :verseId)")
    fun isBookmarked(verseId: Int): Flow<Boolean>

    @Query("""SELECT v.* FROM verses v INNER JOIN bookmarks b
        ON v.id = b.verseId ORDER BY b.createdAt DESC""")
    fun getBookmarkedVerses(): Flow<List<Verse>>
}

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?
}
```

### Database Instance

```kotlin
@Database(
    entities = [
        UserProfile::class, Religion::class, Category::class, Verse::class,
        Alarm::class, StreakEntry::class, Bookmark::class, AlarmTone::class,
        ContentVersion::class // v1.1
    ],
    version = 2, // bumped for v1.1 (adds content_versions table)
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun verseDao(): VerseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun alarmDao(): AlarmDao
    abstract fun streakDao(): StreakDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun alarmToneDao(): AlarmToneDao
}
```

---

# 11. Content Strategy

## 11.1 Content Sources (Public Domain / Open License)
All religious texts used are in PUBLIC DOMAIN: Bhagavad Gita (ancient text); Bible (King James Version, 1611); Quran (Yusuf Ali translation, expired copyright); Dhammapada (ancient text); Guru Granth Sahib (freely shared sacred text); Torah (ancient text); Rumi/Stoics (ancient/expired copyright).

## 11.2 Content Format (JSON)

`assets/content/hinduism_en.json`:
```json
{
  "religion": "Hinduism",
  "language": "en",
  "version": 1,
  "categories": [
    { "id": 101, "name": "Bhagavad Gita", "icon": "book_gita", "verseCount": 700 }
  ],
  "verses": [
    {
      "id": 1001,
      "categoryId": 101,
      "dayNumber": 1,
      "originalText": "...",
      "translatedText": "On the sacred field of Kurukshetra...",
      "sourceReference": "Bhagavad Gita 1.1",
      "tags": "dharma,duty,battlefield,beginning"
    }
  ]
}
```

> **v1.1:** the top-level `"version"` field is now load-bearing — it drives the Content Migration Manager (F002-R13). Bumping it on any content-only release tells the app to diff and insert new verses without touching existing user data.

## 11.3 Content Files Required

| File Name | Religion | Language | Verses |
|---|---|---|---|
| hinduism_en.json | Hinduism | English | 730 |
| hinduism_hi.json | Hinduism | Hindi | 730 |
| christianity_en.json | Christianity | English | 730 |
| christianity_es.json | Christianity | Spanish | 730 |
| islam_en.json | Islam | English | 730 |
| islam_ar.json | Islam | Arabic | 730 |
| buddhism_en.json | Buddhism | English | 730 |
| sikhism_en.json | Sikhism | English | 730 |
| sikhism_pa.json | Sikhism | Punjabi | 730 |
| judaism_en.json | Judaism | English | 730 |
| spiritual_en.json | Spiritual | English | 730 |

Total: ~11 JSON files for MVP (add more languages later).

## 11.4 Content Loading Strategy

**First install:**
1. Read JSON from `assets/content/` folder
2. Parse and insert all verses into Room database
3. Show progress bar during loading
4. Mark content as loaded in SharedPreferences
5. Subsequent launches read from Room DB (fast)

**App update (v1.1 addition):**
1. Read `version` field from the bundled JSON per religion
2. Compare against `content_versions.appliedVersion` for that religion
3. If higher, diff verse IDs against `VerseDao.getExistingVerseIds()` and insert only new/changed rows
4. Never truncate or drop the `verses`, `streaks`, `bookmarks`, or `user_profile` tables during this process
5. Update `content_versions.appliedVersion` and `lastMigratedAt` on success

Estimated loading time: 2–5 seconds for 730 verses (first install); sub-second for a typical incremental content diff.

---

# 12. Audio System Architecture

## 12.1 TTS (Text-to-Speech) Architecture

```
User taps Play → TTSService initialized
 → Set language (user's selected language)
 → Set speech rate (user preference)
 → Set pitch (user preference)
 → Speak verse text
 → Show mini player UI
 → Handle audio focus
 → Update notification with controls
 → On completion: hide mini player
```

```kotlin
class TTSManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onReady()
            }
        }
    }

    fun speak(text: String, language: Locale, rate: Float = 1.0f, pitch: Float = 1.0f) {
        tts?.let {
            it.language = language
            it.setSpeechRate(rate)
            it.setPitch(pitch)
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "verse_id")
        }
    }

    fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown() }
    fun isSpeaking(): Boolean = tts?.isSpeaking == true
}
```

## 12.2 Audio Playback (Pre-recorded prayers)

Using ExoPlayer (Media3):
```
User selects prayer → ExoPlayer loads local asset
 → Create MediaSession
 → Show notification with controls
 → Handle audio focus
 → Support background playback
 → Lock screen controls via MediaSession
```

## 12.3 Audio File Organization

```
app/src/main/assets/audio/
├── alarm_tones/        (15 files: temple_bell.mp3, om_chanting.mp3, church_bell.mp3, ...)
└── prayers/ (Premium)
    ├── hinduism/   (~50 files)
    ├── christianity/ (~50 files)
    ├── islam/      (~50 files)
    ├── buddhism/   (~30 files)
    ├── sikhism/    (~30 files)
    └── judaism/    (~30 files)
```

## 12.4 Audio Size Estimation

| Type | Files | Avg Size | Total |
|---|---|---|---|
| Alarm tones | 15 | 200KB | 3 MB |
| Hindu prayers | 50 | 1.5MB | 75 MB |
| Christian | 50 | 1.5MB | 75 MB |
| Islamic | 50 | 1.5MB | 75 MB |
| Buddhist | 30 | 1.5MB | 45 MB |
| Sikh | 30 | 1.5MB | 45 MB |
| Jewish | 30 | 1.5MB | 45 MB |

**Strategy:** Bundle only alarm tones + selected religion's prayers. Use Android App Bundle (AAB) with asset delivery for prayers. Total APK size (MVP): ~15–20 MB.

---

# 13. Alarm System Architecture

## 13.1 Alarm Flow

```
User sets alarm
 → Save to Room DB
 → [v1.1] Check SCHEDULE_EXACT_ALARM permission (Android 12+)
     → Granted: Schedule with AlarmManager.setExactAndAllowWhileIdle()
     → Denied:  Schedule with AlarmManager.setWindow() + show reliability banner (F004-R25)
 → Register PendingIntent with unique requestCode (alarm ID)

At alarm time:
 → AlarmReceiver (BroadcastReceiver) triggered
 → Start AlarmService (Foreground Service)
 → Play alarm tone via MediaPlayer
 → Show full-screen AlarmActivity (over lock screen)
 → Acquire WakeLock to prevent sleep

User interaction:
 → "Snooze" → Reschedule alarm for +X minutes
 → "Wake Up" → Stop alarm, open DailyVerseActivity
 → If TTS enabled → Speak verse aloud

 [v1.1] If no interaction:
 → Escalate volume for up to 30 minutes
 → Auto-snooze 5 minutes, repeat up to 6 cycles
 → After 6 cycles, auto-dismiss and log alarm_missed to Firebase Analytics

After alarm:
 → Release WakeLock
 → Stop Foreground Service
 → Schedule next occurrence (if repeating)
```

## 13.2 Key Components

```kotlin
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_TONE", alarm.alarmToneId)
            putExtra("TTS_ENABLED", alarm.isTTSEnabled)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        // v1.1: graceful fallback when exact-alarm permission is not granted
        if (canScheduleExactAlarms(context)) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent
            )
        } else {
            val windowMs = 10 * 60 * 1000L // 10-minute delivery window
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP, triggerTime, windowMs, pendingIntent
            )
        }
    }

    fun cancel(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    // v1.1
    private fun canScheduleExactAlarms(context: Context): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.putExtras(intent)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val alarms = database.alarmDao().getEnabledAlarms()
                alarms.forEach { alarm -> AlarmScheduler(context).schedule(alarm) }
            }
        }
    }
}

// v1.1 (new): escalation + auto-snooze loop, owned by AlarmService
class AlarmEscalationController(
    private val maxEscalationMinutes: Int = 30,
    private val autoSnoozeMinutes: Int = 5,
    private val maxAutoSnoozeCycles: Int = 6
) {
    fun onNoInteractionTimeout(alarmId: Int, cyclesUsed: Int, analytics: AnalyticsLogger) {
        if (cyclesUsed >= maxAutoSnoozeCycles) {
            analytics.log("alarm_missed", mapOf("alarm_id" to alarmId, "snooze_cycles_used" to cyclesUsed))
            // stop tone, release wake lock, stop foreground service
        } else {
            // reschedule for +autoSnoozeMinutes, increment cyclesUsed
        }
    }
}
```

## 13.3 Required Permissions

```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.VIBRATE" />
```

## 13.4 Manifest Declarations

```xml
<receiver android:name=".alarm.AlarmReceiver" android:exported="false" />
<receiver android:name=".alarm.BootReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
<service android:name=".alarm.AlarmService"
    android:foregroundServiceType="mediaPlayback" android:exported="false" />
<activity android:name=".alarm.AlarmActivity"
    android:showOnLockScreen="true" android:turnScreenOn="true"
    android:excludeFromRecents="true" android:taskAffinity=""
    android:launchMode="singleInstance" />
```

---

# 14. Notification Strategy

## 14.1 Notification Channels

```kotlin
object NotificationChannels {
    const val ALARM = "alarm_channel"
    const val DAILY_VERSE = "daily_verse_channel"
    const val STREAK_REMINDER = "streak_reminder_channel"
    const val MILESTONE = "milestone_channel"
    const val AUDIO_PLAYBACK = "audio_playback_channel"
}
```

## 14.2 Notification Schedule

| Notification | Time | Condition |
|---|---|---|
| Daily Verse | Alarm time OR 7AM | Every day |
| Streak Reminder | 8:00 PM | Only if not opened today |
| Milestone | Immediately | On badge achievement |
| Audio Playback | During playback | Ongoing notification |

---

# 15. Monetization Strategy

## 15.1 Ad Placement

| Ad Type | Location | Frequency |
|---|---|---|
| Banner Ad | Bottom of Home screen | Always visible (subject to consent, see SEC-11) |
| Interstitial | After viewing 5th verse in Library | Every 5th verse in library browsing |
| Interstitial | On app open | Once per 3 hours |

**Ad Rules:** No ads on alarm dismiss screen (frustrating); no ads on verse detail while playing audio (disruptive); no ads during onboarding (poor first impression); banner ad height 50dp; interstitial frequency cap max 5/day.

> **[UPDATED v1.1]** In the EU/UK, ad requests are gated behind Google UMP consent resolution — see SEC-11 in Section 23. If consent is denied, the banner slot shows a "Support the app with Premium" card instead of an ad.

## 15.2 Premium Features ($2.99 one-time purchase)

| Feature | Free | Premium |
|---|---|---|
| Daily verse | ✓ | ✓ |
| TTS (Text-to-Speech) | ✓ | ✓ |
| 1 Alarm | ✓ | ✓ |
| Basic streak | ✓ | ✓ |
| Bookmarks (up to 10) | ✓ | ✓ |
| Share verse | ✓ | ✓ |
| Ads | Yes | None |
| Multiple alarms (up to 5) | — | ✓ |
| Full audio prayer library | — | ✓ |
| Custom playlists | — | ✓ |
| Unlimited bookmarks | — | ✓ |
| Streak freeze (unlimited) | — | ✓ |
| Extra share templates | — | ✓ |
| Export journal to PDF | — | ✓ |
| Multiple religion access | — | ✓ |
| Extra themes & fonts | — | ✓ |
| Home screen widget | — | ✓ |

## 15.3 In-App Purchase Implementation
Using Google Play Billing Library. Product ID: `daily_divine_premium`. Type: one-time purchase (non-consumable). Price: $2.99 USD (or regional equivalent).

## 15.4 Revenue Projections

| Month | Downloads | DAU | Ad Revenue | IAP Revenue | Total |
|---|---|---|---|---|---|
| 1–3 | 5,000 | 500 | $15/mo | $30/mo | $45/mo |
| 3–6 | 15,000 | 2,000 | $60/mo | $90/mo | $150/mo |
| 6–9 | 40,000 | 5,000 | $150/mo | $200/mo | $350/mo |
| 9–12 | 80,000 | 10,000 | $300/mo | $350/mo | $650/mo |
| 12–18 | 150,000 | 20,000 | $600/mo | $500/mo | $1,100/mo |
| 18–24 | 250,000 | 35,000 | $1,050/mo | $700/mo | $1,750/mo |

---

# 16. Tech Stack & Dependencies

## 16.1 Core Technology

| Component | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | API 26 (Android 8.0 Oreo) |
| Target SDK | API 34 (Android 14) |
| Build System | Gradle (Kotlin DSL) |
| IDE | GitHub Codespaces + Android Studio |
| Architecture | MVVM + Clean Architecture |
| DI Framework | Hilt (Dagger) |
| Async | Kotlin Coroutines + Flow |
| UI | Jetpack Compose + Material3 |
| Navigation | Jetpack Navigation Compose |
| Database | Room (SQLite) |
| Preferences | DataStore |
| Audio | Media3 (ExoPlayer) + Android TTS |
| Ads | Google AdMob (+ Google UMP SDK, v1.1) |
| Billing | Google Play Billing Library v6 |
| Analytics | Firebase Analytics (free) |
| Crash Reporting | Firebase Crashlytics (free) |
| Remote Config | Firebase Remote Config (free) |
| Version Control | Git + GitHub |

## 16.2 Dependencies (build.gradle.kts)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.dailydivine.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.dailydivine.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2      // v1.1
        versionName = "1.1.0"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    implementation("com.google.android.gms:play-services-ads:22.6.0")
    // v1.1: Google User Messaging Platform for GDPR/EU consent
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    implementation("com.android.billingclient:billing-ktx:6.1.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

# 17. GitHub Codespaces Setup

## 17.1 Codespace Configuration

`.devcontainer/devcontainer.json`:
```json
{
  "name": "DailyDivine Android Dev",
  "image": "mcr.microsoft.com/devcontainers/java:17",
  "features": {
    "ghcr.io/devcontainers/features/java:1": {
      "version": "17", "installGradle": "true", "gradleVersion": "8.4"
    },
    "ghcr.io/devcontainers/features/android-sdk:1": {
      "version": "34", "buildTools": "34.0.0", "platformTools": true, "cmdlineTools": true
    }
  },
  "customizations": {
    "vscode": {
      "extensions": [
        "mathiasfrohlich.Kotlin", "fwcd.kotlin", "vscjava.vscode-gradle",
        "naco-siren.gradle-language", "esafirm.kotlin-formatter"
      ],
      "settings": { "editor.fontSize": 14, "editor.tabSize": 4, "kotlin.languageServer.enabled": true }
    }
  },
  "postCreateCommand": "bash .devcontainer/setup.sh",
  "forwardPorts": [5554, 5555],
  "remoteUser": "vscode"
}
```

`.devcontainer/setup.sh`:
```bash
#!/bin/bash
echo "Setting up Android development environment..."
yes | sdkmanager --licenses
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" \
    "extras;google;m2repository" "extras;android;m2repository"
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools' >> ~/.bashrc
chmod +x gradlew
echo "Setup complete! Run './gradlew assembleDebug' to build."
```

## 17.2 Building in Codespaces
```bash
git clone https://github.com/yourusername/DailyDivine.git
cd DailyDivine
./gradlew assembleDebug
./gradlew test
./gradlew assembleRelease
./gradlew bundleRelease
```

## 17.3 Testing without Emulator
Since Codespaces doesn't support the Android Emulator: build the APK in Codespaces, download it from the Codespaces file explorer, install on a physical device via USB or wireless ADB, or use Android Studio locally for UI testing. All unit tests run in Codespaces.

## 17.4 CI/CD with GitHub Actions

`.github/workflows/build.yml`:
```yaml
name: Android CI
on:
  push: { branches: [ main, develop ] }
  pull_request: { branches: [ main ] }
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with: { name: debug-apk, path: app/build/outputs/apk/debug/app-debug.apk }
```

---

# 18. Project Structure

```
DailyDivine/
├── .devcontainer/
├── .github/workflows/
├── app/src/main/java/com/dailydivine/app/
│   ├── DailyDivineApp.kt
│   ├── di/                 (AppModule, DatabaseModule, AudioModule, AdModule)
│   ├── data/
│   │   ├── local/db/       (AppDatabase, Converters, dao/*)
│   │   ├── local/datastore/(UserPreferences)
│   │   ├── local/entity/   (UserProfile, Religion, Category, Verse, Alarm, StreakEntry, Bookmark, AlarmTone, ContentVersion*)
│   │   ├── repository/     (VerseRepository, AlarmRepository, StreakRepository, BookmarkRepository, UserRepository)
│   │   └── content/        (ContentLoader.kt, ContentMigrationManager.kt*)
│   ├── domain/
│   │   ├── model/          (DailyVerse, ReligionInfo, StreakInfo, AlarmInfo, MilestoneBadge)
│   │   └── usecase/        (GetDailyVerseUseCase, GetStreakUseCase, ToggleBookmarkUseCase, ScheduleAlarmUseCase, SearchVersesUseCase, CheckMilestoneUseCase)
│   ├── ui/
│   │   ├── theme/, navigation/, components/
│   │   ├── onboarding/     (WelcomeScreen, ReligionSelectScreen, LanguageSelectScreen, AlarmSetupScreen, PermissionScreen, OnboardingViewModel)
│   │   ├── home/, library/, alarm/, settings/
│   │   └── MainActivity.kt
│   ├── service/            (AlarmService, AudioPlaybackService, DailyVerseWorker)
│   ├── receiver/           (AlarmReceiver, BootReceiver, NotificationReceiver)
│   ├── audio/               (TTSManager, AudioPlayerManager, AlarmTonePlayer)
│   ├── notification/        (NotificationHelper, NotificationChannels)
│   ├── widget/               (DailyVerseWidget, WidgetUpdater)
│   ├── billing/              (BillingManager, PremiumManager)
│   ├── consent/               (ConsentManager.kt — v1.1: wraps Google UMP)
│   └── util/                  (DateUtils, ShareUtils, ImageGenerator, Constants, Extensions)
│   ├── res/ (drawable/, raw/ audio, values/, values-hi/, values-es/, values-ar/, xml/, font/)
│   └── assets/content/ (11 JSON files, one per religion/language)
├── test/  (unit tests, incl. ContentMigrationManagerTest* — v1.1)
├── androidTest/ (instrumented tests)
├── build.gradle.kts, settings.gradle.kts, gradle.properties, gradlew(.bat)
├── README.md, LICENSE, PRIVACY_POLICY.md, .gitignore
```
`*` marks files newly introduced in v1.1.

---

# 19. API Contracts (Internal)

No external APIs needed. All data is local.

**Contract 1: Get Daily Verse**
```
Input:  religionId: Int, date: LocalDate
Output: DailyVerse(verse: Verse, dayNumber: Int, isBookmarked: Boolean, hasJournalEntry: Boolean)
Logic:  dayNumber = ChronoUnit.DAYS.between(installDate, date) % totalVerses + 1
        verse = verseDao.getDailyVerse(religionId, dayNumber)
```

**Contract 2: Calculate Streak**
```
Input:  userId: Int
Output: StreakInfo(currentStreak, longestStreak, totalDaysActive, currentMilestone,
                    nextMilestone, progressToNext, hasFreezeAvailable)
Logic:  Count consecutive dates backwards from today in streaks table
```

**Contract 3: Schedule Alarm**
```
Input:  Alarm entity
Output: Result<AlarmId>
Logic:  Save to Room → Check exact-alarm permission (v1.1) →
        Calculate next trigger → Schedule with AlarmManager (exact or window fallback)
```

**Contract 4 (NEW — v1.1): Migrate Content**
```
Input:  religionId: Int, bundledJsonVersion: Int
Output: MigrationResult(newVersesInserted: Int, appliedVersion: Int)
Logic:  storedVersion = contentVersionDao.get(religionId)?.appliedVersion ?: 0
        if bundledJsonVersion > storedVersion:
            existingIds = verseDao.getExistingVerseIds(religionId)
            newVerses = parsedJson.verses.filter { it.id !in existingIds }
            verseDao.insertAll(newVerses)
            contentVersionDao.upsert(religionId, bundledJsonVersion)
        // streaks, bookmarks, user_profile are never touched by this contract
```

---

# 20. UI/UX Design Guidelines

## 20.1 Design System

**Color Palette (per religion):**

| Religion | Primary | Secondary | Surface |
|---|---|---|---|
| Hinduism | #FF5722 (Deep Orange) | #FFC107 (Amber) | #FFF3E0 |
| Christianity | #1565C0 (Royal Blue) | #FFD54F (Gold) | #E3F2FD |
| Islam | #2E7D32 (Green) | #FFD54F (Gold) | #E8F5E9 |
| Buddhism | #6A1B9A (Purple) | #FFB74D (Orange) | #F3E5F5 |
| Sikhism | #E65100 (Deep Orange) | #1565C0 (Blue) | #FFF3E0 |
| Judaism | #1565C0 (Blue) | #FFFFFF (White) | #E3F2FD |
| Spiritual | #00695C (Teal) | #B39DDB (Lavender) | #E0F2F1 |

**Typography:** Heading Large — Poppins Bold 28sp · Heading Medium — Poppins SemiBold 22sp · Heading Small — Poppins Medium 18sp · Body Large — Poppins Regular 16sp · Body Medium — Poppins Regular 14sp · Body Small — Poppins Regular 12sp · Verse Text — Noto Serif 20sp.

**Spacing:** XS 4dp · S 8dp · M 16dp · L 24dp · XL 32dp · XXL 48dp.

**Corner Radius:** Small 8dp (buttons) · Medium 12dp (cards) · Large 16dp (bottom sheets) · Full 50% (circular elements).

## 20.2 Animation Guidelines

| Element | Animation | Duration |
|---|---|---|
| Screen transitions | Slide horizontal | 300ms |
| Card appearance | Fade + slide up | 400ms |
| Streak counter | Count up | 800ms |
| Badge unlock | Scale + bounce | 600ms |
| Bookmark toggle | Scale pulse | 200ms |
| Play button | Ripple | 200ms |
| Milestone | Lottie confetti | 2000ms |

## 20.3 Accessibility
Minimum touch target 48dp x 48dp · Color contrast ratio 4.5:1 minimum · All images have `contentDescription` · TalkBack support for all interactive elements · Font scaling support (sp units) · Support system font size settings.

---

# 21. Accessibility Requirements
- A11Y-01: All text MUST use sp (scalable pixels)
- A11Y-02: All interactive elements MUST be minimum 48dp
- A11Y-03: All images MUST have contentDescription
- A11Y-04: Color MUST NOT be the only way to convey information
- A11Y-05: App MUST support TalkBack screen reader
- A11Y-06: App MUST support Switch Access
- A11Y-07: App MUST support system font size up to 200%
- A11Y-08: App MUST support dark mode for low-vision users
- A11Y-09: All animations MUST respect "Reduce Motion" setting
- A11Y-10: Form fields MUST have visible labels
- A11Y-11: Error messages MUST be announced by TalkBack
- A11Y-12: Focus order MUST be logical (top-to-bottom, left-to-right)

---

# 22. Performance Requirements
- PERF-01: Cold start time < 2 seconds
- PERF-02: Daily verse display < 500ms after launch
- PERF-03: Search results < 200ms
- PERF-04: Library category load < 300ms
- PERF-05: Share image generation < 2 seconds
- PERF-06: TTS start playback < 1 second
- PERF-07: Alarm trigger accuracy < 1 minute (when exact-alarm permission granted; see F004-R25 for the degraded-mode window otherwise)
- PERF-08: APK size < 25 MB (without premium audio)
- PERF-09: RAM usage < 100 MB during normal use
- PERF-10: Battery impact < 2% per day (background)
- PERF-11: Database query time < 50ms for single record
- PERF-12: Content loading (first launch) < 5 seconds
- PERF-13: Smooth scrolling at 60fps in lists
- PERF-14: Widget update < 500ms
- **PERF-15 (NEW — v1.1): Content Migration diff-and-insert < 1 second for a typical incremental update (≤100 new verses)**

---

# 23. Security & Privacy

## 23.1 Data Storage
- SEC-01: All user data stored locally on device only
- SEC-02: No data transmitted to any server
- SEC-03: No user accounts or authentication required
- SEC-04: No personal information collected
- SEC-05: SharedPreferences use EncryptedSharedPreferences for premium status

## 23.2 Privacy Policy Requirements
- SEC-06: Privacy policy MUST be accessible in-app and on Play Store
- SEC-07: Privacy policy MUST state: no personal data collection; no data sharing with third parties; AdMob collects anonymized ad data (per Google policy); Firebase Analytics collects anonymized usage data; all religious content stored locally
- SEC-08: App MUST comply with GDPR (EU users)
- SEC-09: App MUST comply with COPPA (if children may use)
- SEC-10: Ad consent dialog for EU users (Google UMP SDK)
- **SEC-11 (NEW — v1.1):** The Google UMP (User Messaging Platform) SDK MUST be initialized and its consent status resolved **before** the AdMob SDK makes any ad request. If the user denies consent, no ads are shown, and a **"Support the app with Premium"** banner is shown in the ad slot as a fallback. *Rationale: without this sequencing, Google blocks AdMob requests from EU/UK users entirely, silently zeroing ad revenue from that region with no error surfaced to the app.*

## 23.3 Content Safety
- SEC-12: All religious content MUST be respectful and accurate
- SEC-13: No content that disparages any religion
- SEC-14: Sources MUST be cited for all verses
- SEC-15: Content review by religious scholars (recommended)

---

# 24. Testing Strategy

## 24.1 Unit Tests (Target: 70% coverage)

| Module | Test Cases |
|---|---|
| VerseRepository | getDailyVerse, search, random |
| StreakRepository | calculateStreak, resetStreak, freeze |
| AlarmScheduler | schedule, cancel, reschedule, **exact-vs-window fallback (v1.1)** |
| AlarmEscalationController | **escalation ramp, auto-snooze cycle cap, alarm_missed logging (v1.1, new)** |
| DateUtils | dayNumber calculation, midnight |
| ContentLoader | JSON parsing, validation |
| ContentMigrationManager | **diff-based insert, version comparison, no-data-loss on update (v1.1, new)** |
| ConsentManager | **UMP consent gate blocks AdMob init until resolved (v1.1, new)** |
| PremiumManager | purchase status, feature gating |

## 24.2 Integration Tests

| Flow | Test Cases |
|---|---|
| Onboarding | Complete flow, skip alarm, change religion, **exact-alarm permission prompt (v1.1)** |
| Daily Verse | Day change, verse cycling |
| Alarm | Set, ring, snooze, dismiss, **30-min escalation + 6-cycle auto-snooze (v1.1)** |
| Streak | Increment, reset, freeze |
| Bookmark | Add, remove, list |
| Search | Query, results, empty state |
| Content Update | **Bump JSON version → verify new verses added, existing streaks/bookmarks intact (v1.1)** |

## 24.3 Manual Testing Checklist
- [ ] Fresh install onboarding flow
- [ ] Each religion selection loads correct content
- [ ] Daily verse changes at midnight
- [ ] Alarm rings when app is closed
- [ ] Alarm rings after phone reboot
- [ ] **Unattended alarm escalates and auto-snoozes per F004-R19, then stops after 6 cycles (v1.1)**
- [ ] **Denying exact-alarm permission on Android 12+ still fires the alarm via setWindow(), with banner shown (v1.1)**
- [ ] TTS reads verse in correct language
- [ ] Share generates correct image
- [ ] Streak increments daily
- [ ] Streak resets after missed day
- [ ] Bookmark add/remove works
- [ ] Search finds verses correctly
- [ ] Widget displays and updates
- [ ] Premium purchase flow works
- [ ] Ads display correctly (free tier)
- [ ] Ads hidden (premium tier)
- [ ] **EU/UK test device: UMP consent prompt appears before any ad request; denial shows Premium fallback card (v1.1)**
- [ ] **Simulated app update with a bumped content JSON version preserves existing streaks/bookmarks and adds only new verses (v1.1)**
- [ ] Dark mode renders correctly
- [ ] Large font renders correctly
- [ ] Notifications arrive at correct times
- [ ] Background audio continues
- [ ] Lock screen controls work

## 24.4 Device Testing Matrix

| Device Category | Min Test Devices |
|---|---|
| Budget phones | 2 (e.g., Redmi, Samsung A series) |
| Mid-range | 2 (e.g., Pixel 6a, Samsung S21 FE) |
| Flagship | 1 (e.g., Pixel 8, Samsung S24) |
| Tablets | 1 (e.g., Samsung Tab) |
| Android 8.0 (API 26) | 1 (minimum supported) |
| Android 12 (API 31) | 1 — **required for v1.1 exact-alarm permission testing** |
| Android 14 (API 34) | 1 (latest) |

---

# 25. Release Plan

## 25.1 Play Store Listing

**App Title:** DailyDivine - Daily Verse & Prayer Alarm
**Short Description (80 chars):** Daily spiritual verses, prayer alarm & streak tracker for all faiths

**Full Description:** Start every morning with divine wisdom! DailyDivine delivers a personalized daily verse from your faith tradition, right to your phone. Features: daily verse from 7 major religions; smart prayer alarm that wakes you to sacred sounds; text-to-speech verse reading; spiritual streak building; a library of 5,000+ verses and prayers; bookmarking; beautiful shareable verse images; dark mode; a home screen widget. Supported faiths: Hinduism, Christianity, Islam, Buddhism, Sikhism, Judaism, and Spiritual/non-religious wisdom. 100% offline after install, battery friendly, no account required.

**Category:** Books & Reference · **Content Rating:** Everyone
**Tags:** daily verse, prayer, bible, quran, gita, devotional, spiritual, meditation, alarm, religious

## 25.2 App Store Assets Needed

| Asset | Size | Quantity |
|---|---|---|
| App Icon | 512x512 PNG | 1 |
| Feature Graphic | 1024x500 PNG | 1 |
| Phone Screenshots | 1080x1920 PNG | 8 |
| Tablet Screenshots | 1200x1920 PNG | 4 (opt) |
| Promo Video | YouTube link | 1 (opt) |

## 25.3 Screenshots Needed
1. Onboarding – Religion Selection · 2. Home Screen – Daily Verse (Light) · 3. Home Screen – Daily Verse (Dark) · 4. Alarm Setting Screen · 5. Alarm Ring Screen · 6. Library / Browse Categories · 7. Streak & Badges Screen · 8. Share Verse Image Preview

## 25.4 ASO (App Store Optimization) — NEW WORKSTREAM (v1.1)

Original PRD only listed ASO under goal G4 with no concrete process. Since DailyDivine has no marketing budget, Play Store search visibility is effectively the entire acquisition channel.

> **Release Plan – ASO (NEW):** Before launch, use Google Play's built-in A/B testing (**Experiments**) to test different screenshot sets and short-description variants. Prioritize these keyword targets: **"offline alarm," "daily Bible verse," "Quran alarm clock," "multi-faith devotional."** Review store listing performance and update descriptions **monthly** based on Play Console search-term analytics.

Implementation notes:
- Add an ASO checklist item to Phase 3 (Production Launch) below, and to the monthly maintenance task list (Section 27.1).
- Track keyword-level impressions/conversion in Play Console — this is separate from the Firebase events in Section 26.1.
- Title each screenshot-set Experiment arm around one of the four priority keyword phrases so it maps to a distinct search intent.

## 25.5 Release Phases

**Phase 1 – Closed Testing (2 weeks):** 20 internal testers; focus on crash detection and UX feedback; Play Console Internal Testing.

**Phase 2 – Open Beta (2 weeks):** 200 beta testers; focus on performance, alarm reliability (including the v1.1 escalation/auto-snooze behavior and exact-alarm fallback), and content accuracy; Play Console Open Testing.

**Phase 3 – Production Launch:** Staged rollout 10% → 25% → 50% → 100%; monitor crash rate, ANR rate, reviews; respond to all reviews within 24 hours; **kick off the first ASO Experiment (v1.1) alongside the staged rollout.**

**Phase 4 – Post-Launch (Week 1–4):** Daily analytics monitoring; hotfix critical bugs; respond to user feedback; **first monthly ASO review (v1.1)** based on early search-term data.

---

# 26. Analytics & KPIs

## 26.1 Events to Track (Firebase Analytics)

| Event Name | Parameters |
|---|---|
| onboarding_started | – |
| religion_selected | religion_name |
| language_selected | language_code |
| alarm_set | time, religion |
| onboarding_completed | duration_seconds |
| daily_verse_viewed | day_number, religion |
| verse_played_tts | verse_id, language |
| verse_bookmarked | verse_id |
| verse_shared | verse_id, share_method |
| verse_copied | verse_id |
| library_opened | – |
| category_viewed | category_id |
| verse_searched | query, results_count |
| random_verse_tapped | – |
| alarm_triggered | alarm_id |
| alarm_snoozed | alarm_id |
| alarm_dismissed | alarm_id, method |
| **alarm_missed (NEW v1.1)** | **alarm_id, snooze_cycles_used** |
| streak_milestone | days, badge_name |
| streak_broken | previous_streak |
| settings_opened | – |
| religion_changed | from, to |
| theme_changed | theme_mode |
| premium_screen_viewed | – |
| premium_purchased | – |
| premium_restored | – |
| ad_impression | ad_type, screen |
| ad_clicked | ad_type, screen |
| **ad_consent_resolved (NEW v1.1)** | **status (granted/denied/not_required)** |
| **content_migration_applied (NEW v1.1)** | **religion_id, from_version, to_version, verses_added** |
| widget_added | – |
| widget_tapped | – |
| notification_received | type |
| notification_tapped | type |
| journal_entry_written | word_count |
| app_opened | source (notification/widget/direct) |
| app_session_duration | seconds |

## 26.2 Key Performance Indicators (KPIs)

**Acquisition:** Daily installs · Install source (organic vs. search vs. referral) · Uninstall rate

**Engagement:** DAU · MAU · DAU/MAU ratio (stickiness) · Average session duration · Sessions per user per day · Verses read per session

**Retention:** Day 1, 7, 30 retention · Streak distribution (7+, 30+, 100+ day streaks) · Churn rate · **Alarm-missed rate as a leading indicator of alarm-driven churn (v1.1)**

**Revenue:** ARPU · ARPPU · Premium conversion rate · Ad eCPM · Daily/Monthly revenue · **EU/UK ad revenue post-consent-gate, to quantify SEC-11's revenue impact (v1.1)**

**Feature:** Alarm usage rate (% of users with active alarm) · TTS usage rate · Share rate · Bookmark rate · Widget adoption rate

---

# 27. Post-Launch Maintenance Plan

## 27.1 Minimal Maintenance Tasks

**Weekly (15 minutes):** Check Firebase Crashlytics for new crashes · Check Play Console for new reviews (respond) · Glance at revenue dashboard.

**Monthly (1 hour):** Review analytics KPIs · Check for Android SDK/library security updates · Review and respond to all Play Store reviews · Check AdMob policy compliance · **Run the monthly ASO review — keywords, screenshot Experiment results, description updates (v1.1).**

**Quarterly (2–4 hours):** Update targetSdk if a new Android version released · Update dependencies to latest stable versions · Review ASO (keywords, screenshots) · Consider adding 1 new language pack if demand exists.

**Yearly (1–2 days):** Major dependency updates · Android version compatibility check · Content review and expansion (bump content JSON `version` and let the Content Migration Manager handle the update safely, per F002-R13) · Review monetization strategy.

## 27.2 What Could Require Updates

| Trigger | Likelihood | Effort | Action |
|---|---|---|---|
| Android target SDK bump | Yearly | Low | Gradle change |
| AdMob SDK update | Quarterly | Low | Dependency bump |
| Play Billing library update | Yearly | Medium | Code changes |
| User-reported bug | Rare | Low | Hotfix |
| New Android feature (widget) | Rare | Medium | Optional |
| Content expansion request | Ongoing | Low | Add/bump JSON file — handled safely by Content Migration Manager (v1.1) |
| **Android exact-alarm policy change** | **Rare** | **Medium** | **Update AlarmScheduler fallback logic (v1.1 surface)** |

## 27.3 Estimated Annual Maintenance Time
Total: ~20–30 hours per year (truly minimal!).

---

# 28. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Religious content inaccuracy | Medium | High | Source from authoritative texts, cite references |
| Alarm not firing reliably | Medium | High | **[v1.1] AlarmClockInfo when granted, `setWindow()` fallback + banner when exact-alarm permission is denied; 30-min escalation + 6-cycle auto-snooze prevents silent failure** |
| Low Play Store visibility | High | High | **[v1.1] Structured ASO workstream — Experiments, keyword targeting, monthly review** |
| User complaints about ads | Medium | Medium | Non-intrusive placement, easy premium upgrade |
| Content sensitivity/offense | Low | High | Neutral tone, no comparative content between religions |
| Android API breaking changes | Low | Medium | Target stable APIs, avoid deprecated ones |
| TTS quality poor for some languages | Medium | Medium | Fallback to English, prompt language data download |
| Large APK size (with all audio) | Medium | Medium | Use Android App Bundle + dynamic delivery |
| Competitor launches similar app | Low | Medium | Focus on UX quality and multi-faith USP |
| Google Play policy violation | Low | High | Follow all policies, use official ad SDK |
| **[NEW v1.1] Content update wipes user data** | **Low (post-fix)** | **High** | **Diff-based Content Migration Manager (F002-R13) — never truncates verses/streaks/bookmarks tables** |
| **[NEW v1.1] EU/UK ad revenue blocked** | **Was High pre-fix** | **Medium (revenue)** | **UMP consent resolved before any AdMob request (SEC-11)** |

---

# 29. Timeline & Milestones

**Sprint Plan (2-week sprints)**

- **Sprint 1 (Wk 1–2) – Foundation:** Project setup in Codespaces; Gradle config; Room DB with all entities (incl. `content_versions`); content loader; base theme; navigation graph.
- **Sprint 2 (Wk 3–4) – Onboarding + Home:** 5-screen onboarding; religion/language selection; DataStore preferences; Home screen UI; daily verse display logic; day number calculation.
- **Sprint 3 (Wk 5–6) – Core Features:** Streak tracking; milestone badges/animations; bookmarks/favorites; copy/share; share image generation.
- **Sprint 4 (Wk 7–8) – Alarm System:** AlarmManager scheduling **with Android 12+ exact-alarm permission flow and setWindow() fallback (v1.1)**; alarm ring screen; **escalation + auto-snooze controller (v1.1)**; tone playback; boot receiver; snooze; alarm configuration UI.
- **Sprint 5 (Wk 9–10) – Audio & TTS:** TTS integration; language/speed settings; mini player; background playback service; lock screen controls; media notification.
- **Sprint 6 (Wk 11–12) – Library & Search:** Library screen with categories; verse list/detail; search; history tab; random verse; favorites tab.
- **Sprint 7 (Wk 13–14) – Monetization & Polish:** AdMob integration **gated by Google UMP consent (v1.1)**; Play Billing integration; premium feature gating; dark mode; font size settings; performance optimization.
- **Sprint 8 (Wk 15–16) – Content & Testing:** Load all 7 religion content files (730 verses each); **Content Migration Manager + its unit/integration tests (v1.1)**; load all alarm tones; unit tests; integration tests; manual testing on multiple devices; bug fixes.
- **Sprint 9 (Wk 17–18) – Launch Prep:** Home screen widget; notification system; Play Store listing prep; screenshots and feature graphic; **first ASO Experiment setup (v1.1)**; privacy policy (updated for SEC-11); internal testing release.
- **Sprint 10 (Wk 19–20) – Launch:** Beta testing (2 weeks); bug fixes from beta feedback; production release (staged rollout); monitor launch metrics; respond to initial reviews.

**Key Milestones**

| Milestone | Target Date | Status |
|---|---|---|
| Project Setup | End of Week 1 | Pending |
| Onboarding Complete | End of Week 4 | Pending |
| Core Features Done | End of Week 6 | Pending |
| Alarm System Working (incl. v1.1 escalation & permission flow) | End of Week 8 | Pending |
| Audio System Working | End of Week 10 | Pending |
| All Features Complete | End of Week 14 | Pending |
| Content Loaded (incl. Migration Manager) | End of Week 16 | Pending |
| Beta Release | End of Week 18 | Pending |
| Production Launch | End of Week 20 | Pending |

**TOTAL DEVELOPMENT TIME:** ~20 weeks (5 months) for a solo developer.

---

# 30. Appendix

## A. Content Source Links
- Hinduism: Bhagavad Gita (holy-bhagavad-gita.org), Upanishads (wisdomlib.org/hinduism) — Public Domain
- Christianity: King James Bible (kingjamesbibleonline.org) — Public Domain (1611, copyright expired)
- Islam: Quran (Yusuf Ali translation, multiple public domain sources), Hadith (Sahih Bukhari, Sahih Muslim) — Public Domain
- Buddhism: Dhammapada (accesstoinsight.org) — Public Domain (ancient text)
- Sikhism: Guru Granth Sahib (sikhitothemax.org) — Freely distributable (sacred text)
- Judaism: Torah/Tanakh — Public domain translations available
- Spiritual: Rumi, Stoics, Zen — Public domain (ancient/expired copyright)

## B. Audio Resources (Royalty-Free)
Alarm tones and nature sounds from freesound.org (CC0 license). Always verify license before including.

## C. Design Resources
Icons: Material Design Icons (Apache 2.0) · Illustrations: unDraw.co (free, no attribution) · Backgrounds: Unsplash (free, no attribution) · Lottie Animations: lottiefiles.com (free tier)

## D. Useful Android Documentation
AlarmManager, Room Database, TextToSpeech, Media3, Jetpack Compose, App Widgets, AdMob, Play Billing — see developer.android.com and developers.google.com/admob.

## E. Play Store Compliance Checklist
- [ ] Privacy policy URL added to Play Console (updated for SEC-11 UMP consent language)
- [ ] Content rating questionnaire completed
- [ ] App access instructions provided (if needed)
- [ ] Data safety section completed
- [ ] Ads declaration completed
- [ ] Target audience and content declared
- [ ] COVID-19 contact tracing declaration (N/A)
- [ ] Financial features declaration (N/A)
- [ ] Health features declaration (N/A)

## F. v1.1 Change Log (Pre-Development Review)

| Change | Sections Touched | New/Modified IDs |
|---|---|---|
| Escalating alarm timeout replaces flat 5-min auto-dismiss | §6.2 F004, §13 | F004-R19 (modified), AC11 |
| Graceful exact-alarm permission handling on Android 12+ | §6.2 F004, §13, §8, §9 | F004-R25 (new), AC12 |
| Diff-based content migration on app update | §6.2 F002, §10, §11, §19 | F002-R13 (new) |
| UMP consent gates AdMob init in EU/UK | §15, §23, §16.2 | SEC-11 (new) |
| ASO A/B testing and monthly keyword review | §25, §27 | Release Plan – ASO (new) |

See the standalone *DailyDivine PRD Change Addendum v1.1* for the full problem/rationale writeup behind each change, and Section 31 (Implementation Task Plan, separate document) for the engineering task breakdown.

---

*End of Product Requirements Document — v1.1.0*
