package com.pablormlopez.iphonedialer
import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.pablormlopez.iphonedialer.ui.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
class ActivityLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun favoriteEmptyStateDoesNotOverlapSearch() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName,Manifest.permission.READ_CONTACTS)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Favoritos").performClick()
        val search=compose.onNodeWithText("Buscar nombre o número").fetchSemanticsNode().boundsInRoot
        val title=compose.onNodeWithText("Tus personas favoritas").fetchSemanticsNode().boundsInRoot
        assertTrue("Empty state overlaps search field",title.top>=search.bottom)
    }
}
