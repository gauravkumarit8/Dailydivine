#!/usr/bin/env python3
"""Fills in the personal details that only the developer can supply, everywhere they are needed:
the in-app Settings > About contact, and the landing page, privacy policy and terms.
Usage (repo root):
  python3 tools/configure_release.py --email you@example.com --name "Your Name or Company"
  python3 tools/configure_release.py --play-url "https://play.google.com/store/apps/details?id=com.dailydivine.app"   # after the app is live
No Python? See the PowerShell alternative in play-store/RELEASE.md."""
import argparse, glob, os, re, sys
ROOT = os.path.join(os.path.dirname(__file__), '..')
ap = argparse.ArgumentParser()
ap.add_argument('--email'); ap.add_argument('--name'); ap.add_argument('--play-url')
a = ap.parse_args()
if not (a.email or a.name or a.play_url): ap.error('give at least one of --email, --name, --play-url')
if a.email and not re.fullmatch(r'[^@\s]+@[^@\s]+\.[^@\s]+', a.email): sys.exit('That does not look like an email address')
if a.play_url and not a.play_url.startswith('https://play.google.com/'): sys.exit('--play-url must start with https://play.google.com/')
files = glob.glob(os.path.join(ROOT, 'docs', '*.html')) + [os.path.join(ROOT, 'app', 'src', 'main', 'java', 'com', 'dailydivine', 'app', 'util', 'AppLinks.kt')]
changed = 0
for p in files:
    s = open(p, encoding='utf-8').read(); o = s
    if a.email: s = s.replace('REPLACE_ME_EMAIL', a.email)
    if a.name: s = s.replace('REPLACE_ME_DEVELOPER_NAME', a.name)
    if a.play_url and p.endswith('index.html'):
        s = re.sub(r'<!--PLAY_BUTTON_START-->.*?<!--PLAY_BUTTON_END-->',
                   f'<!--PLAY_BUTTON_START--><a class="btn" href="{a.play_url}">Get it on Google Play</a><span class="badge-note">Android 8.0 and newer</span><!--PLAY_BUTTON_END-->', s, flags=re.S)
    if s != o: open(p, 'w', encoding='utf-8').write(s); changed += 1; print('updated', os.path.relpath(p, ROOT))
print(changed, 'file(s) updated')
