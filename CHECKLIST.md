# DailyDivine — Implementation Checklist

Tracks actual engineering progress against the PRD (v1.1) Section 29 sprint plan.
Updated as work lands in this repo — not aspirational, reflects what's really here.

Legend: ✅ done & present in repo · 🟡 partial/stubbed · ⬜ not started

---

## Sprint 1 — Foundation

| Task | Status | Notes |
|---|---|---|
| Project setup (Gradle, `settings.gradle.kts`, `.devcontainer`) | ✅ | Builds against Gradle 8.4, AGP 8.2.2, Kotlin 1.9.22 |
| Working `./gradlew` wrapper | ✅ | Generated and verified locally — points at Gradle 8.4 |
| Room database with all entities | ✅ | 9 entities incl. `ContentVersion` (v1.1) — see `data/local/entity/` |
| Additive schema migration (no data loss) | ✅ | `AppDatabase.MIGRATION_1_2`, wired in `DatabaseModule` — deliberately **not** `fallbackToDestructiveMigration()` |
| Content loader (JSON → Room) | ✅ | `ContentLoader.kt` — parses `assets/content/*.json` |
| Sample content file | 🟡 | `hinduism_en.json` has 5 real verses in the correct schema; full 730×11 content library is a **content-authoring task**, not engineering — not attempted here |
| Base theme / design system | ✅ | `ui/theme/` — per-religion color palettes (Section 20.1), typography scale |
| Navigation graph | ✅ | `ui/navigation/NavGraph.kt` — onboarding + Home wired; Library/Alarm/Settings routes reserved |
| Hilt DI setup | ✅ | `DailyDivineApp`, `di/DatabaseModule.kt`, `di/AppModule.kt` |
| Launcher icons | ✅ | Generated at all 5 mipmap densities (placeholder art — swap before release) |

## Sprint 2 — Onboarding + Home

| Task | Status | Notes |
|---|---|---|
| Onboarding flow (5 screens) | ✅ | `WelcomeScreen`, `ReligionSelectScreen`, `LanguageSelectScreen`, `AlarmSetupScreen`, `PermissionScreen` |
| Religion/language selection UI | ✅ | 7-religion grid (S03), language list filtered per religion (S04) via `util/Religions.kt` |
| DataStore preferences | ✅ | `data/local/datastore/UserPreferences.kt` — religion, language, install date, onboarding-completed, notifications-granted all persisted. Wired into onboarding via a shared `OnboardingViewModel` scoped to a nested nav graph (all 5 screens share one instance/one set of in-progress selections) |
| Home screen UI | ✅ | `HomeScreen.kt` — greeting (now shows the real selected religion name), verse card, streak card |
| Daily verse display logic | ✅ | `VerseRepository.getDailyVerse()` — deterministic day-number algorithm (API Contract 1), now fed by the **real** persisted `religionId`/install date via `HomeViewModel`, not a hardcoded placeholder |
| Day number calculation | ✅ | Implemented + matches PRD Section 19 contract exactly |
| Returning-user start destination | ✅ | `MainViewModel` resolves `onboardingCompleted` from DataStore and gates the splash screen (`setKeepOnScreenCondition`) until known — a returning user lands on Home directly, never re-sees Welcome |
| Content actually loads on onboarding completion | ✅ | `OnboardingViewModel.completeOnboarding()` now calls `ContentMigrationManager.migrateIfNeeded()` for the selected religion — previously built (Sprint 1) but never invoked anywhere. Only Hinduism has real sample content (`hinduism_en.json`); other religions show a clear empty-state message rather than a blank/confusing screen |
| Real Android notification permission (`POST_NOTIFICATIONS`) | ⬜ | Permission screen (S06) records the user's stated preference to DataStore but does not yet trigger the actual system permission dialog — needs an `ActivityResultContracts.RequestPermission` launcher wired from `MainActivity`. Flagged inline in `PermissionScreen.kt` |

## Sprint 3 — Core Features (Streaks, Bookmarks, Share)

| Task | Status | Notes |
|---|---|---|
| Streak tracking | ✅ | `StreakRepository.calculateStreak()` — consecutive-day walk, milestone lookup |
| Milestone badges (data) | ✅ | `StreakInfo.MILESTONES` — all 10 badges from Section 6.2 |
| Milestone badge animations | ⬜ | Needs Lottie asset + `BadgeAnimation.kt` composable |
| Bookmark/Favorites | 🟡 | `BookmarkDao` done; no UI screen yet |
| Copy/share functionality | ⬜ | Icon buttons stubbed in `HomeScreen.kt`, no handlers wired |
| Share image generation | ⬜ | Not started |

## Sprint 4 — Alarm System ⭐ (v1.1 changes live here)

