package com.pablormlopez.iphonedialer
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.pablormlopez.iphonedialer.data.Preferences
import com.pablormlopez.iphonedialer.ui.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
class KeypadTest {
    @get:Rule val compose = createComposeRule()
    @Test fun keypadCollectsActualInput() {
        var dialed=""
        compose.setContent { var number by remember { mutableStateOf("") };DialerTheme(Preferences()) { KeypadScreen(number,{number=it},Preferences(),emptyList(),emptyList(),{dialed=it},{}) } }
        compose.onNodeWithText("2",useUnmergedTree=true).performClick()
        compose.onNodeWithText("5",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Llamar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("25",dialed) }
    }
    @Test fun clearEmptyDoesNotCrash() {
        compose.setContent { DialerTheme(Preferences()) { KeypadScreen("",{},Preferences(),emptyList(),emptyList(),{},{}) } }
        compose.onNodeWithContentDescription("Borrar dígito").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Llamar").assertIsNotEnabled()
    }
}
