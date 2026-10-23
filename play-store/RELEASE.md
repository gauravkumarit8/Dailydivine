# Publishing DailyDivine on Google Play: step by step

Do the steps in order. Anything marked **you** needs your accounts or decisions; everything else is already built.

## What is already done
- App targets **Android 16 (API 36)**, the Play requirement for new apps since 31 Aug 2026.
- Release builds use **R8** (smaller, with keep rules so verse loading still works) and are **signed from CI secrets**; the version comes from the workflow inputs.
- Unused libraries removed (Firebase, Play Billing, Media3, Coil, Lottie). No INTERNET permission. Backup off. This is why the Data safety answer is "no data collected".
- Real adaptive icon (with Android 13 themed icon), Play icon and feature graphic.
- Privacy policy, terms and landing page in `docs/` (GitHub Pages), linked from Settings > About.
- `release.yml` builds, signs, verifies (signature, manifest guardrails, bundletool) and can upload to Play.

## Timeline reality check
A **personal** developer account created after 13 Nov 2023 must run a **closed test with at least 12 testers for 14 continuous days** before Play grants production access. Internal testing does not count. Plan for roughly 3 to 4 weeks from first upload to launch. (Rules change; confirm in Play Console under Release > Production.) Organisation accounts have different requirements.

## 1. Fill in your details (you)
Edit nothing by hand. Run, from the repo root:
```powershell
python tools/configure_release.py --email "you@example.com" --name "Your Name or Company"
```
No Python? Use PowerShell:
```powershell
$email = "you@example.com"; $name = "Your Name or Company"
Get-ChildItem docs\*.html, app\src\main\java\com\dailydivine\app\util\AppLinks.kt | ForEach-Object {
  $t = Get-Content $_.FullName -Raw -Encoding UTF8
  $t = $t -replace 'REPLACE_ME_EMAIL', $email -replace 'REPLACE_ME_DEVELOPER_NAME', $name
  [IO.File]::WriteAllText($_.FullName, $t, (New-Object Text.UTF8Encoding $false))
}
```
Use the same name as on your Play developer account. Commit and push.

## 2. Create the upload key and the GitHub secrets (you)
```powershell
keytool -genkeypair -v -keystore dailydivine-upload.jks -alias dailydivine -keyalg RSA -keysize 2048 -validity 10000
```
Keep the `.jks` file **outside the repo** (it is git-ignored anyway) and **back it up in two places** with its passwords.
Google's Play App Signing holds the real app-signing key; this is only your *upload* key, and Play support can reset it if lost, but it takes time.

Copy the keystore as Base64 to the clipboard:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("dailydivine-upload.jks")) | Set-Clipboard
```
Repo **Settings > Secrets and variables > Actions > New repository secret**:
| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | the Base64 text you just copied |
| `ANDROID_KEYSTORE_PASSWORD` | the keystore password |
| `ANDROID_KEY_ALIAS` | `dailydivine` |
| `ANDROID_KEY_PASSWORD` | the key password (same as the keystore password if you pressed Enter) |
| `PLAY_SERVICE_ACCOUNT_JSON` | *optional*, only for automatic upload (step 9) |

## 3. Publish the website (you)
The `docs/` folder is the website. It is hosted at https://grvthakur.bid/daily-divine/ (upload the contents of `docs/` there after
running step 1, so the pages carry your name and email). Open https://grvthakur.bid/daily-divine/privacy.html and check it loads;
that URL goes into Play Console. (The "Deploy website" workflow is for GitHub Pages and is manual-only; ignore it if you host elsewhere.)
Whenever you change a page, upload it again, and keep the in-app link (`AppLinks.SITE_URL`) pointing at the same place.

## 4. Make sure CI is green
Run **Actions > Android CI**. It must pass (unit tests and the debug APK) before you release.

## 5. Create the app in Play Console (you)
Play Console > Create app: name `DailyDivine: Verse & Alarm`, English (US), App, Free, accept the declarations.
Then Dashboard > "Set up your app": fill every task using `play-store/LISTING.md` and `play-store/DATA_SAFETY_AND_DECLARATIONS.md`.
Upload `play-store/assets/icon-512.png`, the feature graphic and **your own phone screenshots**.

## 6. Build the signed bundle
Actions > **Release AAB (Google Play)** > Run workflow:
- `version_name`: `1.0.0`
- `version_code`: `1` (**never 0**: Android rejects it; must be higher for every later upload: 2, 3, ...)

The workflow stops with a clear message if a placeholder, a forbidden permission, a wrong targetSdk or a missing secret is found.
Download the artifact `DailyDivine-release-aab` (the `.aab`, `mapping.txt` and the merged manifest).

## 7. First upload (you)
Google requires the **first** release of a new app to be uploaded by hand: Play Console > Testing > **Internal testing** > Create new release > upload the `.aab`.
Accept **Play App Signing** when asked. Add yourself as a tester, install from the opt-in link, and test on a real phone:
alarm rings on time with the screen off, "Wake up & read" speaks the verse, wallpaper updates, notifications arrive, Settings > About links open.

## 8. Closed test, then production (you)
Release > Testing > **Closed testing**: create a track, add at least 12 testers by email (a Google Group works), roll out, and keep them opted in for **14 days**. Then Release > Production > "Apply for access", answer the questionnaire, and create the production release (a staged rollout of 10 to 20% is sensible).

## 9. Optional: automatic uploads later
Create a Google Cloud service account, enable the Google Play Android Developer API, invite the service account in Play Console (Users and permissions) with release permissions, store its JSON key in the `PLAY_SERVICE_ACCOUNT_JSON` secret, then tick **upload_to_play** when running the workflow. Uploads go to Internal testing as a draft.

## 10. After you publish
- Put the live store URL on the website: `python tools/configure_release.py --play-url "https://play.google.com/store/apps/details?id=com.dailydivine.app"`
- Watch Play Console > Quality > Android vitals and crashes (the embedded R8 mapping de-obfuscates stack traces).
- Every update: bump `version_code`, rerun the workflow, upload.
- Before adding ads, analytics, billing, accounts, location or INTERNET: update the privacy policy, the Data safety form and `tools/check_release_readiness.py` first, **then** the code.

## Known open items
- Judaism and Sikhism content, and more languages, are not built yet.
- The Hindu, Christian, Islamic and Buddhist passage selections should be reviewed by a knowledgeable reader of each tradition.
- The toolchain (AGP 8.2, Kotlin 1.9, compileSdk 34) is older than Google's current recommendation. It works for targetSdk 36 today; plan a tested upgrade after launch.
- Have the privacy policy and terms reviewed by someone qualified in your country. They describe the app accurately but are not legal advice.
