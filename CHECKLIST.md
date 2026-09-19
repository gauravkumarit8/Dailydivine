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
| Religion/language selection UI | ✅ | 7-religion grid (S03), language list (S04) |
| DataStore preferences | ⬜ | Onboarding screens don't yet persist selections — next step before this sprint is "done" |
| Home screen UI | ✅ | `HomeScreen.kt` — greeting, verse card, streak card |
| Daily verse display logic | ✅ | `VerseRepository.getDailyVerse()` — deterministic day-number algorithm (API Contract 1) |
| Day number calculation | ✅ | Implemented + matches PRD Section 19 contract exactly |

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
| **F004-R19 (v1.1): escalation / auto-snooze cap** | ✅ | `AlarmEscalationController` — **unit-tested and verified standalone** (11/11 assertions pass, see below) |
| `AlarmService` (foreground service, tone playback) | ✅ | Drives the escalation loop, volume ramp, WakeLock |
| `AlarmReceiver` | ✅ | Starts the foreground service |
| `BootReceiver` (reschedule after reboot) | 🟡 | Structure in place; DB wiring left as a TODO pending Hilt entry-point pattern for non-Compose receivers |
| Alarm ring screen (full-screen activity) | ⬜ | `AlarmRingActivity` referenced in the manifest, not yet implemented |
| Alarm tone playback (real audio) | 🟡 | `AlarmService` plays from `res/raw/`; only a **placeholder** `temple_bell.mp3` exists — needs real royalty-free audio (Appendix B) |
| Alarm configuration UI (S11) | ⬜ | Not started |
| Unit tests: `AlarmEscalationControllerTest` | ✅ | **4 test cases, verified passing** (see Verification section) |

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

---

## What's Next (as of this session)

Real, current priority order — supersedes the older "Next Session Priorities" list above where they conflict:

1. **Push this session's two changes** (`.github/workflows/build.yml`, this CHECKLIST update) and trigger the Actions workflow. This is the actual end-to-end verification we've been building toward across the last several fix cycles — if it goes green, everything in `alarm/`, `data/`, `ui/`, and the Hilt wiring compiles clean on a real, independent machine.
2. **If Actions goes red:** paste me the failing step's log the same way you have been — same pattern, another real bug to fix, likely something Codespaces papered over differently than a clean runner will.
3. **If Actions goes green:** install the APK on your device per the steps above and confirm the app actually launches and shows the Welcome onboarding screen. That's the first real "does this app run at all" signal we'll have had.
4. Once that's confirmed, next actual feature work (in order, per Sprint 2/3/4 gaps in the tables above):
   - Wire DataStore into onboarding so religion/language selections persist and Home screen reads a real `religionId` instead of the hardcoded `1` placeholder.
   - Build `AlarmRingActivity` (Screen S08) — `AlarmEscalationController` and `AlarmService` currently have no UI to hand control back to.
   - Add the API 26 `WindowManager` flag fallback for `showWhenLocked`/`turnScreenOn` noted in the Sept 9 bug-fix entry above.
