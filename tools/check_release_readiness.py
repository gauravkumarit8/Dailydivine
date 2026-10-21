#!/usr/bin/env python3
"""Pre-release gate, run by the release workflow (and runnable locally). Fails (exit 1) on anything that
would get the app rejected by Google Play or break the privacy promises made in the policy.
Usage (repo root): python3 tools/check_release_readiness.py"""
import os, re, sys, glob
ROOT = os.path.join(os.path.dirname(__file__), '..')
def read(*p): return open(os.path.join(ROOT, *p), encoding='utf-8').read()
problems = []
def need(cond, msg):
    if not cond: problems.append(msg)

# 1. build.gradle.kts
g = read('app', 'build.gradle.kts')
live = '\n'.join(l for l in g.splitlines() if not l.strip().startswith('//'))
m = re.search(r'targetSdk\s*=\s*(\d+)', live); need(m and int(m.group(1)) >= 36, 'targetSdk must be 36 or higher (Google Play requirement since 2026-08-31)')
need(re.search(r'isMinifyEnabled\s*=\s*true', live), 'release build must enable R8 (isMinifyEnabled = true)')
need('proguardFiles' in live, 'release build must reference proguard-rules.pro')
for bad in ('firebase', 'billing', 'play-services-ads', 'user-messaging-platform', 'media3', 'coil', 'lottie'):
    need(bad not in live.lower().replace('implementation(platform', ''), f'dependency "{bad}" must not be active in the first release (see build.gradle.kts note)')
need('appVersionCode' in g and 'appVersionName' in g, 'versionCode/versionName must come from -PappVersionCode/-PappVersionName')

# 2. proguard keep rule that protects the Gson content parsing
pg = read('app', 'proguard-rules.pro')
need('data.content' in pg and '-keep' in pg, 'proguard-rules.pro must keep com.dailydivine.app.data.content.** (Gson reflection)')

# 3. manifest
mf = read('app', 'src', 'main', 'AndroidManifest.xml')
perms = set(re.findall(r'<uses-permission[^>]*android:name="([^"]+)"', mf))
allowed = {'android.permission.' + x for x in ('SCHEDULE_EXACT_ALARM', 'USE_EXACT_ALARM', 'RECEIVE_BOOT_COMPLETED', 'WAKE_LOCK', 'FOREGROUND_SERVICE',
           'FOREGROUND_SERVICE_MEDIA_PLAYBACK', 'POST_NOTIFICATIONS', 'USE_FULL_SCREEN_INTENT', 'VIBRATE', 'SET_WALLPAPER')}
need(perms <= allowed, f'unexpected permissions in the manifest (update the privacy policy and Data safety answers first): {sorted(perms - allowed)}')
need('android.permission.INTERNET' not in perms, 'INTERNET must not be declared (the privacy policy says the app has no internet access)')
need('android:allowBackup="false"' in mf, 'allowBackup must be false (the privacy policy says backups are off)')

# 4. icons and no leftover placeholder bitmaps
res = os.path.join(ROOT, 'app', 'src', 'main', 'res')
for f in ('mipmap-anydpi-v26/ic_launcher.xml', 'mipmap-anydpi-v26/ic_launcher_round.xml', 'drawable/ic_launcher_foreground.xml', 'drawable/ic_launcher_background.xml', 'drawable/ic_launcher_monochrome.xml'):
    need(os.path.exists(os.path.join(res, f)), f'missing launcher icon file {f}')
need(not glob.glob(os.path.join(res, 'mipmap-*dpi', 'ic_launcher*.png')), 'old placeholder launcher PNGs are still present')

# 5. website + store assets
for f in ('index.html', 'privacy.html', 'terms.html', 'style.css', '.nojekyll', 'assets/icon-512.png'):
    need(os.path.exists(os.path.join(ROOT, 'docs', f)), f'missing docs/{f}')
for f in ('assets/icon-512.png', 'assets/feature-graphic-1024x500.png'):
    need(os.path.exists(os.path.join(ROOT, 'play-store', f)), f'missing play-store/{f}')
links = read('app', 'src', 'main', 'java', 'com', 'dailydivine', 'app', 'util', 'AppLinks.kt')
need('privacy.html' in links and 'terms.html' in links, 'AppLinks must point at privacy.html and terms.html')

# 6. placeholders that must be filled in before a public release
for pat in glob.glob(os.path.join(ROOT, 'docs', '*.html')) + glob.glob(os.path.join(ROOT, 'app', 'src', '**', '*.kt'), recursive=True):
    t = open(pat, encoding='utf-8').read()
    for token in ('REPLACE_ME_EMAIL', 'REPLACE_ME_DEVELOPER_NAME'):
        need(token not in t, f'{os.path.relpath(pat, ROOT)} still contains {token}: run tools/configure_release.py (play-store/RELEASE.md, step 1)')

if problems:
    print('RELEASE READINESS: FAILED')
    for p in problems: print(' -', p)
    sys.exit(1)
print('RELEASE READINESS: OK  (targetSdk>=36, R8 on, permissions as declared, no INTERNET, icons + site present, no placeholders)')
