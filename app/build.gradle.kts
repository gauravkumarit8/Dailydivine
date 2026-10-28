plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// Release versioning comes from the release workflow (-PappVersionCode / -PappVersionName).
// Local and CI debug builds fall back to these defaults. Keep the default versionCode at or
// above any build already installed on a test phone: Android refuses to install a LOWER one.
val appVersionCode: Int = (findProperty("appVersionCode") as String?)?.let { raw ->
    // Android (and Google Play) reject 0 and negative numbers. Fail here with a plain message
    // instead of a long Gradle stack trace.
    raw.toIntOrNull()?.takeIf { it >= 1 }
        ?: throw GradleException("appVersionCode must be a whole number of 1 or more, but got \"$raw\". Use 1 for the first Play upload, then 2, 3, ...")
} ?: 2
val appVersionName: String = (findProperty("appVersionName") as String?) ?: "1.0.0"

android {
    namespace = "com.dailydivine.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dailydivine.app"
        minSdk = 26
        // Google Play requires new apps and updates to target Android 16 (API 36) since
        // 2026-08-31. targetSdk is deliberately NOT tied to compileSdk (the official Android
        // build docs: "you can have a value of targetSdk that is higher, the same, or lower than
        // compileSdk"). The Android 15/16 runtime behaviour changes are handled in code (edge-to-edge,
        // predictive back); compileSdk stays on the tested toolchain until a planned AGP/Kotlin upgrade.
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing: only when the release workflow (or a developer) provides the keystore via
    // environment variables. Without them `bundleRelease` produces an UNSIGNED bundle, so an
    // unsigned or debug-signed build can never be uploaded to Play by accident. The keystore
    // itself is never committed (see .gitignore and play-store/RELEASE.md).
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // R8 shrinking/obfuscation: smaller download and Play's recommended release setup.
            // The keep rules in proguard-rules.pro are REQUIRED (Gson reads the verse files by reflection).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) signingConfig = releaseSigning
        }
    }

    lint {
        // Release builds run "lintVital". Keep the report (the release workflow uploads it) but do
        // not let a finding block a store build on a toolchain we cannot test locally.
        abortOnError = false
        checkReleaseBuilds = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    testOptions {
        unitTests {
            // Robolectric (JVM UI layout tests) needs the merged Android resources and manifest.
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

// The slower UI layout tests (ResponsiveLayoutTest, category UiLayoutTests) run only when asked for
// with -PuiTests, in their own CI step, so they can never block the normal test run or the release gate.
tasks.withType<Test>().configureEach {
    if (!project.hasProperty("uiTests")) {
        useJUnit { excludeCategories("com.dailydivine.app.UiLayoutTests") }
    }
}

// Resolves the Room KSP warning: "Schema export directory was not provided".
// Exported schemas are checked into version control under app/schemas/ so
// migrations (like v1.1's MIGRATION_1_2) can be tested against real prior
// schema snapshots later, per Room's own recommended practice.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    // v was 2024.01.00 -- bumped to fix a CONFIRMED Google bug (not a guess):
    // https://issuetracker.google.com/issues/322214617 -- a binary
    // compatibility break in KeyframesSpec.at()/atFraction() shipped in the
    // 2024.01.00-era animation-core, breaking material3's own
    // CircularProgressIndicator at runtime with:
    //   NoSuchMethodError: No virtual method at(...)Landroidx/compose/
    //   animation/core/KeyframesSpec$KeyframeEntity; in KeyframesSpecConfig
    // This is not a version-skew issue between OUR dependencies (we already
    // ruled that out by disabling androidx.glance and confirming the exact
    // same crash persisted, disproving that hypothesis) -- it's a genuine
    // bug inside this specific compose-bom release that even hit Google's
    // own official JetNews sample app and multiple unrelated projects.
    // Google's fix landed Jan 25 2024 and shipped in the next BOM release;
    // 2024.04.01 is safely past it and still Compose-Compiler-1.5.8/
    // Kotlin-1.9.22 compatible (same "Compose 1.6.x" runtime family, no
    // compiler/Kotlin bump needed). See CHECKLIST.md for the full trail.
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room (Database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // REMOVED for the first Play release (none of these were used by any code):
    //  - Media3 (the alarm uses MediaPlayer), Coil, Lottie (confetti is drawn natively).
    //  - Firebase Analytics: would add the advertising-ID permission and a data-collection
    //    disclosure for no benefit. Re-add deliberately, together with the privacy policy + Data safety form.
    //  - Play Billing 6.1: Google Play requires Billing Library 8.0.0+ for any app that includes it; add
    //    the current version when Sprint 7 actually implements premium.
    //
    // Google AdMob + UMP (v1.1 — SEC-11 consent gate)
    // DISABLED until Sprint 7 (Monetization) actually implements AdMob.
    // play-services-ads registers a ContentProvider (MobileAdsInitProvider)
    // that auto-runs on app startup -- before MainActivity, before Hilt,
    // before anything in our code -- and it hard-crashes the entire process
    // if the manifest has no AdMob App ID meta-data. We had this dependency
    // declared with zero AdMob configuration (no App ID, no ConsentManager),
    // so every real launch crashed immediately with:
    //   "The Google Mobile Ads SDK was initialized incorrectly... add a
    //    valid App ID inside the AndroidManifest."
    // Caught via a real device install + adb logcat, see CHECKLIST.md.
    // Re-enable both lines AND add the required <meta-data> App ID to
    // AndroidManifest.xml together, as one Sprint 7 change -- never one
    // without the other.
    // implementation("com.google.android.gms:play-services-ads:22.6.0")
    // implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    // WorkManager (Background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Glance (Home Screen Widgets) — DISABLED until Sprint 9 actually builds
    // DailyVerseWidget.kt. Simply unused right now, nothing more: it was
    // briefly suspected as the cause of a real runtime crash (a
    // NoSuchMethodError in material3's CircularProgressIndicator), but that
    // theory was DISPROVEN -- disabling Glance alone did not fix the crash,
    // which turned out to be a confirmed Google bug in the compose-bom
    // itself (see the compose-bom comment above, and CHECKLIST.md's "second
    // runtime crash, corrected diagnosis" entry for the full trail,
    // including the wrong first guess). Safe to re-enable in Sprint 9 --
    // no known reason not to, now that the actual bug is fixed via the BOM
    // bump.
    // implementation("androidx.glance:glance-appwidget:1.0.0")
    // implementation("androidx.glance:glance-material3:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    // JVM UI layout tests (ResponsiveLayoutTest): Compose UI test + Robolectric, no emulator needed.
    testImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.robolectric:robolectric:4.11.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
