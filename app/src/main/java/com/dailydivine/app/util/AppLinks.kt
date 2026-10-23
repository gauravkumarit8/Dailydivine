package com.dailydivine.app.util

/**
 * Public links used by Settings > About. The GitHub Pages site lives in /docs and is published by
 * .github/workflows/pages.yml. SUPPORT_EMAIL is a placeholder until tools/configure_release.py
 * (or the PowerShell snippet in play-store/RELEASE.md) fills it in; the release workflow REFUSES
 * to build while a placeholder is still present.
 */
object AppLinks {
    const val SITE_URL = "https://grvthakur.bid/daily-divine/"
    const val PRIVACY_URL = SITE_URL + "privacy.html"
    const val TERMS_URL = SITE_URL + "terms.html"
    const val SUPPORT_EMAIL = "gaurav954624@gmail.com"
}
