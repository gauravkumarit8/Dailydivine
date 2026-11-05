#!/usr/bin/env python3
"""Partial compile check that works in the sandbox (no Android SDK, no Compose libraries).

Downloads the Kotlin compiler from a GitHub release (github.com is on the sandbox allow-list), compiles
every project source file WITHOUT the Android/Compose classpath, and reports what is still meaningful:
  - syntax errors (anything that is not an "unresolved reference" family message),
  - unresolved references to symbols declared in this project.
Thousands of "unresolved reference" errors for Android/Compose/Hilt/Room symbols are EXPECTED and ignored.
Known false positives: extension functions on Compose receivers (RowScope.X), android.provider.Settings,
annotation arguments that name library classes, 'operator modifier required on compareTo' on library types.
This is NOT a substitute for the real build (Gradle on CI); it catches typos, unbalanced code and project
call mistakes. Usage: python3 tools/kotlinc_syntax_check.py
"""
import glob, io, os, re, subprocess, sys, urllib.request, zipfile

VERSION = "1.9.22"
HOME = "/tmp/kotlinc_" + VERSION
ZIP = HOME + ".zip"
URL = f"https://github.com/JetBrains/kotlin/releases/download/v{VERSION}/kotlin-compiler-{VERSION}.zip"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def ensure_compiler():
    kotlinc = os.path.join(HOME, "kotlinc", "bin", "kotlinc")
    if os.path.exists(kotlinc):
        return kotlinc
    print("downloading Kotlin compiler", VERSION, "...")
    urllib.request.urlretrieve(URL, ZIP)
    with zipfile.ZipFile(ZIP) as z:
        z.extractall(HOME)
    os.chmod(kotlinc, 0o755)
    return kotlinc

def main():
    kotlinc = ensure_compiler()
    files = sorted(glob.glob(ROOT + "/app/src/main/java/**/*.kt", recursive=True) +
                   glob.glob(ROOT + "/app/src/test/java/**/*.kt", recursive=True))
    out = subprocess.run([kotlinc, "-nowarn", "-d", "/tmp/kotlinc_out", *files],
                         capture_output=True, text=True, timeout=600)
    lines = (out.stdout + out.stderr).splitlines()
    errors = [l for l in lines if " error: " in l]
    decl = set()
    for f in files:
        decl |= set(re.findall(r"\b(?:fun|class|object|interface|val|var)\s+(?:<[^>]*>\s*)?(?:[A-Za-z0-9_.]+\.)?([A-Za-z_]\w*)",
                               io.open(f, encoding="utf-8").read()))
    syntax = [l for l in errors if re.search(r"error: (expecting|unexpected|missing|unclosed|illegal|syntax)", l, re.I)]
    project_refs = []
    for l in errors:
        m = re.search(r"^(\S+\.kt):(\d+):\d+: error: unresolved reference: (\w+)", l)
        if m and m.group(3) in decl:
            project_refs.append(f"{os.path.basename(m.group(1))}:{m.group(2)} {m.group(3)}")
    print(f"{len(files)} files, {len(errors)} errors in total (mostly missing-library noise)")
    print(f"SYNTAX ERRORS: {len(syntax)}")
    for l in syntax[:50]:
        print("  ", l)
    print(f"unresolved references to project-declared names (review; many are false positives): {len(project_refs)}")
    for r in sorted(set(project_refs))[:60]:
        print("  ", r)
    return 1 if syntax else 0

if __name__ == "__main__":
    sys.exit(main())
