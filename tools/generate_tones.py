#!/usr/bin/env python3
"""Generates DailyDivine's bundled alarm tones as original synthesized audio
(no third-party recordings, so no licensing issues). Output: 16 kHz mono
16-bit WAV files in app/src/main/res/raw/. Run: python3 tools/generate_tones.py

Deliberately NOT synthesized: vocal/sacred recitations (Om chanting, Azaan,
choir hymn, Shabad). Faking those would be inaccurate and disrespectful; they
need properly licensed real recordings (PRD Appendix B).
"""
import numpy as np, wave, os
SR = 16000
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res', 'raw')
rng = np.random.default_rng(7)

def T(d): return np.arange(int(SR * d)) / SR
def sine(f, t): return np.sin(2 * np.pi * f * t)
def env_exp(t, d): return np.exp(-d * t)
def place(buf, sig, start):
    s = int(SR * start); n = min(len(sig), len(buf) - s)
    if n > 0: buf[s:s + n] += sig[:n]
def lowpass(x, k):
    kern = np.ones(k) / k
    return np.convolve(x, kern, mode='same')
def finish(y, fade_in=0.02, fade_out=0.4):
    n = len(y); t = np.arange(n) / SR; d = n / SR
    y = y * np.minimum(1, t / fade_in) * np.minimum(1, (d - t) / fade_out)
    return y / max(1e-9, np.max(np.abs(y))) * 0.85
def bell(f0, partials, dur, t0=0):
    t = T(dur); y = np.zeros_like(t)
    for r, a, d in partials: y += a * env_exp(t, d) * sine(f0 * r, t)
    return y * np.minimum(1, t / 0.004)

TEMPLE = [(0.5,.6,.7),(1,1,.9),(1.19,.5,1.1),(1.56,.4,1.3),(2,.45,1.6),(2.74,.3,2.0),(3.76,.2,2.6),(5.43,.1,3.5)]
CHURCH = [(0.5,.5,.35),(1,1,.5),(1.2,.6,.6),(1.5,.5,.7),(2,.6,.8),(2.5,.35,1.0),(3,.25,1.3),(4.1,.15,1.8)]
BOWL   = [(1,1,.28),(2.71,.55,.5),(5.15,.3,.8),(8.4,.12,1.4)]
GONG   = [(0.5,.5,.45),(1,1,.55),(1.47,.6,.7),(2.09,.5,.8),(2.95,.4,1.0),(3.6,.25,1.3),(4.8,.15,1.7)]
CHIME  = [(1,1,1.6),(2.76,.5,2.4),(5.4,.25,3.6)]

def temple():
    y = np.zeros(int(SR*8)); place(y, bell(440, TEMPLE, 5), 0); place(y, bell(440, TEMPLE, 5)*.9, 3.8); return finish(y)
def church():
    y = np.zeros(int(SR*8)); 
    for i, f in enumerate([392, 392, 392, 392]): place(y, bell(f, CHURCH, 3.5)*.9, i*1.7)
    return finish(y)
def bowl():
    y = np.zeros(int(SR*8)); place(y, bell(220, BOWL, 8)*(1+0.12*sine(5,T(8))), 0); return finish(y, fade_out=0.8)
def gong():
    y = np.zeros(int(SR*7)); place(y, bell(110, GONG, 7)*(1+0.1*sine(3.3,T(7))), 0); return finish(y, fade_out=0.8)
def chimes():
    y = np.zeros(int(SR*8)); notes=[784,988,1175,1319,1568,1319,988]
    for i,f in enumerate(notes): place(y, bell(f, CHIME, 3)*.7, i*0.7)
    return finish(y)
