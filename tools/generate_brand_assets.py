#!/usr/bin/env python3
"""Generates DailyDivine's icon (Android adaptive-icon vectors) and store artwork from ONE geometry
definition, so the app icon, Play Store icon, feature graphic and website images always match.
The motif is a sunrise over still water: a universal 'new day' symbol that favours no one tradition.
Needs Pillow for the PNGs. Run from the repo root:  python3 tools/generate_brand_assets.py"""
import math, os
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(__file__), '..')
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')
FONT_DIR = os.path.join(RES, 'font')

# ---- geometry in the 108 x 108 adaptive-icon viewport (content stays inside the 66dp safe circle)
CX, CY, SUN_R = 54.0, 62.0, 14.0
RAY_IN, RAY_OUT = 20.0, 29.0
RAY_ANGLES = [150, 120, 90, 60, 30]
STOPS = [(0.0, (0x1B, 0x1F, 0x6B)), (0.55, (0x6A, 0x2F, 0x8F)), (0.85, (0xF2, 0x76, 0x2E)), (1.0, (0xFF, 0xB3, 0x00))]
CREAM = (0xFD, 0xF3, 0xE0)

def ray(theta):
    t = math.radians(theta)
    return (CX + RAY_IN * math.cos(t), CY - RAY_IN * math.sin(t), CX + RAY_OUT * math.cos(t), CY - RAY_OUT * math.sin(t))

def hexa(rgb): return '#FF%02X%02X%02X' % rgb

def foreground_paths(color, with_alpha=True):
    out = [f'    <path android:fillColor="{color}" android:pathData="M{CX-SUN_R:g},{CY:g} A{SUN_R:g},{SUN_R:g} 0 0 1 {CX+SUN_R:g},{CY:g} Z" />']
    def stroke(d, alpha=1.0):
        a = f' android:strokeAlpha="{alpha}"' if (alpha != 1.0 and with_alpha) else ''
        out.append(f'    <path android:pathData="{d}" android:strokeColor="{color}" android:strokeWidth="3" android:strokeLineCap="round"{a} />')
    for th in RAY_ANGLES:
        x1, y1, x2, y2 = ray(th); stroke(f'M{x1:.2f},{y1:.2f} L{x2:.2f},{y2:.2f}')
    stroke(f'M28,{CY:g} L80,{CY:g}')
    stroke(f'M40,70 L68,70', 0.7)
    stroke(f'M47,77 L61,77', 0.5)
    return '\n'.join(out)

def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w', encoding='utf-8').write(text)

