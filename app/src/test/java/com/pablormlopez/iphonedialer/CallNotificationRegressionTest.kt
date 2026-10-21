package com.pablormlopez.iphonedialer

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.test.core.app.ApplicationProvider
import com.pablormlopez.iphonedialer.telephony.CallNotificationFactory
import com.pablormlopez.iphonedialer.telephony.LiveCall
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=Application::class)
class CallNotificationRegressionTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private fun call(state: Int) = LiveCall("qa-session", "Contacto de prueba", "5550101", state, 0, false, false, false, false)
    // Exact relevant Android 13 AOSP contract, not a claim to execute system_server on hardware.
    private fun assertAndroid13Contract(n: Notification) {
        val isCallStyle = n.extras.getString("android.template") == Notification.CallStyle::class.java.name
        require(!isCallStyle || n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0 || n.fullScreenIntent != null) {
            "Android 13 rejects CallStyle without foreground service/fullScreenIntent"
        }
    }
    @Test fun originalOngoingConstructionViolatesPlatformContract() {
        val pending=PendingIntent.getActivity(context,11,Intent(context, android.app.Activity::class.java),PendingIntent.FLAG_IMMUTABLE)
        val old = NotificationCompat.Builder(context,"ongoing").setSmallIcon(R.drawable.ic_call)
            .setStyle(NotificationCompat.CallStyle.forOngoingCall(Person.Builder().setName("Prueba").build(),pending)).build()
        assertThrows(IllegalArgumentException::class.java) { assertAndroid13Contract(old) }
    }
    @Test fun outgoingUsesOrdinaryNotificationAndRealHangupAction() {
        val n=CallNotificationFactory(context).build(call(Call.STATE_DIALING),false)
        assertAndroid13Contract(n)
        assertNull(n.fullScreenIntent)
        assertNotEquals(Notification.CallStyle::class.java.name,n.extras.getString("android.template"))
        assertTrue(n.actions.any { it.title.toString()=="Finalizar" && it.actionIntent!=null })
    }
    @Test fun activeTransitionNeverAddsInvalidCallStyle() {
        val n=CallNotificationFactory(context).build(call(Call.STATE_ACTIVE),true)
        assertAndroid13Contract(n)
        assertNull(n.fullScreenIntent) // Never open full-screen merely because an outgoing call connects.
        assertNotEquals(Notification.CallStyle::class.java.name,n.extras.getString("android.template"))
    }
    @Test fun incomingFullScreenCallStyleMeetsContract() {
        val n=CallNotificationFactory(context).build(call(Call.STATE_RINGING),true)
        assertAndroid13Contract(n)
        assertNotNull(n.fullScreenIntent)
        assertEquals(Notification.CallStyle::class.java.name,n.extras.getString("android.template"))
    }
    @Test fun deniedFullScreenFallsBackToOrdinaryAnswerRejectActions() {
        val n=CallNotificationFactory(context).build(call(Call.STATE_RINGING),false)
        assertAndroid13Contract(n)
        assertNull(n.fullScreenIntent)
        assertEquals(setOf("Contestar","Rechazar"),n.actions.map { it.title.toString() }.toSet())
    }
}
