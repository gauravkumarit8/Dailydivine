# DailyDivine: UI audit and revamp plan

Date: 2026-10-28. Code at HEAD `fc0e1e0` plus Rounds A, B and C (see CHECKLIST.md).

## 0. How this was done, and its limits

Static review of all Compose screens (about 4,600 lines under `ui/`), the theme, the manifest and the
resources, plus measurements by script. **No device, no emulator, no screenshots.** Nothing here has
been seen rendering. Every finding is graded: **Certain** (visible in code), **Likely** (follows from the
code, needs a device to confirm), **Check** (a question to settle on a device).
Strong claims of the form "production grade" cannot be made from code alone; section 6 defines the
evidence needed.

## Status (updated 2026-10-28, Round D)

Done in code, not yet seen on a device: P0 items 1 to 4 (ring screen, dark window, adaptive frame with width cap
and navigation rail, time picker), item 7 (Alarm rows collapsed), item 8 (day circles and tap targets), fixed
dialog and wallpaper-preview heights. Still open: Phase 0 tooling (previews, screenshot tests, string
extraction, design tokens), Phase 2 screen redesigns, items 9 to 17.

## 1. Scorecard (code-level, per screen)

| Screen | Small phone / large font | Tablet / landscape | Dark mode | A11y basics | Verdict |
|---|---|---|---|---|---|
| Splash | n/a | n/a | **Fails** (white background hard-coded) | n/a | Fix |
| Onboarding (5) | Good: scaffold scrolls, buttons pinned | Stretches full width | OK | Good (step, radio roles, grouped rows) | Polish |
| Home | Scrolls; long verses fine | Stretches full width | OK | Good (labelled actions) | Redesign candidate |
| Alarm tab | Scrolls; **rows are now very tall** | Stretches | OK | Mixed (40 dp day circles) | Redesign |
| Ring screen | **No scroll; can clip** | **Likely clips in landscape** | OK (own gradient) | Good contrast work | **Fix first** |
| Library (browse, search, favourites, history) | OK | Stretches | OK | Check | Polish |
| Badges | OK | Stretches | OK | Reduce-motion done | Polish |
| Wallpaper settings | Scrolls; fixed 380 dp preview | Stretches | OK | Check | Polish |
| Settings | Scrolls | Stretches | OK | Good (toggle rows) | Polish |
| Dialogs (time picker, tones, religion) | **Time picker not scroll-safe** | Likely clips | OK | Check | Fix |

## 2. What is already good (keep it)

- `OnboardingScaffold`: scrolling content with pinned actions is the right pattern; reuse it.
- Contrast engineering (`Contrast.kt`): brand colours nudged to 4.5:1, dark mode lightened.
- TalkBack work: merged semantics, radio/switch roles, step indicator read as one item.
- Reduce Motion is honoured (confetti, count-up, pulse).
- Edge-to-edge is handled once, in the `Scaffold`, so screens do not each deal with bars.
- Religion palettes give the app identity without a custom component set.

## 3. Findings

### P0: breaks real use on some phones

1. **Ring screen cannot scroll (Certain), and likely clips (Likely).** One `Column`, centred, 64 sp clock,
   up to several lines of verse, two buttons, 48 dp vertical padding. On a 360x640 dp phone at 130 % font,
   or any phone in landscape (about 360 dp tall), the buttons can fall off-screen. It is the screen the
   user sees half asleep and the only way to stop the alarm. Note also: `AlarmRingActivity` is
   `singleInstance` and not locked to portrait.
2. **Dark mode flashes white (Certain).** The splash background is `@android:color/white`, the window
   theme is `android:Theme.Material.Light.NoActionBar` and there is no `values-night`. Dark-mode users get
   a white flash at launch, and a light window behind the app during rotation and transitions.
3. **No adaptive layout at all (Certain).** No window size classes, no max content width, no navigation
   rail, and the manifest does not restrict orientation. On tablets, foldables and landscape, cards and
   verse text stretch edge to edge, and a bottom bar spans a 10-inch screen. Android 16 ignores
   orientation and resizability restrictions on large screens for apps targeting 36, so tablets and
   foldables will run this app at full size (re-check the current Android docs when implementing).
4. **Time picker dialog (Likely).** Material's dial picker inside an `AlertDialog` is about 256 dp tall and
   its `text` slot does not scroll. In landscape or at large font the OK button can be clipped. It also
   hard-codes `is24Hour = false`, ignoring the phone's 12/24-hour setting.

### P1: quality and maintainability gaps

5. **No string resources (Certain).** About 99 hard-coded `Text("...")` literals and only the app name in
   `strings.xml`. Localisation (a stated goal) and consistent copy are blocked.
6. **No `@Preview`s and no screenshot tests (Certain).** Design cannot be iterated or regression-tested.
7. **Alarm row overload (Certain, caused by Round B).** Each alarm now shows time, switch, days, tone,
   read-aloud, volume slider, two switches, snooze buttons, change time and delete. With several alarms the
   list is very long. Advanced settings belong in an expandable section or an edit sheet.
8. **Touch targets (Likely).** Day circles are 40 dp, below the 48 dp guideline unless the click area is
   padded (Check). Small tap areas matter on a sleepy morning.
9. **State coverage is uneven.** Library has empty notes; Home has only a spinner; there is no error
   state anywhere (a failed verse load shows "coming soon"). Delete has no undo.