| Task | Status | Notes |
|---|---|---|
| `AlarmScheduler` (schedule/cancel/snooze) | ✅ | Includes v1.1 exact-alarm fallback |
| **F004-R25 (v1.1): exact-alarm permission fallback** | ✅ | `canScheduleExactAlarms()` gates `setAlarmClock()` vs `setWindow()`; reliability banner in `AlarmSetupScreen` |
| **F004-R19 (v1.1): escalation / auto-snooze cap** | ✅ | `AlarmEscalationController` — **unit-tested and verified standalone** (11/11 assertions pass) |
| `AlarmService` (foreground service, tone playback) | ✅ | Drives the escalation loop, volume ramp, WakeLock. Now also handles `ACTION_STOP` (sent by `AlarmRingActivity`) and launches the ring screen via a full-screen notification intent |
| `AlarmReceiver` | ✅ | Starts the foreground service |
| `BootReceiver` (reschedule after reboot) | ✅ | Now a real `@AndroidEntryPoint` receiver, Hilt-injects `AlarmRepository`, calls `rescheduleAllEnabled()` on `ACTION_BOOT_COMPLETED` — was a TODO stub before this session |
| `AlarmRepository` (Room + AlarmScheduler wiring) | ✅ | **New this session.** Closes the gap where `Alarm` entities and `AlarmScheduler` existed but were never connected — every create/update/delete now both persists to Room AND (re)schedules or cancels the real system alarm in one call, so they can't drift out of sync |
| **Onboarding actually creates a real alarm** | ✅ | **New this session.** Previously "Set Alarm →" and "Skip, I'll set up later" did the *exact same thing* (both just navigated forward) — no alarm was ever created either way, regardless of which button was tapped. `OnboardingViewModel` now tracks `wantsAlarm` distinctly per button and calls `AlarmRepository.createAndSchedule()` on completion only when true |
| Alarm ring screen (S08) | ✅ | **New this session.** `AlarmRingActivity.kt` — Snooze / Wake Up & Read buttons, current time display, back-press absorbed (can't silently dismiss an alarm). Verse preview (first line of today's verse) deliberately deferred — needs a `VerseRepository` lookup keyed off the alarm, tracked below |
| **API 26 lock-screen fallback** | ✅ | **New this session.** `showWhenLocked`/`turnScreenOn` manifest attributes only take effect on API 27+; `AlarmRingActivity` now also sets the older `WindowManager.LayoutParams` flags (`FLAG_SHOW_WHEN_LOCKED`, `FLAG_TURN_SCREEN_ON`, `FLAG_DISMISS_KEYGUARD`) for exactly API 26, this project's `minSdk` floor |
| Alarm tone playback (real audio) | 🟡 | `AlarmService` plays from `res/raw/`; only a **placeholder** `temple_bell.mp3` exists — needs real royalty-free audio (Appendix B) |
| Alarm configuration UI (S11) | ⬜ | Still not started — no screen to view/edit/delete the alarm created during onboarding, or add additional alarms (premium, up to 5 per F004-R02) |
| Verse preview on the ring screen | ⬜ | Deferred this session (see above) — `AlarmRingActivity` shows a generic greeting instead of the actual daily verse's first line |
| Time picker in onboarding (S05) | ⬜ | `AlarmSetupScreen` still shows static "Default alarm time: 5:30 AM" text rather than an interactive picker — the alarm that gets created this session always uses 5:30 AM. A real `TimePicker` is experimental in this BOM version (same `@OptIn` pattern as the `Card` fix would be needed) — scoped out to keep this pass focused |
| Unit tests: `AlarmEscalationControllerTest` | ✅ | 4 test cases, verified passing |

## Sprint 5 — Audio & TTS

| Task | Status | Notes |
|---|---|---|
| TTS integration | ✅ | `TTSManager.kt` — matches PRD Section 12.1 exactly |
| TTS wired into Home screen play button | ⬜ | Icon present, handler not wired |
| Mini player UI | ⬜ | Not started |
| Background playback service (prayers) | ⬜ | Not started |

## Sprint 6 — Library & Search
All ⬜ — not started this pass.

## Sprint 7 — Monetization & Polish

| Task | Status | Notes |
|---|---|---|
| **SEC-11 (v1.1): UMP dependency added** | ✅ | `user-messaging-platform:2.2.0` in `app/build.gradle.kts` |
| `ConsentManager` wrapper | ⬜ | Dependency present, class not yet written |
| AdMob integration | ⬜ | Not started (gate on ConsentManager per SEC-11) |
| Play Billing integration | ⬜ | Dependency present (`billing-ktx`), no code yet |
| Dark mode | ✅ | `DailyDivineTheme` supports `darkTheme` param via system default |

## Sprint 8 — Content & Testing

| Task | Status | Notes |
|---|---|---|
| **F002-R13 (v1.1): Content Migration Manager** | ✅ | `ContentMigrationManager.kt` — diff-based insert, version-gated, never truncates |
| Migration unit tests | ✅ | `ContentMigrationManagerTest.kt` — 3 test cases (first install, version bump, no-op) using DAO fakes |
| Full 730-verse content × 11 files | ⬜ | Content-authoring task, out of scope for this engineering pass |
| Integration/instrumented tests | ⬜ | Needs a connected device/emulator — not runnable in this sandbox |

## Sprint 9 — Launch Prep
All ⬜ — not started this pass (widget, notifications, Play Store assets).

## Sprint 10 — Launch
Not applicable yet.

---

## v1.1 Change Verification Summary

