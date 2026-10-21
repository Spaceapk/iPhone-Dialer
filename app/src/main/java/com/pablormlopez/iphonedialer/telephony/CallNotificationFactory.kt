package com.pablormlopez.iphonedialer.telephony

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.pablormlopez.iphonedialer.R
import com.pablormlopez.iphonedialer.ui.InCallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Bound InCallService is not a foreground service. Never post ongoing CallStyle here.
 * Android 12/13 rejects it without FLAG_FOREGROUND_SERVICE or a full-screen intent.
 * A normal ongoing notification keeps the real hang-up action without adding permissions.
 */
@Singleton
class CallNotificationFactory @Inject constructor(@ApplicationContext private val context: Context) {
    fun build(call: LiveCall, allowFullScreen: Boolean): Notification {
        val incoming = call.state == Call.STATE_RINGING
        val open = screenIntent(call.id)
        val end = actionIntent(call.id, if (incoming) "reject" else "end")
        val builder = NotificationCompat.Builder(context, if (incoming) "incoming" else "ongoing")
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(call.name.ifBlank { "Llamada" })
            .setContentText(if (incoming) "Llamada entrante" else "Llamada en curso")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(if (incoming) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setOngoing(true).setOnlyAlertOnce(true)
            .setContentIntent(open).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        if (incoming && allowFullScreen) {
            val person = Person.Builder().setName(call.name.ifBlank { "Llamada" }).setImportant(true).build()
            builder.setFullScreenIntent(open, true)
                .setStyle(NotificationCompat.CallStyle.forIncomingCall(person, end, screenIntent(call.id, "answer")))
        } else if (incoming) {
            // Full-screen access denied/restricted: use regular actionable notification.
            builder.addAction(R.drawable.ic_call, "Contestar", screenIntent(call.id, "answer"))
                .addAction(R.drawable.ic_call, "Rechazar", end)
        } else {
            builder.addAction(R.drawable.ic_call, "Finalizar", end)
        }
        if (!incoming && call.connectedAt > 0) builder.setWhen(call.connectedAt).setUsesChronometer(true)
        return builder.build()
    }

    private fun screenIntent(id: String, action: String? = null): PendingIntent {
        val intent = Intent(context, InCallActivity::class.java)
            .setAction("call:$id:${action.orEmpty()}")
            .putExtra("call_id", id).putExtra("call_action", action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, id.hashCode() xor (action?.hashCode() ?: 0), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun actionIntent(id: String, action: String): PendingIntent = PendingIntent.getBroadcast(
        context, (id + action).hashCode(), Intent(context, CallActionReceiver::class.java)
            .setAction(action).putExtra("call_id", id), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