10. **Long text risks.** Only 5 uses of `maxLines`/`overflow` in the whole UI. Long translations, large
    fonts and long alarm labels are untested.
11. **Keyboard inset on Home (Check).** `imePadding()` is used inside a `Scaffold` with a bottom bar; on
    some versions this can double-pad or leave a gap.
12. **RTL is declared (`supportsRtl=true`) but untested.** Hebrew, Arabic and Urdu are in the language
    list; icons that imply direction (back arrow, progress) need mirroring review.

### P2: polish

13. Generic system notification icons; no custom monochrome status icons.
14. One typeface (Poppins) for everything; the verse style exists, but the hierarchy is flat.
15. No haptics on key actions (bookmark, alarm toggle).
16. No Material You (dynamic colour) option; religion palettes are the identity, so this is optional.
17. Verse cards, streak card and reflection card all use the same elevated-card style, so nothing leads.

## 4. Revamp plan

Principle: fix what blocks use first (Phase 1), build the tooling that makes redesign safe (Phase 0 runs
alongside), then redesign screens in order of how often the user sees them. Estimates are rough, for one
developer with this assistant, excluding waiting for CI.

### Phase 0: foundations (about 3 days)

- **Design tokens** in `ui/theme`: spacing scale (4, 8, 12, 16, 24, 32), radii, elevation, plus a small
  set of semantic roles (verse text, caption, success, warning).
- **Strings:** extract every literal into `strings.xml` (plurals for "min", "days"). Mechanical, safe.
- **Previews:** one annotation (`@DevicePreviews`) covering phone, small phone (320 dp), tablet, font
  scale 1.0/1.3/2.0, light/dark. Every screen gets previews with fake state.
- **Screenshot tests in CI** (Paparazzi or Roborazzi, JVM-only, no emulator): golden images for each
  screen across the preview matrix; CI fails on unexpected pixel changes. This is the "will it work on all
  screens" proof the project lacks.
- **Adaptive primitives:** `ContentWidth` (max about 600 dp, centred), `AdaptiveScaffold`
  (NavigationBar on compact, NavigationRail on medium/expanded), based on window size class.

### Phase 1: P0 fixes (about 4 days)

- **Ring screen v2:** scrollable and adaptive: compact portrait stacks; landscape splits into time/verse on
  one side, actions on the other. Large, well-spaced targets. Swipe-to-dismiss (PRD R20) as an
  alternative to buttons. Closes itself when dismissed from the notification. Verified with screenshots at
  320x568, 360x640, 411x891, landscape and font scale 2.0.
- **Dark window and splash:** `values-night/themes.xml`, dark splash background, window background from
  the theme, so no white flash.
- **Time picker:** respect `DateFormat.is24HourFormat`; use `TimeInput` (keyboard entry) when height is
  short; make the dialog content scrollable.
- **Adaptive pass** over all screens using Phase 0 primitives (content width, rail).

### Phase 2: screen redesigns, in this order (about 10 days)

1. **Home:** the verse is the hero: larger serif type, generous spacing, source line, one primary action
   row (Listen, Save, Share), streak as a compact chip, reflection collapsed to a prompt that expands.
   Clear empty, loading and error states.
2. **Alarm tab:** compact rows (time, label, days, switch). Tap opens a bottom sheet or screen for sound,
   volume, vibration, snooze and read-aloud. Undo snackbar on delete. 48 dp day targets.
3. **Onboarding:** keep the structure; add a small illustration per step, better "Coming soon" treatment,
   and a short permission rationale before each system prompt.
4. **Library:** clearer tabs, search with filters (category), history as a calendar strip.
5. **Settings:** grouped sections with icons, a short "Reliability" help page (battery, exact alarms,
   full-screen alerts) because that is where most alarm-app support questions come from.
6. **Wallpaper, Badges:** consistent card system and spacing; wallpaper preview sized by available width,
   not a fixed 380 dp.

### Phase 3: reach and delight (about 5 days, optional before launch)

- RTL review and mirrored icons; tablet and foldable polish (two-pane Library).
- Custom monochrome notification icons; haptics; subtle motion (respecting Reduce Motion).
- Optional dynamic colour; app-icon variants.

## 5. Design decisions I need from you

1. Visual direction: keep the current warm, card-based look and refine it, or a bolder redesign
   (larger type, fewer cards, more whitespace)? My recommendation: refine, then simplify Home and Alarm.
2. Is landscape/tablet important for launch, or "works and looks acceptable" enough?
3. Do you have any brand assets (logo, colours, typeface) beyond the current ones?

## 6. Definition of "production grade" (the evidence to collect)

A screen is done when all of these are true and recorded in CHECKLIST.md:

- Screenshot tests pass for: 320x568 dp, 360x640, 411x891, 600 dp tablet, landscape; font scale 1.0, 1.3 and
  2.0; light and dark.
- No content clipped or unreachable at font scale 2.0 and display size "largest".
- TalkBack pass: every control labelled, sensible order, no unlabeled icons; targets >= 48 dp.
- Contrast >= 4.5:1 for text, 3:1 for icons and borders (checked by the existing contrast tests).
- Empty, loading and error states exist and are reachable.
- Reviewed on at least: one small phone, one large phone, one tablet or foldable, and one aggressive OEM
  skin (Xiaomi/Oppo/Samsung), Android 8 to 16.
- Google Play **pre-launch report** reviewed (free; runs the build on real devices and returns
  screenshots, crashes and accessibility findings), no code changes needed to use it.