# ---- Android vectors
write(os.path.join(RES, 'drawable', 'ic_launcher_background.xml'), f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient android:type="linear" android:startX="54" android:startY="0" android:endX="54" android:endY="108">
''' + '\n'.join(f'                <item android:offset="{o}" android:color="{hexa(c)}" />' for o, c in STOPS) + '''
            </gradient>
        </aapt:attr>
    </path>
</vector>
''')
vec_head = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
'''
write(os.path.join(RES, 'drawable', 'ic_launcher_foreground.xml'), vec_head + foreground_paths(hexa(CREAM)) + '\n</vector>\n')
write(os.path.join(RES, 'drawable', 'ic_launcher_monochrome.xml'), vec_head + foreground_paths('#FF000000', with_alpha=False) + '\n</vector>\n')
adaptive = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <!-- Android 13+ themed icons -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''
write(os.path.join(RES, 'mipmap-anydpi-v26', 'ic_launcher.xml'), adaptive)
write(os.path.join(RES, 'mipmap-anydpi-v26', 'ic_launcher_round.xml'), adaptive)

# ---- PNG artwork (4x supersampled for clean edges)
SS = 4
def grad_color(t):
    t = min(1.0, max(0.0, t))
    for i in range(len(STOPS) - 1):
        (t0, c0), (t1, c1) = STOPS[i], STOPS[i + 1]
        if t0 <= t <= t1:
            f = (t - t0) / (t1 - t0)
            return tuple(int(c0[k] + (c1[k] - c0[k]) * f) for k in range(3))
    return STOPS[-1][1]

def gradient_image(w, h):
    im = Image.new('RGB', (w, h)); px = im.load()
    for y in range(h):
        col = grad_color(y / max(1, h - 1))
        for x in range(w): px[x, y] = col
    return im

def draw_sun(draw, ox, oy, scale, rgb, H):
    """Draws the 108-unit motif with its (0,0) at (ox, oy), scaled by `scale`."""
    def P(x, y): return (ox + x * scale, oy + y * scale)
    r = SUN_R * scale; cx, cy = P(CX, CY)
    draw.pieslice([cx - r, cy - r, cx + r, cy + r], 180, 360, fill=rgb)
    w = max(1, int(3 * scale))
    def line(x1, y1, x2, y2, a=1.0):
        # translucent: blend the cream with the gradient colour actually behind this line
        bg = grad_color(P(0, y1)[1] / H)
        col = tuple(int(rgb[k] * a + bg[k] * (1 - a)) for k in range(3)) if a < 1.0 else rgb
        draw.line([P(x1, y1), P(x2, y2)], fill=col, width=w)
        for (px_, py_) in (P(x1, y1), P(x2, y2)):
            draw.ellipse([px_ - w / 2, py_ - w / 2, px_ + w / 2, py_ + w / 2], fill=col)
    for th in RAY_ANGLES:
        x1, y1, x2, y2 = ray(th); line(x1, y1, x2, y2)
    line(28, CY, 80, CY); line(40, 70, 68, 70, 0.8); line(47, 77, 61, 77, 0.65)

def canvas(w, h):
    im = gradient_image(w * SS, h * SS); return im, ImageDraw.Draw(im)

def finish(im, w, h): return im.resize((w, h), Image.LANCZOS)

def font(name, size):
    return ImageFont.truetype(os.path.join(FONT_DIR, name), size * SS)

def text_centered_left(draw, xy, text, fnt, fill):
    draw.text((xy[0] * SS, xy[1] * SS), text, font=fnt, fill=fill)

# Play Store / website icon: full-bleed square (Google Play applies the rounded mask itself).
im, d = canvas(512, 512); draw_sun(d, 0, 0, 512 * SS / 108, CREAM, 512 * SS)
finish(im, 512, 512).save(os.path.join(ROOT, 'play-store', 'assets', 'icon-512.png'))

def banner(w, h, path, title_size, tag_size):
    """Sun + title + tagline, measured and centred so nothing can clip at any size."""
    im, d = canvas(w, h)
    sun = h * 0.66
    gap = w * 0.035
    tf, gf = font('poppins_semibold.ttf', title_size), font('poppins_regular.ttf', tag_size)
    title, tag = 'DailyDivine', 'A verse for every morning'
    tw = max(tf.getlength(title), gf.getlength(tag)) / SS
    total = sun + gap + tw
    while total > w * 0.90:                       # shrink text until the whole group fits with margins
        title_size = int(title_size * 0.95); tag_size = int(tag_size * 0.95)
        tf, gf = font('poppins_semibold.ttf', title_size), font('poppins_regular.ttf', tag_size)
        tw = max(tf.getlength(title), gf.getlength(tag)) / SS
        total = sun + gap + tw
    left = (w - total) / 2
    draw_sun(d, left * SS, (h - sun) / 2 * SS, sun * SS / 108, CREAM, h * SS)
    tx = left + sun + gap
    block = title_size * 1.35 + tag_size * 1.2
    ty = (h - block) / 2
    text_centered_left(d, (tx, ty), title, tf, CREAM)
    text_centered_left(d, (tx, ty + title_size * 1.35), tag, gf, (0xFF, 0xFF, 0xFF))
    finish(im, w, h).save(path)

banner(1024, 500, os.path.join(ROOT, 'play-store', 'assets', 'feature-graphic-1024x500.png'), 84, 38)
banner(1200, 630, os.path.join(ROOT, 'docs', 'assets', 'og-image.png'), 96, 42)
# website copies
im = Image.open(os.path.join(ROOT, 'play-store', 'assets', 'icon-512.png'))
im.save(os.path.join(ROOT, 'docs', 'assets', 'icon-512.png'))
im.resize((64, 64), Image.LANCZOS).save(os.path.join(ROOT, 'docs', 'assets', 'favicon-64.png'))
print('assets written')