| Change | Implemented | Verified |
|---|---|---|
| F004-R19 — alarm escalation/auto-snooze | ✅ `AlarmEscalationController.kt` | ✅ Compiled + ran standalone with `kotlinc`; **11/11 assertions passed**, incl. proof the loop always terminates within 6 cycles instead of looping forever or going silent |
| F004-R25 — exact-alarm permission fallback | ✅ `AlarmScheduler.kt` | 🟡 Logic reviewed; needs a physical API 31+ device to verify the real `setWindow()` fallback fires (can't emulate `AlarmManager.canScheduleExactAlarms()` outside Android) |
| F002-R13 — Content Migration Manager | ✅ `ContentMigrationManager.kt` | ✅ Diff/insert/version-stamp contract verified via `ContentMigrationManagerTest.kt` (DAO-fake based — see in-file note on the one known gap: `ContentLoader` itself needs a `ContentSource` interface extraction to be exercised end-to-end in a plain JVM test) |
| SEC-11 — UMP consent gate | 🟡 Dependency wired | ⬜ `ConsentManager` class not yet written |
| ASO workstream | N/A (process, not code) | — Tracked in `DailyDivine_Implementation_Task_Plan.docx`, Sprint 9 |

---

## Known Gaps / Honest Limitations

1. **This was built in a sandboxed environment without the Android SDK or an emulator.** Every file here is real, hand-written Kotlin — not templated boilerplate — but the project has **not** been run through a full `./gradlew assembleDebug`. What *has* been verified:
   - `AlarmEscalationController` compiled and executed standalone with `kotlinc` (zero Android dependencies by design), all assertions passing.
   - The Gradle wrapper (`./gradlew`) was actually generated and points at a real Gradle 8.4 distribution.
   - JSON content matches the Kotlin data classes field-for-field.
   - The Room migration is additive-only (no `fallbackToDestructiveMigration()`), which is the actual mechanism the F002-R13 fix depends on.
2. **First real build should happen in GitHub Codespaces** per the PRD's own Section 17 — that's where `./gradlew assembleDebug` will surface any remaining wiring issues (e.g. the `BootReceiver` DB access TODO).
3. Full religious content (730 verses × 11 religion/language files) is a content-authoring and licensing effort, not something to fabricate — only a 5-verse Hinduism sample is included to prove the pipeline.
4. Placeholder assets: launcher icon (generated, not designed) and one placeholder `temple_bell.mp3` (a stub file, not real audio) — both flagged inline in code comments.

---

## Next Session Priorities (suggested order)
1. Open in Codespaces, run `./gradlew assembleDebug`, fix whatever the real compiler finds.
2. Wire DataStore into onboarding (closes the Sprint 2 gap).
3. Build `AlarmRingActivity` (S08) — the escalation controller has nothing to interact with yet.
4. Extract `ContentSource` interface so `ContentMigrationManager` gets a true end-to-end unit test, not just its DAO contract.

---

## Environment / Build Issues Log

Real problems hit while actually running this in Codespaces, and how they were fixed. Append to this, don't just overwrite — it's the debugging history.

### 2026-09-09 — `./gradlew assembleDebug` fails immediately, `BUILD FAILED`, cryptic `* What went wrong: 25.0.4.1`

**Symptom:** first real build attempt in the pushed Codespace failed before compiling anything. `java -version` in that terminal showed **OpenJDK 25.0.4.1** (Microsoft build) as the active JDK.

**Root cause:** Gradle 8.4 (this project's wrapper version) officially only runs on JVM 17–21 (some later 8.x patches stretch to 24). It does **not** run on JDK 25 — that requires **Gradle 9.1.0+**, confirmed against Gradle's own compatibility matrix. The Codespace's default `java` resolved to a JDK 25 the base machine ships, overriding/ignoring the JDK 17 our `.devcontainer/devcontainer.json` asks for — almost certainly because **the container was never rebuilt** after `.devcontainer/` was pushed (GitHub Codespaces doesn't auto-rebuild an already-running codespace when devcontainer config changes land on `main`).

**Why not just upgrade Gradle to 9.1+?** Because AGP (Android Gradle Plugin) 9.0+ requires Gradle 9.1+ *and* is a major release with breaking changes (built-in Kotlin support replaces the separate `org.jetbrains.kotlin.android` plugin, DSL changes). That's a real migration, not a quick fix, and this project doesn't need JDK 25 for anything — it only needs *a* supported JDK to run the Gradle daemon. Pinning to JDK 17 (which the devcontainer already installs) is the lower-risk fix.

**Fix applied:**
1. `.devcontainer/devcontainer.json` — added `containerEnv.JAVA_HOME` pinned to the SDKMAN-managed `.../candidates/java/current` path the `java:17` feature maintains, so the Gradle daemon uses 17 regardless of what else is on the base image's `PATH`.
2. `.devcontainer/setup.sh` — now explicitly runs `sdk default java <17.x-tem>` and exports/persists `JAVA_HOME`/`PATH` to `~/.bashrc`, with a printed fallback recovery procedure if SDKMAN isn't where expected.

**What you need to do:** Command Palette → **"Codespaces: Rebuild Container"** (or delete and recreate the Codespace from the latest `main`) so the updated devcontainer config actually takes effect — pushing the file alone doesn't retroactively fix an already-running container.

**If you can't rebuild right now**, unblock the current terminal directly:
```bash
ls /usr/local/sdkman/candidates/java/        # find the 17.x-tem folder name
export JAVA_HOME=/usr/local/sdkman/candidates/java/<that-folder>
export PATH=$JAVA_HOME/bin:$PATH
java -version                                 # confirm it now says 17.x
./gradlew assembleDebug
```
If there's no `/usr/local/sdkman` directory at all, the `java:17` devcontainer feature never ran — that's the rebuild-container case, not a JAVA_HOME problem.

**Status:** ✅ Fixed and verified — the JDK/SDK environment issues are resolved. See the next entry for the first real code bug this build actually caught.

### 2026-09-09 — First real code bug, caught by an actual `./gradlew assembleDebug`: `processDebugResources FAILED`

**Symptom:** with JDK and Android SDK both correctly resolved, the build reached real resource linking and failed:
```
error: style attribute 'android:attr/windowShowWhenLocked' not found.
error: style attribute 'android:attr/windowTurnScreenOn' not found.
```

**Root cause:** genuinely my mistake, not an environment problem. `res/values/themes.xml` declared `Theme.DailyDivine.AlarmRing` with `<item name="android:windowShowWhenLocked">` and `<item name="android:windowTurnScreenOn">` — **these theme attributes don't exist in the Android framework.** I conflated them with the real mechanism: `showWhenLocked` and `turnScreenOn` are **`<activity>` manifest attributes** (added API 27), not style/theme items. On top of that, the manifest itself had the attribute name slightly wrong too (`android:showOnLockScreen`, which also isn't real — the correct name is `android:showWhenLocked`).

**Fix applied:**
1. `AndroidManifest.xml` — `android:showOnLockScreen` → `android:showWhenLocked` (the real attribute) on `AlarmRingActivity`.
2. `res/values/themes.xml` — removed both invalid `<item>` lines; `Theme.DailyDivine.AlarmRing` is now just a plain style with no bogus attributes, since the lock-screen behavior lives on the `<activity>` element instead.
3. Swept every other XML resource in the project for the same class of mistake (`grep` for `android:window*`/`android:show*`/`android:turn*`) — this was the only occurrence.
4. Noted a real follow-up: `showWhenLocked`/`turnScreenOn` only take effect on API 27+, but this project's `minSdk` is 26. `AlarmRingActivity` (not yet built) should additionally set `WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED`/`FLAG_TURN_SCREEN_ON` at runtime as an API 26 fallback.

**Status:** ✅ Fixed in this repo. **Not yet re-verified against a real Gradle build** — please pull and re-run `./gradlew assembleDebug`; if resource linking passes, that confirms this specific fix and the build should move on to actually compiling the Kotlin sources, which is the first real test of everything in `alarm/`, `data/`, and `ui/`.

### 2026-09-09 — Second real code bug, caught by KSP during Kotlin compilation: `kspDebugKotlin FAILED`

**Symptom:** resource linking passed (previous fix confirmed working). Next failure was in annotation processing:
```
e: [ksp] .../Converters.kt:8: Class is referenced as a converter but it does not have any converter methods.
w: [ksp] .../AppDatabase.kt:27: Schema export directory was not provided...
e: Error occurred in KSP, check log for detail
```

**Root cause (the blocking error):** `AppDatabase` declared `@TypeConverters(Converters::class)`, but `Converters.kt` was an intentionally-empty placeholder class with zero `@TypeConverter`-annotated methods. Room's KSP processor now hard-fails on that combination rather than silently ignoring it — reasonable behavior on Room's part, this was genuinely dead/premature code on mine. None of the current entities need a converter (every field is a Room-native primitive), so the annotation shouldn't have been there yet.

**Root cause (the warning, non-blocking but fixed anyway):** `exportSchema = true` was set with no `room.schemaLocation` configured, so Room had nowhere to write schema snapshots.