def flute():
    # pentatonic phrase; breathy sine + soft vibrato + noise
    notes=[(293.7,1.2),(329.6,.8),(392,1.2),(440,.8),(392,1.0),(329.6,.8),(293.7,2.2)]
    y=[]
    for f,d in notes:
        t=T(d); vib=1+0.006*np.sin(2*np.pi*5.5*t)*np.minimum(1,t/.4)
        ph=2*np.pi*np.cumsum(f*vib)/SR
        s=np.sin(ph)+.25*np.sin(2*ph)+.08*np.sin(3*ph)
        s+=0.05*lowpass(rng.standard_normal(len(t)),4)
        e=np.minimum(1,t/.12)*np.minimum(1,(d-t)/.15)
        y.append(s*e)
    return finish(np.concatenate(y), fade_out=0.6)
def harp():
    y = np.zeros(int(SR*9)); notes=[261.6,329.6,392,523.3,659.3,784,659.3,523.3,392,329.6]
    for i,f in enumerate(notes):
        t=T(3); s=(sine(f,t)+.4*sine(2*f,t)*env_exp(t,3)+.2*sine(3*f,t)*env_exp(t,5))*env_exp(t,1.6)*np.minimum(1,t/.003)
        place(y, s, i*0.75)
    return finish(y)
def piano():
    y = np.zeros(int(SR*10)); chords=[[261.6,329.6,392],[220,261.6,329.6],[174.6,220,261.6],[196,246.9,293.7]]
    for i,ch in enumerate(chords):
        for j,f in enumerate(ch):
            t=T(3.2); s=(sine(f,t)+.5*sine(2*f,t)*env_exp(t,2)+.25*sine(3*f,t)*env_exp(t,3.5)+.1*sine(4*f,t)*env_exp(t,5))
            place(y, s*env_exp(t,1.1)*np.minimum(1,t/.004)*.8, i*2.3 + j*0.12)
    return finish(y, fade_out=0.8)
def rain():
    d=10; n=int(SR*d); x=rng.standard_normal(n)
    y=lowpass(x,3)*0.5+lowpass(x,40)*1.5
    drops=np.zeros(n)
    for _ in range(180):
        p=rng.integers(0,n-300); k=int(rng.integers(60,200)); drops[p:p+k]+=rng.uniform(.2,.7)*np.exp(-np.arange(k)/(k/5))*np.sin(2*np.pi*rng.uniform(1500,3500)*np.arange(k)/SR)
    y=y/np.max(np.abs(y))*0.6+drops*0.6
    return finish(y, fade_in=0.4, fade_out=0.6)
def ocean():
    d=10; t=T(d); x=rng.standard_normal(len(t)); base=lowpass(x,24)
    swell=0.55+0.45*np.sin(2*np.pi*t/5.0-1.2)  # ~5 s wave cycle -> loops cleanly over 10 s
    return finish(base/np.max(np.abs(base))*swell, fade_in=0.4, fade_out=0.6)
def birds():
    d=10; y=np.zeros(int(SR*d))
    for _ in range(9):
        start=rng.uniform(0,d-1.6); f0=rng.uniform(2200,4200); n_chirps=int(rng.integers(2,5))
        for c in range(n_chirps):
            dur=rng.uniform(.06,.14); t=T(dur); sweep=f0*(1+rng.uniform(.15,.45)*t/dur)
            ph=2*np.pi*np.cumsum(sweep)/SR; s=np.sin(ph)*np.sin(np.pi*t/dur)**2*rng.uniform(.4,.9)
            place(y, s, start+c*(dur+.05))
    y+=0.02*lowpass(rng.standard_normal(len(y)),6)
    return finish(y, fade_in=0.3, fade_out=0.6)

TONES = {'temple_bell':temple,'bamboo_flute':flute,'church_bell':church,'tibetan_bowl':bowl,'meditation_gong':gong,
         'soft_chimes':chimes,'gentle_harp':harp,'peaceful_piano':piano,'nature_rain':rain,'nature_ocean':ocean,'nature_birds':birds}
if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for name, fn in TONES.items():
        y = fn(); pcm = (y * 32767).astype('<i2')
        p = os.path.join(OUT, name + '.wav')
        with wave.open(p, 'wb') as w: w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
        print(f'{name}.wav {len(y)/SR:.1f}s {os.path.getsize(p)//1024}KB')
