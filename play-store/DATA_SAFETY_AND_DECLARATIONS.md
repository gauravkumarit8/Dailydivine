# Data safety form and permission declarations

These answers are true for the app as built. **Re-check them (and the privacy policy) before any release
that adds ads, analytics, billing, accounts, location or the INTERNET permission.** The release workflow
fails if INTERNET, advertising ID or Billing permissions appear in the merged manifest.

## Data safety form (Policy > App content > Data safety)
Google's definition: data is "collected" when it is transmitted off the user's device. DailyDivine never
transmits anything (no INTERNET permission), so:

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Data types (all categories: personal info, health, location, messages, photos, files, calendar, contacts, app activity, web history, device IDs...) | none collected, none shared |
| Is all of the user data collected by your app encrypted in transit? | Not applicable (nothing is transmitted) |
| Do you provide a way for users to request that their data be deleted? | Not applicable (no data is held by you). In-app: Settings > Clear all data. |
| Security practices | Data is stored locally on the device only |

Notes for accuracy:
- The user's religion choice, journal text and wallpaper photo are processed **only on the device**; they are
  never sent to you or anyone else, so they are not "collected" under Google's definition.
- Text-to-speech is performed by the phone's own engine. That is the user's device software, not a service the
  app sends data to (the privacy policy explains it anyway).

## Declarations Play Console will ask for

### 1. Exact alarms (permission `USE_EXACT_ALARM`, plus `SCHEDULE_EXACT_ALARM` on Android 12)
Play Console: Policy > App content > Exact alarm permission.
- **Core functionality:** Alarm clock. The app's main feature is a user-set morning alarm that must ring at the chosen time.
- Suggested text: "DailyDivine is an alarm clock app. The user sets a daily wake-up time (with optional weekday repeats) and the app
  rings at exactly that time using AlarmManager.setAlarmClock. Without exact timing the core feature does not work."

### 2. Full-screen intent (`USE_FULL_SCREEN_INTENT`)
Play Console: Policy > App content > Full screen intent (shown because the app targets Android 14+).
- **Use case:** Alarm. The alarm ringing screen (Snooze / Wake up & read) is shown over the lock screen when the alarm fires.
- Suggested text: "When the user's alarm rings, a full-screen activity shows the verse with Snooze and Wake up buttons over the
  lock screen, like a standard alarm clock."

### 3. Foreground service (`FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`)
Play Console: Policy > App content > Foreground service permissions. Type: **Media playback**.
- Suggested text: "While an alarm is ringing, a foreground service (type mediaPlayback) plays the alarm tone and shows an ongoing
  notification so the sound keeps playing until the user snoozes or dismisses it."
- Play asks for a short **video** showing the feature: record the phone while an alarm rings (set one for 1 minute ahead), showing the notification, the
  full-screen alarm screen, and tapping Snooze or Wake up. 20 to 30 seconds is enough.

### 4. Other permissions (no form, but be ready to explain)
| Permission | Why |
|---|---|
| `POST_NOTIFICATIONS` | daily verse, streak reminder, milestone notifications (asked at onboarding, can be declined) |
| `RECEIVE_BOOT_COMPLETED` | re-arm alarms and daily jobs after a restart |
| `WAKE_LOCK`, `VIBRATE` | wake the screen and vibrate when the alarm rings |
| `SET_WALLPAPER` | only for the optional Daily Wallpaper feature the user turns on |

Added automatically by libraries (reviewed, harmless, checked by `tools/check_merged_manifest.py`):
| Permission | Source | Effect |
|---|---|---|
| `ACCESS_NETWORK_STATE` | WorkManager | lets the app *see* whether the phone is online; it cannot send or receive data, so "no data collected" stays true |
| `<package>.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | AndroidX Core | private signature permission that protects the app's own broadcast receivers |

Deliberately not requested: INTERNET, location, camera, microphone, contacts, storage, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
(the battery tips open the system battery list, which needs no permission).

## Wallpaper feature: disclosure
The store description and privacy policy both state that the feature replaces the wallpaper once a day and cannot restore the old one.
Keep that wording if you edit the listing.