**Fix applied:**
1. `AppDatabase.kt` — removed `@TypeConverters(Converters::class)` and the now-unused `import androidx.room.TypeConverters`. Left a comment explaining exactly when to re-add it (once a field actually needs a converter, e.g. `List<Int>` stored as JSON).
2. `Converters.kt` — comment updated to explain it's deliberately unreferenced for now, not accidentally orphaned.
3. `app/build.gradle.kts` — added a `ksp { arg("room.schemaLocation", "$projectDir/schemas") }` block so Room exports schema JSON on every build.
4. Created `app/schemas/` (with a `.gitkeep`) so the directory exists and its exported contents get committed — useful later for testing `MIGRATION_1_2` against a real prior-schema snapshot rather than just the hand-written SQL.
5. Re-swept **every** `.kt` and `.gradle.kts` file in the project for brace/paren balance after these edits (I broke and had to re-fix `app/build.gradle.kts`'s `packaging {}` block mid-edit — caught by the same sweep before it ever reached you this time).

**Status:** ✅ Fixed in this repo. **Not yet re-verified against a real Gradle build.**

### 2026-09-09 — GitHub Actions CI added (build + test + downloadable APK)

**What changed:** upgraded `.github/workflows/build.yml` from the original PRD scaffold to actually be usable for on-demand device testing:
- Added `workflow_dispatch` trigger — you can hit "Run workflow" in the Actions tab any time, no push needed.
- Swapped the manual `actions/cache` block for `gradle/actions/setup-gradle@v4`, the currently-recommended official caching action (confirmed current as of this session — the older `android-actions/setup-android@v3` was also confirmed still correct/maintained, not deprecated).
- Split `./gradlew test` into `testDebugUnitTest` specifically (faster — doesn't also run release-variant tests) and run it *before* `assembleDebug`, so a broken unit test fails fast instead of after a slow APK build.
- `--stacktrace` on both, so a CI failure gives you a real stack trace instead of a one-line summary.
- Uploads the debug APK as a downloadable artifact (`DailyDivine-debug-apk`, 14-day retention) and, separately, unit test result XML even when the build fails (`if: always()`), so a red run still tells you *why*.

**Why this build won't hit the JDK-25 problem:** `actions/setup-java@v4` pins JDK 17 explicitly on a clean runner — the whole Codespace issue was a pre-existing default JDK colliding with what we asked for. That can't happen here the same way.

**How to use it (once pushed):**
1. GitHub → your repo → **Actions** tab → **Android CI** (left sidebar) → **Run workflow** button (top right) → pick `main` → **Run workflow**.
2. Wait for the green check (first run will be slower — no Gradle cache yet; subsequent runs are faster).
3. Click into the completed run → scroll to **Artifacts** at the bottom → download **DailyDivine-debug-apk** (downloads as a `.zip` containing the `.apk`).
4. Get it onto your phone (any of: email it to yourself, upload to Google Drive and download on-device, or `adb install app-debug.apk` over USB with the phone connected to your PC) → tap the `.apk` on your phone → allow "install from unknown sources" if prompted → install.
5. This is a **debug build**, auto-signed with a debug key — it installs fine for testing but is not suitable for the Play Store (that needs the release signing setup from PRD Section 25, not yet done).

**Status:** ✅ Workflow written, YAML-validated locally. **Not yet run for real** — first live run on your push will also serve as the actual verification that everything fixed in the last three sessions (JDK pin, SDK, theme attrs, Room converters) genuinely compiles clean, independent of your Codespace's environment quirks.

### 2026-09-09 — First real CI run, caught immediately: `android-actions/setup-android@v3` fails on `Failed to find package 'tools'`

**Symptom:** the very first Actions run failed before Gradle even started, inside the "Set up Android SDK" step:
```
Warning: Failed to find package 'tools'
Error: The process '.../sdkmanager' failed with exit code 1
```

**Root cause:** confirmed via research, not a guess — `android-actions/setup-android@v3`'s default `packages` input is `"tools platform-tools"`. The standalone `tools` package (the old, pre-`cmdline-tools` Android SDK Tools bundle) was removed from Google's SDK repository years ago and no longer exists at any version. Any workflow using this action's defaults with no `packages:` override hits this immediately on a fresh runner — this isn't specific to our project.

**Fix applied:** pass `packages: ''` to the action so it only sets up licenses/environment without trying to install the dead `tools` package, then explicitly install exactly what we need via a separate `sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"` step. This is also more explicit/predictable than relying on the action's implicit defaults.

**Status:** ✅ Fixed in this repo. **Not yet re-verified** — next push's Actions run is the real test.

### 2026-09-09 — CI blocked by Gradle Wrapper security validation: our wrapper jar wasn't "genuine"

**Symptom:** past the `setup-android` fix, `gradle/actions/setup-gradle@v4` failed immediately with:
```
✗ Found unknown Gradle Wrapper JAR files:
  b5173cbc...02bf365 gradle/wrapper/gradle-wrapper.jar
Error: At least one Gradle Wrapper Jar failed validation!
```

**Root cause — and this one is a real, useful security control, not a bug to just silence:** `setup-gradle@v4` automatically runs Gradle's [Wrapper Validation](https://github.com/gradle/actions/blob/main/docs/wrapper-validation.md) check on every run. It compares the SHA-256 of every `gradle-wrapper.jar` in the repo against Gradle's own published list of checksums for legitimate, official releases — specifically to catch supply-chain attacks where someone slips a tampered wrapper jar into a repo (the wrapper jar is executable code that runs on every build). Our `gradle-wrapper.jar` failed because it was never actually an official Gradle release artifact — back when this project was first scaffolded, it was generated locally using a very old Gradle 4.4.1 (from Ubuntu's `apt` package) running `gradle wrapper --gradle-version 8.4`, which produces a *functionally* correct but *byte-different* jar from Gradle's real 8.4.0 release. Same root issue affected `gradlew`/`gradlew.bat` — diffing them against the genuine v8.4.0 scripts showed they were generated by that same old, different wrapper-template generator, not actual Gradle 8.4 output.

**Fix applied:** replaced all three wrapper files with the genuine, official versions:
1. Downloaded `gradle-wrapper.jar` directly from Gradle's own `gradle/gradle` GitHub repo at tag `v8.4.0` (`raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar`).
2. **Verified, not assumed:** cross-checked its SHA-256 (`0336f591...aa0ef15`) against `gradle/actions`' own published `wrapper-checksums.json` — it's the exact, listed checksum for version `8.4`. This is the same list `setup-gradle` validates against, so this specific failure is now provably fixed, not just "should probably work."
3. Replaced `gradlew` and `gradlew.bat` with the matching genuine v8.4.0 scripts from the same source, and re-set the executable bit on `gradlew` (lost by default when downloading a fresh file).
4. `gradle-wrapper.properties` was already correctly pointing at `gradle-8.4-bin.zip` — no change needed there, it just wasn't consistent with the jar/scripts until now.

**Follow-up not yet done:** `gradle-wrapper.properties` could additionally pin `distributionSha256Sum` for the distribution zip itself (extra integrity layer beyond just the wrapper jar) — skipped for now since it wasn't blocking anything and `services.gradle.org` isn't reachable from this session's sandboxed environment to fetch that checksum; low priority, can add later.

**Status:** ✅ Fixed and independently verified against Gradle's own checksum list. **Not yet run in a real Actions build** — this should be the last environment-layer fix; if this goes green, everything downstream is finally testing the actual app code.

### 2026-09-09 — First real Kotlin compilation attempt: 2 genuine API-usage bugs found and fixed

**This is the milestone we've been building toward.** Every previous fix in this log was environment/tooling (JDK, SDK, wrapper security). This run got past all of that and reached `:app:compileDebugKotlin` — the actual Kotlin compiler, checking the actual app code, for the first time. It found exactly two real bugs, both API-version mismatches, no environment issues at all:

```
e: HomeScreen.kt:95:13 None of the following functions can be called with the arguments supplied:
   public fun LinearProgressIndicator(progress: Float, ...)   [only this overload exists]
e: ReligionSelectScreen.kt:35:17 This material API is experimental and is likely to change or to be removed in the future.
```

**Bug 1 — `HomeScreen.kt`:** I wrote `LinearProgressIndicator(progress = { streak.progressToNext }, ...)`, using the lambda-based `progress: () -> Float` overload. That overload was added in a **newer** Compose Material3 release than the one this project's `compose-bom:2024.01.00` pins — that BOM version only exposes the plain `progress: Float` overload. **Fix:** `progress = streak.progressToNext` (no lambda).

**Bug 2 — `ReligionSelectScreen.kt`:** the clickable `Card(onClick = { ... }, ...)` overload (used for the tappable religion-selection cards) is marked `@ExperimentalMaterial3Api` in this BOM version, and Kotlin treats using an experimental API without opting in as a hard compile **error**, not a warning. **Fix:** added `@OptIn(ExperimentalMaterial3Api::class)` to `ReligionSelectScreen`.

**Verification beyond just fixing the two reported lines:** swept the entire `app/src/main/java/` tree for (a) any other `progress = { ... }` lambda usages — none found beyond the fixed one — and (b) every other Material3 component actually in use in the project (`grep` for `Scaffold`, `TopAppBar`, `ModalBottomSheet`, etc.) — `Card` was the *only* one in use, and its one clickable instance is now the fixed one. So this isn't "fixed the two lines the compiler happened to mention" — it's "confirmed these are the only two instances of these two bug classes anywhere in the codebase."

**Status:** ✅ Fixed in this repo, and swept for recurrence elsewhere. **Not yet re-verified against a real Gradle build** — but for the first time, a green run here means the actual Compose UI code compiles, not just the environment around it.

### 2026-09-19 — First real runtime crash, caught on an actual physical device: AdMob's auto-init `ContentProvider` crashes on launch, unconditionally

**This is a different class of bug than everything above.** The build succeeded, the APK installed — and then the app crashed immediately on open, before showing anything. This can't be caught by a compiler; it only shows up by actually running the app, which is exactly what happened here (via `adb logcat --uid=<app-uid>`, filtered to just this app's process).

**The actual crash** (from `adb logcat AndroidRuntime:E`):
```
FATAL EXCEPTION: main
Process: com.dailydivine.app, PID: 32695
java.lang.RuntimeException: Unable to get provider com.google.android.gms.ads.MobileAdsInitProvider: java.lang.IllegalStateException:
*** The Google Mobile Ads SDK was initialized incorrectly. AdMob publishers should follow the instructions here... to add a valid App ID inside the AndroidManifest. ***
    at android.app.ActivityThread.installProvider(...)
    at android.app.ActivityThread.installContentProviders(...)
    at android.app.ActivityThread.handleBindApplication(...)
```

**Root cause:** the `play-services-ads` (AdMob) dependency auto-registers a `ContentProvider` (`MobileAdsInitProvider`) via its own bundled manifest, which gets merged into ours. Android installs all `ContentProvider`s during `handleBindApplication` — **before `Application.onCreate()`, before Hilt, before `MainActivity`, before anything in our code runs at all.** That provider's `attachInfo()` unconditionally requires an AdMob App ID `<meta-data>` entry in the manifest, and throws `IllegalStateException` if it's missing. We added this dependency back in the initial Sprint 1 scaffold (forward-looking, for Sprint 7's monetization work), but Sprint 7 hasn't started — no App ID, no `ConsentManager`, nothing configured. So the dependency sat there as a live landmine: the very first time the app was actually run on a device (this session — every prior "success" was compile-only), it went off.

**Fix applied:**
1. Commented out `play-services-ads` and `user-messaging-platform` in `app/build.gradle.kts`, with an inline comment explaining exactly why, quoting the crash, and instructing that both must be re-enabled *together* with the required manifest App ID as a single Sprint 7 change — never the dependency alone.
2. Verified via `grep` that zero lines of actual code reference `com.google.android.gms.ads.*` or `com.google.android.ump.*` — confirms removing the dependency breaks nothing currently working.
3. **Proactively checked for the same bug class elsewhere** rather than declaring victory after one fix: Firebase Analytics is also in `build.gradle.kts`, also unconfigured (no `google-services.json`, `google-services` plugin not applied), also has zero real code references. Researched whether `FirebaseInitProvider` has the same unconditional-crash behavior as AdMob's provider — **it doesn't**: it degrades gracefully when unconfigured rather than throwing during provider install, and only fails if code later calls a Firebase API (ours doesn't). So it's **not** a confirmed live crash risk the way AdMob was, and I left it in place rather than making an unverified speculative change. **Flagged here for Sprint 7:** apply the `google-services` plugin and add `google-services.json` at the same time Firebase Analytics logging actually gets wired into `AlarmService.kt`'s commented-out `alarm_missed` event — don't let it sit configured-but-unused the way AdMob just did.

**Status:** ✅ Fixed and root-caused with a real stack trace, not a guess. **Not yet re-verified** — next build + device install is the real test. This is also the first crash in the whole project that a `./gradlew assembleDebug` (or even the CI pipeline) *cannot* catch — worth remembering that "CI is green" and "the app actually runs" are genuinely different levels of verification, and Sprint 7+ should budget for real device testing before assuming a green build means a working app.

### 2026-09-20 — Second real runtime crash: Compose BOM version skew via `androidx.glance`, `NoSuchMethodError` in the loading spinner

**Symptom:** the AdMob crash fix worked — the app now opens, and onboarding (Religion → Language → Alarm setup) works correctly. Crash happens on reaching the **Home screen**, specifically while the loading spinner (`CircularProgressIndicator`) is showing:
```
FATAL EXCEPTION: main
java.lang.NoSuchMethodError: No virtual method at(Ljava/lang/Object;I)Landroidx/compose/animation/core/KeyframesSpec$KeyframeEntity;
  in class Landroidx.compose.animation.core.KeyframesSpec$KeyframesSpecConfig
    at androidx.compose.material3.ProgressIndicatorKt$CircularProgressIndicator$endAngle$1.invoke(ProgressIndicator.kt:371)
    ...
    at com.dailydivine.app.ui.home.HomeScreenKt.HomeScreen(HomeScreen.kt:37)
```

**Root cause:** a classic **Compose BOM version-skew bug**, not a logic error in our code. `material3`'s `CircularProgressIndicator` (from `compose-bom:2024.01.00`) was compiled against a specific version of `androidx.compose.animation:animation-core` and calls a method (`KeyframesSpecConfig.at(...)`) that exists with that exact signature only in that version. But the actual `animation-core` .jar that ended up bundled in the final APK was a **different, incompatible version** — meaning some other dependency pulled its own transitive `animation-core` version that "won" Gradle's dependency resolution instead of the BOM's. `androidx.glance:*:1.0.0` (added early for the still-unbuilt Sprint 9 home screen widget) is a well-known culprit for exactly this: it predates our Jan-2024 BOM (released mid/late-2023) and has documented history of fighting Compose BOM version alignment.

**Fix applied:** commented out `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3` in `app/build.gradle.kts`, same pattern as the AdMob fix — confirmed via `grep` that zero code currently references Glance (the widget feature is Sprint 9, not started), so nothing breaks. Also checked Lottie (also unused) as a weaker secondary suspect and left it alone — Lottie has its own internal animation engine and doesn't typically depend on `compose.animation.core`, so it's a much less likely contributor and I didn't want to remove more than the evidence supports in one change.

**Being honest about confidence level here:** unlike the AdMob fix (where the stack trace pointed at AdMob's own code directly), this fix is **strong circumstantial reasoning, not a proven-certain root cause** — I don't have a way to run `./gradlew :app:dependencies` myself in this sandbox to confirm Glance was definitively the version that "won" resolution. If the *exact same* `KeyframesSpecConfig.at()` crash recurs after this fix, that disproves Glance as the cause and the next suspects to isolate (one at a time) are `lottie-compose` and then `navigation-compose`'s pinned version against this BOM.

**Status:** 🟡 Fix applied based on strong (not certain) reasoning. **Needs a real device re-test to confirm** — please rebuild, reinstall, and get all the way to the Home screen again.

### 2026-09-21 — Same crash recurred after the Glance fix: the Glance hypothesis was WRONG. Real root cause found: confirmed Google bug, not our dependency graph

**What happened:** rebuilt with Glance disabled, reinstalled, got past onboarding (Religion → Language → Alarm setup all working now) — then hit the **exact same** `NoSuchMethodError` on `KeyframesSpecConfig.at()` on reaching the Home screen. Identical stack trace, identical crash point.

**This disproves the previous entry's hypothesis.** Disabling Glance did not fix it, which means Glance was never the cause — worth stating plainly rather than quietly moving past it. The earlier fix wasn't wrong to *attempt* (it was well-reasoned circumstantial evidence at the time, and removing an unused dependency was low-risk either way), but it wasn't the actual root cause, and I said so explicitly at the time precisely so this could be caught and corrected here.

**Actual root cause — this time confirmed, not inferred:** found the exact same crash reported against **Google's own official JetNews sample app**, plus multiple unrelated third-party apps, all with the identical stack trace. It traces to a real, acknowledged bug on Google's issue tracker ([issuetracker.google.com/issues/322214617](https://issuetracker.google.com/issues/322214617)): a **binary compatibility break** in `KeyframesSpecConfig.at()`/`atFraction()` shipped in the `androidx.compose.animation` release that our `compose-bom:2024.01.00` pinned. Google's own fix commit (merged Jan 25, 2024, days after our pinned BOM was cut) states directly: *"Fix binary compatibility of KeyframesSpec... makes sure the signature of `at` and `atFraction` of KeyframesSpecConfig is binary compatible with previous releases."* This has nothing to do with any dependency in our project fighting the BOM — it's a bug **inside that specific BOM release itself**, and it explains why removing Glance changed nothing: there was no version conflict to remove.

**Fix applied:** bumped `compose-bom` from `2024.01.00` → `2024.04.01` in `app/build.gradle.kts` — safely past Google's fix, still within the same "Compose 1.6.x" runtime family as our pinned `kotlinCompilerExtensionVersion = "1.5.8"` / Kotlin 1.9.22, so no compiler or Kotlin version bump needed alongside it (lower risk than jumping further forward). Left Glance still disabled for this round on purpose — re-enabling it at the same time as the BOM bump would muddy verification of whether the BOM bump alone actually fixes this. Its build.gradle.kts comment now correctly says it's safe to re-enable in Sprint 9, with the wrong original theory called out rather than silently erased.

**Status:** ✅ Root-caused against a real, external, confirmed source (Google's issue tracker) rather than inference from our own dependency list — this is a materially higher confidence level than the previous attempt. **Not yet re-verified** — next device test is the real confirmation, same as always. If this crash recurs a third time with the identical trace, the BOM-bug theory would also need to be reconsidered, but that's now a low-probability outcome given the direct match to a documented, fixed Google bug.

### 2026-09-21 — App confirmed working end-to-end on device. Closed the Sprint 2 gap: onboarding now actually persists and drives Home

**Confirmed by you:** the compose-bom fix worked — full onboarding flow (Religion → Language → Alarm → Notifications) completes without crashing, Home screen loads. This closes out the crash-debugging arc from the last several sessions.

**What this session actually built** (real feature work, not a bug fix):

1. **`data/local/datastore/UserPreferences.kt` (new).** DataStore-backed persistence for `religionId`, `languageCode`, `onboardingCompleted`, `installEpochDay` (anchors the day-number algorithm), `notificationsGranted`. This is the piece that was completely missing before — every onboarding selection was previously discarded the moment the user tapped "Continue."

2. **`util/Religions.kt` (new).** Single source of truth for the 7 religions, their taglines, supported languages, and (where one exists) their content JSON filename — replaces the hardcoded religion list that used to live directly inside `ReligionSelectScreen.kt` with no connection to the rest of the app.

3. **`ui/onboarding/OnboardingViewModel.kt` (new).** A single `@HiltViewModel` shared across all 5 onboarding screens (scoped to a nested Navigation Compose sub-graph's back stack entry — the standard pattern for this exact situation). Holds in-progress selections as the user moves screen to screen, and on completion: persists everything, stamps the install date exactly once (idempotent — never overwrites a returning user's original install date), and **calls `ContentMigrationManager.migrateIfNeeded()` for the selected religion** — this Sprint-1-built class existed but was never actually invoked anywhere until now.

4. **`ui/MainViewModel.kt` (new) + `MainActivity.kt` (updated).** Resolves `onboardingCompleted` from DataStore and uses it to gate the splash screen via `setKeepOnScreenCondition` — the idiomatic `core-splashscreen` pattern for "don't render anything until we know where to start." A returning user now lands directly on Home; a new user still sees Welcome. Previously the app *always* started at Welcome regardless of prior completion.

5. **`ui/navigation/NavGraph.kt` (rewritten).** Onboarding screens moved into a nested `navigation(route = "onboarding") { ... }` graph specifically so they can share one `OnboardingViewModel` instance via `hiltViewModel(parentBackStackEntry)`. This is the standard, documented way to share state across a multi-screen flow in Navigation Compose — avoids threading every selection through navigation arguments by hand.

6. **`ui/home/HomeViewModel.kt` + `HomeScreen.kt` (updated).** `load()` now reads the real `religionId` and install date from `UserPreferences` instead of the `religionId = 1, LocalDate.now()` placeholders that were hardcoded in `HomeScreen.kt` since Sprint 1. Home screen's greeting now shows the actual selected religion's name, and the empty-state message is explicit about *why* most religions show no verse yet (only Hinduism has sample content loaded — this is expected, not a bug, until real content is authored for the other 6).

**Known, explicitly-flagged gap left for later:** the Permission screen (S06) records the user's *stated* preference ("Allow Notifications" vs "Maybe Later") to DataStore, but doesn't yet trigger Android's real `POST_NOTIFICATIONS` runtime permission dialog (API 33+) — that needs an `ActivityResultContracts.RequestPermission` launcher wired from `MainActivity`, deliberately scoped out of this pass to keep it focused. Flagged inline in `PermissionScreen.kt` and in the Sprint 2 table above, not silently skipped.

**Verification performed:** full brace/paren balance sweep across every new and modified file (all clean), cross-checked every new call site against the actual target function signatures (`AlarmScheduler.canScheduleExactAlarms()`, `ContentMigrationManager.migrateIfNeeded()`, `hiltViewModel(NavBackStackEntry)`) rather than assuming they'd match, and grepped for any stale references to the old `HomeViewModel.load(religionId, installDate)` signature (none found). **Not yet compiled** — same as every previous round, the real test is your next `./gradlew assembleDebug` + device install.

**Status:** 🟡 Substantial new feature code, carefully self-reviewed, not yet build-verified. This is a bigger single change than the recent one-file bug fixes — if the build fails, the error will likely point at one specific file rather than being a systemic issue, given how contained each piece is.

### 2026-09-21 — Crashed on every launch: nested navigation graph start destination bug

**Symptom:** app crashed immediately on open, before any UI rendered — happened on every single launch attempt (3 in a row in the log), 100% reproducible, not intermittent:
```
FATAL EXCEPTION: main
java.lang.IllegalArgumentException: navigation destination onboarding/welcome is not a direct child of this NavGraph
    at androidx.navigation.NavGraphNavigator.navigate(...)
    at androidx.navigation.compose.NavHostKt.NavHost(...)
    at com.dailydivine.app.ui.navigation.NavGraphKt.DailyDivineNavGraph(NavGraph.kt:24)
```

**Root cause:** a genuine bug in the previous session's nested-navigation-graph work, caught immediately by the first real test — exactly the value of testing every change on a device rather than assuming compile-clean means correct. The 5 onboarding screens were (correctly) nested inside their own sub-graph (route `"onboarding"`) so they could share one `OnboardingViewModel`. But `Screen.Welcome.route` (`"onboarding/welcome"`) is a child of *that sub-graph*, not of the *root* graph — and `NavHost`'s `startDestination` parameter must always be a direct child of the graph it's building. Both `NavGraph.kt`'s default parameter and `MainViewModel.kt`'s "not yet onboarded" branch were passing `Screen.Welcome.route` straight to the root `NavHost`, which can't find it there and throws immediately, before any composable ever renders — explaining why it crashed before even reaching Welcome, and why every retry hit the same wall.

**Fix applied:**
1. Added `Screen.OnboardingGraph` (route `"onboarding"`) to `Screen.kt` as the single source of truth for the sub-graph's own route, with an explanatory comment on exactly this failure mode so it can't quietly regress again.
2. `NavGraph.kt`: root `NavHost`'s default `startDestination` now correctly points at `Screen.OnboardingGraph.route` (which Navigation then automatically descends into that sub-graph's own `startDestination`, `Screen.Welcome.route` — a separate, correct usage that legitimately stays as-is). Also removed the private, duplicated `ONBOARDING_GRAPH_ROUTE` string constant in favor of the single shared `Screen.OnboardingGraph.route`, since a second hardcoded copy of that string is exactly how this class of bug could resurface later.
3. `MainViewModel.kt`: the "onboarding not completed" branch now also resolves to `Screen.OnboardingGraph.route` instead of `Screen.Welcome.route`, for the identical reason.

**Verification:** grepped for any remaining problematic usage of `Screen.Welcome.route` as a start-destination argument (only the one *correct* usage remains — the sub-graph's own internal `startDestination`, which is right by design) and re-ran the full brace/paren balance sweep across every file (clean).

**Status:** ✅ Fixed and the specific mechanism is well understood (not a guess) — this was a straightforward, well-documented Navigation Compose API contract violation, not a mysterious runtime issue. **Not yet re-verified on device.**

### 2026-09-21 — Confirmed working end-to-end. Continued development: made the alarm system actually functional

**Confirmed by you:** full onboarding-to-Home flow works cleanly now. Moving on to real feature work per the checklist's "What's Next."

**The gap closed this session:** the app's entire core feature — the prayer alarm — was completely inert. `AlarmScheduler`, `AlarmEscalationController`, and `AlarmService` all existed and were individually correct (the v1.1 fixes), but **nothing in the UI ever actually created a real alarm.** Tapping "Set Alarm →" during onboarding just navigated to the next screen with no side effect at all — same as tapping "Skip, I'll set up later," since both buttons called the identical navigation lambda. And even if an alarm *had* been scheduled, there was no `AlarmRingActivity` for it to launch when it fired — the manifest declared the activity, but the class didn't exist.

**What this session built:**

1. **`data/repository/AlarmRepository.kt` (new).** Wraps `AlarmDao` together with `AlarmScheduler` so every create/update/delete of an alarm both persists to Room *and* schedules/cancels the real system alarm in one call — previously these two pieces existed independently with nothing connecting them. Also exposes `rescheduleAllEnabled()` for boot recovery.

2. **`OnboardingViewModel.kt` (updated).** Added a `wantsAlarm: Boolean` field distinguishing "Set Alarm →" (`confirmAlarm()`) from "Skip, I'll set up later" (`skipAlarm()`) — these were indistinguishable before. `completeOnboarding()` now actually calls `alarmRepository.createAndSchedule()` when the user didn't skip, using the TTS preference already collected on that screen.

3. **`NavGraph.kt` (updated).** Wired `AlarmSetupScreen`'s `onContinue`/`onSkip` callbacks to call `confirmAlarm()`/`skipAlarm()` before navigating, instead of both doing the exact same thing.

4. **`ui/alarm/AlarmRingActivity.kt` (new).** Screen S08 — the full-screen UI that appears when an alarm actually fires. Snooze (reschedules via `AlarmScheduler.scheduleSnooze()` using the alarm's own configured duration) and "Wake Up & Read" (opens `MainActivity`/Home) buttons, both of which first send `AlarmService.ACTION_STOP` so the escalation loop stops cleanly rather than continuing to ring in the background. Back button is absorbed (can't silently dismiss an alarm — matches F004-R14's intent). **Also implements the API 26 fallback** noted as a follow-up in an earlier session: `showWhenLocked`/`turnScreenOn` manifest attributes only apply on API 27+, so this activity additionally sets the older `WindowManager.LayoutParams` flags for exactly this project's `minSdk` floor (26).

5. **`AlarmService.kt` (updated).** Now handles an `ACTION_STOP` intent (stops the escalation loop and tone cleanly when sent by `AlarmRingActivity`) and, more importantly, **actually launches `AlarmRingActivity`** — previously the service played the tone and showed a notification but nothing ever displayed the ring screen. Uses the notification's `setFullScreenIntent()` mechanism specifically (not a direct `startActivity()` call from the service), since that's the officially correct approach for alarm/call-style interruptions — a direct `startActivity()` from a background service is subject to Android 10+'s background-activity-launch restrictions and can silently fail to show anything, which `setFullScreenIntent()` is specifically exempted from.

6. **`BootReceiver.kt` (rewritten).** Was a stub with a TODO comment and zero real logic. Now a real `@AndroidEntryPoint` `BroadcastReceiver` (the standard, documented pattern for injecting Hilt dependencies into a receiver, which isn't part of the normal Activity/Fragment Hilt graph) that calls `AlarmRepository.rescheduleAllEnabled()` on `ACTION_BOOT_COMPLETED`, using `goAsync()` to keep the process alive long enough for the coroutine to finish.

**A smaller thing worth noting:** while writing `AlarmRingActivity`, I initially reached for overriding the classic `onBackPressed()` method, then caught myself — given this exact toolchain's history of hard-failing on deprecated/experimental APIs (the `Card(onClick=...)` opt-in issue from an earlier session), I switched to the modern `onBackPressedDispatcher.addCallback()` API instead, before it became a problem rather than after.

**Explicitly scoped out, not silently skipped** (all listed in the Sprint 4 table above): the verse preview on the ring screen shows a generic greeting instead of today's actual verse text; the alarm always fires at the PRD's default 5:30 AM since `AlarmSetupScreen` still has no interactive time picker (a real `TimePicker` is experimental in this Compose BOM, same class of issue as the `Card` fix); and there's still no Alarm screen (S11) to view, edit, or delete the alarm afterward.

**Verification performed:** full brace/paren balance sweep across every new and modified file (all clean), and — learning from earlier sessions where I stated call signatures from memory without checking — explicitly grepped each new cross-file call site against its actual target function signature side-by-side (`AlarmRepository.createAndSchedule()`, `AlarmScheduler.scheduleSnooze()`, `AlarmService.ACTION_STOP`) rather than assuming they'd match. **Not yet compiled or device-tested** — same as every substantial round, that's the next real checkpoint.

**Status:** 🟡 The largest single feature addition so far, carefully self-reviewed and cross-checked, but unverified against a real build. Given how many distinct pieces this touches (new repository, rewritten service, new activity, rewritten receiver, updated ViewModel and nav graph), a build failure here is more likely to be a genuine typo/import miss than in recent single-file rounds — worth budgeting for at least one fix-and-retest cycle.

---

## What's Next (as of this session)

1. **Build + device-test this session's alarm system work.** Same drill: rebuild, reinstall, complete onboarding with "Set Alarm →" (not Skip), wait for 5:30 AM or — more practically for testing — temporarily change the device clock forward, or add a quick debug-only "trigger test alarm now" button before removing it again. Confirm `AlarmRingActivity` actually appears, Snooze reschedules, and "Wake Up & Read" opens Home.
2. If it crashes or fails to build: same process — `adb logcat --uid=<uid>`, paste the trace.
3. If it works: next real gaps, in rough priority order —
   - Alarm configuration screen (S11) — right now the only way to create an alarm is once, during onboarding; there's no way to view, edit, disable, or delete it afterward, or add a second one.
   - Real `TimePicker` in `AlarmSetupScreen` so the alarm isn't hardcoded to 5:30 AM.
   - Verse preview on the alarm ring screen.
   - The real `POST_NOTIFICATIONS` runtime permission dialog (still just records intent, doesn't trigger the system prompt).
   - Re-enable `androidx.glance` whenever Sprint 9's widget work actually starts; confirmed safe, no rush.
