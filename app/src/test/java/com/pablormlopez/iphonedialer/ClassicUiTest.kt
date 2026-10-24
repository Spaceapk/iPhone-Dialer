package com.pablormlopez.iphonedialer
import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.pablormlopez.iphonedialer.data.Preferences
import com.pablormlopez.iphonedialer.ui.*
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=Application::class, qualifiers="w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalMaterial3Api::class)
class ClassicUiTest {
    @get:Rule val compose=createAndroidComposeRule<androidx.activity.ComponentActivity>()
    @Composable private fun Screen(theme: String) {
        DialerTheme(Preferences(theme=theme)) {
            Scaffold(containerColor=MaterialTheme.colorScheme.background,bottomBar={
                NavigationBar(containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp,modifier=Modifier.height(72.dp)) {
                    val names=listOf("Favoritos","Recientes","Contactos","Teclado","Buzón")
                    val icons=listOf(Icons.Default.Star,Icons.Default.History,Icons.Default.Contacts,Icons.Default.Dialpad,Icons.Default.Voicemail)
                    names.forEachIndexed { i,name -> NavigationBarItem(selected=i==3,onClick={},icon={Icon(icons[i],null)},label={Text(name,fontSize=12.sp,maxLines=1)},colors=NavigationBarItemDefaults.colors(indicatorColor=Color.Transparent)) }
                }
            }) { padding -> Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top=20.dp,bottom=16.dp)) { Text("Teclado",fontSize=34.sp,modifier=Modifier.weight(1f));IconButton(onClick={}) { Icon(Icons.Default.Settings,"Configuración") } }
                KeypadScreen("",{},Preferences(theme=theme),emptyList(),emptyList(),{},{})
            } }
        }
    }
    private fun capture(name: String) {
        val dir=File(System.getProperty("user.dir"),"build/classic-ui");dir.mkdirs()
        compose.waitForIdle()
        compose.runOnIdle {
            val view=compose.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            File(dir,name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
    }
    @Test fun lightClassicKeypadRenders() { compose.setContent { Screen("light") };compose.onNodeWithContentDescription("Llamar").assertIsDisplayed().assertIsNotEnabled();capture("classic-light.png") }
    @Test fun darkClassicKeypadRenders() { compose.setContent { Screen("dark") };compose.onNodeWithContentDescription("Llamar").assertIsDisplayed();capture("classic-dark.png") }
    @Test fun keypadOffersSaveAndCopyWithoutChangingNumber() {
        var saved="";var copied="";var current="12345"
        compose.setContent { DialerTheme(Preferences()) { KeypadScreen(current,{current=it},Preferences(),emptyList(),emptyList(),{},{},onSave={saved=it},onCopy={copied=it}) } }
        compose.onNodeWithText("Guardar número").performClick()
        compose.onNodeWithContentDescription("Copiar número").performClick()
        compose.runOnIdle { assertEquals("12345",saved);assertEquals("12345",copied);assertEquals("12345",current) }
    }
    @Test fun keypadLongDeleteClearsNumber() {
        var current="12345"
        compose.setContent { DialerTheme(Preferences()) { KeypadScreen(current,{current=it},Preferences(),emptyList(),emptyList(),{},{}) } }
        compose.onNodeWithContentDescription("Borrar dígito").performTouchInput { longClick() }
        compose.runOnIdle { assertEquals("",current) }
    }
    @Test fun keypadStillRequestsEnteredNumber() {
        var dialed=""
        compose.setContent { var number by remember { mutableStateOf("") };DialerTheme(Preferences()) { KeypadScreen(number,{number=it},Preferences(),emptyList(),emptyList(),{dialed=it},{}) } }
        compose.onNodeWithText("2",useUnmergedTree=true).performClick();compose.onNodeWithText("5",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Llamar").assertIsDisplayed().performClick();compose.runOnIdle { assertEquals("25",dialed) }
    }
}
