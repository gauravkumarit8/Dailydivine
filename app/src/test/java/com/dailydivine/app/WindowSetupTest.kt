package com.dailydivine.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the window setup that once showed an unwanted "DailyDivine" ActionBar on every screen:
 * enableEdgeToEdge() ran BEFORE installSplashScreen(), so the window decor (and an ActionBar) was
 * built under the splash theme. These read the source files (unit tests run in the module directory).
 */
class WindowSetupTest {

    private fun codeOf(path: String): String =
        File(path).readLines().filterNot { it.trim().startsWith("//") }.joinToString("\n")

    @Test fun splashIsInstalledBeforeEdgeToEdgeAndSuperOnCreate() {
        val code = codeOf("src/main/java/com/dailydivine/app/ui/MainActivity.kt")
        val splash = code.indexOf("installSplashScreen()")
        val edge = code.indexOf("enableEdgeToEdge()")
        val sup = code.indexOf("super.onCreate(")
        assertTrue("installSplashScreen() missing", splash >= 0)
        assertTrue("enableEdgeToEdge() missing", edge >= 0)
        assertTrue("installSplashScreen() must come before enableEdgeToEdge()", splash < edge)
        assertTrue("enableEdgeToEdge() must come before super.onCreate()", edge < sup)
    }

    /** The text of one <style> element: up to its own "/>" or "</style>", whichever comes first. */
    private fun styleBlock(xml: String, name: String): String {
        val start = xml.indexOf("<style name=\"$name\"")
        assertTrue("style $name missing", start >= 0)
        val close = listOf(xml.indexOf("/>", start), xml.indexOf("</style>", start)).filter { it >= 0 }.minOrNull()!!
        return xml.substring(start, close)
    }

    @Test fun splashThemesNeverHaveAnActionBar() {
        for (path in listOf("src/main/res/values/themes.xml", "src/main/res/values-night/themes.xml")) {
            val block = styleBlock(File(path).readText(), "Theme.DailyDivine.Splash")
            assertTrue("$path: windowActionBar must be false", block.contains("name=\"android:windowActionBar\">false"))
            assertTrue("$path: windowNoTitle must be true", block.contains("name=\"android:windowNoTitle\">true"))
        }
    }

    @Test fun appWindowThemesAreNoActionBar() {
        for (path in listOf("src/main/res/values/themes.xml", "src/main/res/values-night/themes.xml")) {
            val block = styleBlock(File(path).readText(), "Theme.DailyDivine")
            assertTrue("$path: Theme.DailyDivine must be a NoActionBar theme", block.contains("NoActionBar"))
            assertFalse("$path: no AppCompat action bar theme", block.contains("AppCompat"))
        }
    }
}
