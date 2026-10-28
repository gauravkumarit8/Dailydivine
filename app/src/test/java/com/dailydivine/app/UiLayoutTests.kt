package com.dailydivine.app

/**
 * JUnit category for the Robolectric UI layout tests. They are slower and need the Android
 * resources, so the normal `testDebugUnitTest` run (and the release gate) skips them; the
 * "UI layout tests" step in build.yml runs them with -PuiTests.
 */
interface UiLayoutTests
