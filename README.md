# DailyDivine

Daily Devotional & Prayer Alarm — Android (Kotlin, Jetpack Compose), offline-first.

This repo is scaffolded from `DailyDivine_PRD_v1.1.md` (the full PRD, incorporating
the 5 pre-development-review changes) and `DailyDivine_Implementation_Task_Plan.docx`
(the sprint-by-sprint build plan). **See `CHECKLIST.md` for exactly what's
implemented vs. stubbed vs. not started** — it's updated as work lands, not
aspirational.

## What's real in this scaffold

- Full Gradle project (Kotlin DSL), working `./gradlew` wrapper (Gradle 8.4)
- Room database, 9 entities, additive migration (v1→v2)
- Hilt DI wiring
- Onboarding flow (5 screens) + Home screen, in Jetpack Compose
- The two v1.1 alarm-system fixes, fully implemented:
  - `alarm/AlarmEscalationController.kt` (F004-R19) — **unit tested, verified passing**
  - `alarm/AlarmScheduler.kt` exact-alarm permission fallback (F004-R25)
- The v1.1 Content Migration Manager (F002-R13), with unit tests
- A 5-verse sample content JSON proving the `assets/content/*.json` → Room pipeline

## Getting started (GitHub Codespaces, per PRD Section 17)

```bash
# Open this repo in a Codespace — .devcontainer/devcontainer.json installs
# JDK 17 + Android SDK 34 automatically.
./gradlew assembleDebug
./gradlew test
```

Codespaces doesn't support the Android Emulator — build the APK, download it
from the Codespaces file explorer, and install on a physical device (or open
the project in Android Studio locally for UI testing/emulator work).

## Documents

- `DailyDivine_PRD_v1.1.md` / `.pdf` — full product requirements
- `CHECKLIST.md` — live build progress
