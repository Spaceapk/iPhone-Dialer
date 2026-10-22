package com.pablormlopez.iphonedialer

import android.app.Application
import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.ContactsContract
import android.telecom.Call
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.pablormlopez.iphonedialer.data.Preferences
import com.pablormlopez.iphonedialer.telephony.*
import com.pablormlopez.iphonedialer.ui.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class,qualifiers="w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CallPhotoUiTest {
    @get:Rule val compose=createAndroidComposeRule<androidx.activity.ComponentActivity>()
    private fun show(photo: Boolean,ringing: Boolean=false) {
        val context=RuntimeEnvironment.getApplication()
        val fixture=System.getenv("CALL_PREVIEW_PHOTO")?.let(::File)?.takeIf { it.isFile }
        val bitmap=fixture?.let { BitmapFactory.decodeFile(it.path) } ?: Bitmap.createBitmap(240,320,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.rgb(110,130,160)) }
        val file=File(context.cacheDir,"test-contact-photo.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        val provider=object: ContentProvider() {
            override fun onCreate()=true
            override fun query(uri: Uri,projection: Array<out String>?,selection: String?,args: Array<out String>?,sort: String?): Cursor = MatrixCursor(arrayOf("_id","lookup","display_name","photo_uri")).apply { addRow(arrayOf<Any?>(1,"test","Contacto de prueba",if(photo) "content://com.android.contacts/display_photo/1" else null)) }
            override fun openFile(uri: Uri,mode: String): ParcelFileDescriptor=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
            override fun getType(uri: Uri)="image/png"
            override fun insert(uri: Uri,values: ContentValues?): Uri?=null
            override fun update(uri: Uri,values: ContentValues?,selection: String?,args: Array<out String>?)=0
            override fun delete(uri: Uri,selection: String?,args: Array<out String>?)=0
        }
        provider.attachInfo(context,android.content.pm.ProviderInfo().apply { authority=ContactsContract.AUTHORITY;exported=true })
        ShadowContentResolver.registerProviderInternal(ContactsContract.AUTHORITY,provider)
        val store=CallStore()
        @Suppress("UNCHECKED_CAST")
        val flow=CallStore::class.java.getDeclaredField("mutable").apply { isAccessible=true }.get(store) as MutableStateFlow<LiveState>
        flow.value=LiveState(calls=listOf(LiveCall("test","Contacto de prueba","5550101",if(ringing) Call.STATE_RINGING else Call.STATE_ACTIVE,0,true,false,false,false)),outputs=listOf(AudioOutput("speaker","Altavoz")),canAdd=true)
        compose.setContent { DialerTheme(Preferences(theme="dark")) { InCallScreen(store,false,{},{}) } }
        compose.waitUntil(10000) { compose.onAllNodesWithText("5550101").fetchSemanticsNodes().isNotEmpty() }
        if(photo) compose.waitUntil(10000) { compose.onAllNodesWithTag("call-photo-background").fetchSemanticsNodes().isNotEmpty() }
        compose.waitForIdle()
    }
    private fun capture(name: String) {
        val dir=File(System.getProperty("user.dir"),"build/call-photo-ui");dir.mkdirs()
        compose.waitForIdle()
        compose.runOnIdle {
            val view=compose.activity.window.decorView
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            File(dir,name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        }
    }
    @Test fun photoDoesNotCoverControlsOrConsumeTaps() {
        show(true)
        compose.onNodeWithContentDescription("Silencio").assertIsDisplayed().assertHasClickAction().performClick()
        compose.onNodeWithContentDescription("Finalizar llamada").assertIsDisplayed().assertHasClickAction()
        capture("call-photo-active.png")
    }
    @Test fun missingPhotoKeepsFallbackAndControls() {
        show(false);compose.onNodeWithContentDescription("Finalizar llamada").assertIsDisplayed();capture("call-no-photo.png")
    }
    @Test fun incomingPhotoKeepsAnswerAndRejectAccessible() {
        show(true,true)
        compose.onNodeWithText("Contestar").assertIsDisplayed().assertHasClickAction()
        compose.onNodeWithText("Rechazar").assertIsDisplayed().assertHasClickAction()
        capture("call-photo-incoming.png")
    }
}
