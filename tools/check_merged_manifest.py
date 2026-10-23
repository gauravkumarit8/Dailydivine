#!/usr/bin/env python3
"""Release guardrail on the MERGED release manifest (what Google Play actually sees, including
permissions that libraries add on their own).
  python3 tools/check_merged_manifest.py <merged AndroidManifest.xml> <applicationId>
Fails if: a permission that breaks the privacy policy/Data safety answers is present (INTERNET,
advertising ID, Billing, battery-optimization request), a permission nobody has reviewed appears
(allow-list), or targetSdkVersion is not 36."""
import re, sys
path, app_id = sys.argv[1], sys.argv[2]
mf = open(path, encoding='utf-8').read()
perms = set(re.findall(r'<uses-permission[^>]*?android:name="([^"]+)"', mf))

OWN = {'android.permission.' + x for x in (
    'SCHEDULE_EXACT_ALARM', 'USE_EXACT_ALARM', 'RECEIVE_BOOT_COMPLETED', 'WAKE_LOCK', 'FOREGROUND_SERVICE',
    'FOREGROUND_SERVICE_MEDIA_PLAYBACK', 'POST_NOTIFICATIONS', 'USE_FULL_SCREEN_INTENT', 'VIBRATE', 'SET_WALLPAPER')}
# Added automatically by libraries we use. Both are harmless and are described in the privacy policy / docs:
LIBRARY = {
    'android.permission.ACCESS_NETWORK_STATE': 'added by WorkManager: lets the app SEE whether the phone is online; it cannot send or receive data',
    f'{app_id}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION': 'added by AndroidX Core to protect in-app broadcast receivers (a private signature permission)',
}
FORBIDDEN = {
    'android.permission.INTERNET': 'the privacy policy says the app has no internet permission',
    'com.google.android.gms.permission.AD_ID': 'the app has no ads or advertising ID (Data safety / policy)',
    'com.android.vending.BILLING': 'no Play Billing in this release (Play requires Billing 8.0+ if the library is present)',
    'android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS': 'Play restricts it; the app opens the system battery list instead',
}
problems = []
for p in sorted(perms):
    if p in FORBIDDEN: problems.append(f'FORBIDDEN permission {p}: {FORBIDDEN[p]}')
    elif p not in OWN and p not in LIBRARY: problems.append(f'UNREVIEWED permission {p}: review it, then update the privacy policy, Data safety answers and this allow-list')
missing = sorted(OWN - perms)
if 'android:targetSdkVersion="36"' not in mf: problems.append('targetSdkVersion is not 36 (Google Play requires 36)')

print('Permissions in the merged manifest:')
for p in sorted(perms):
    tag = 'own' if p in OWN else ('library: ' + LIBRARY[p] if p in LIBRARY else 'UNEXPECTED')
    print(f'  {p}  [{tag}]')
if missing: print('Note: declared in source but not in the merged manifest:', missing)
if problems:
    print('\nMERGED MANIFEST CHECK: FAILED')
    for x in problems: print(' -', x)
    sys.exit(1)
print('\nMERGED MANIFEST CHECK: OK')
