package com.pablormlopez.iphonedialer.telephony
import android.app.*
import android.content.*
import android.os.*
import android.telecom.*
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.pablormlopez.iphonedialer.R
import com.pablormlopez.iphonedialer.ui.InCallActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
@AndroidEntryPoint class DialerInCallService: InCallService() {
    @Inject lateinit var store: CallStore
    private val callbacks=mutableMapOf<Call, Call.Callback>()
    private val notificationIds=mutableMapOf<Call,Int>()
    private var nextId=100
    private var endpoints: List<CallEndpoint> = emptyList()
    private var selectedEndpoint=""
    private var isMuted=false
    private val notifications get() = getSystemService(NotificationManager::class.java)
    override fun onCreate() {
        super.onCreate(); store.service=this
        // Telecom retains responsibility for ringtone (IN_CALL_SERVICE_RINGING not claimed).
        notifications.createNotificationChannel(NotificationChannel("incoming", "Llamadas entrantes", NotificationManager.IMPORTANCE_HIGH).apply { setSound(null,null); lockscreenVisibility=Notification.VISIBILITY_PRIVATE })
        notifications.createNotificationChannel(NotificationChannel("ongoing", "Llamadas en curso", NotificationManager.IMPORTANCE_LOW).apply { setSound(null,null); lockscreenVisibility=Notification.VISIBILITY_PRIVATE })
    }
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        store.calls[UUID.randomUUID().toString()]=call
        notificationIds[call]=nextId++
        val cb=object: Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) = updateAll()
            override fun onDetailsChanged(call: Call, details: Call.Details) = updateAll()
            override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: MutableList<Call>) = updateAll()
            override fun onParentChanged(call: Call, parent: Call?) = updateAll()
            override fun onChildrenChanged(call: Call, children: MutableList<Call>) = updateAll()
        }
        callbacks[call]=cb;call.registerCallback(cb, Handler(Looper.getMainLooper()))
        updateAll()
        if(call.state!=Call.STATE_RINGING) openUi(false)
    }
    override fun onCallRemoved(call: Call) {
        callbacks.remove(call)?.let { call.unregisterCallback(it) }
        notificationIds.remove(call)?.let { notifications.cancel(it) }
        store.calls.entries.removeAll { it.value==call };updateAll();super.onCallRemoved(call)
    }
    private fun updateAll() { store.refresh();store.calls.forEach { (id,call) -> notifyCall(id,call) } }
    private fun screenIntent(id: String, action: String?=null): PendingIntent {
        val intent=Intent(this,InCallActivity::class.java).setAction("call:$id:${action.orEmpty()}").putExtra("call_id",id).putExtra("call_action",action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(this, id.hashCode() xor (action?.hashCode() ?: 0), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    private fun actionIntent(id: String, action: String)=PendingIntent.getBroadcast(this,(id+action).hashCode(),Intent(this,CallActionReceiver::class.java).setAction(action).putExtra("call_id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun notifyCall(id: String, call: Call) {
        val notificationId=notificationIds[call] ?: return
        if(call.parent!=null || call.state==Call.STATE_DISCONNECTED) { notifications.cancel(notificationId);return }
        val ringing=call.state==Call.STATE_RINGING
        val live=store.state.value.calls.find { it.id==id } ?: return
        val person=Person.Builder().setName(live.name).setImportant(true).build()
        val style=if(ringing) NotificationCompat.CallStyle.forIncomingCall(person,actionIntent(id,"reject"),screenIntent(id,"answer")) else NotificationCompat.CallStyle.forOngoingCall(person,actionIntent(id,"end"))
        val builder=NotificationCompat.Builder(this,if(ringing) "incoming" else "ongoing")
            .setSmallIcon(R.drawable.ic_call).setContentTitle(live.name).setContentText(if(ringing) "Llamada entrante" else "Llamada en curso")
            .setCategory(NotificationCompat.CATEGORY_CALL).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(screenIntent(id))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setStyle(style)
        if(ringing && (Build.VERSION.SDK_INT<34 || notifications.canUseFullScreenIntent())) builder.setFullScreenIntent(screenIntent(id),true)
        if(live.connectedAt>0) builder.setWhen(live.connectedAt).setUsesChronometer(true)
        try { notifications.notify(notificationId,builder.build()) } catch(e: SecurityException) { store.error("Permite las notificaciones para mostrar las llamadas entrantes.") }
    }
    private fun openUi(dialpad: Boolean) { try { startActivity(Intent(this,InCallActivity::class.java).putExtra("dialpad",dialpad).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)) } catch(e: RuntimeException) { store.error("Abre la llamada desde su notificación.") } }
    override fun onBringToForeground(showDialpad: Boolean) { openUi(showDialpad) }
    override fun onCanAddCallChanged(canAddCall: Boolean) { store.refresh() }
    @Deprecated("Compatibility with Android 10–13")
    override fun onCallAudioStateChanged(state: CallAudioState) {
        if(Build.VERSION.SDK_INT>=34) return
        isMuted=state.isMuted
        val options=listOf(CallAudioState.ROUTE_EARPIECE to "Auricular",CallAudioState.ROUTE_SPEAKER to "Altavoz",CallAudioState.ROUTE_WIRED_HEADSET to "Auriculares",CallAudioState.ROUTE_BLUETOOTH to "Bluetooth")
        store.audio(state.isMuted, options.filter { state.supportedRouteMask and it.first != 0 }.map { AudioOutput(it.first.toString(),it.second) },state.route.toString())
    }
    override fun onAvailableCallEndpointsChanged(availableEndpoints: MutableList<CallEndpoint>) { endpoints=availableEndpoints;publishAudio() }
    override fun onCallEndpointChanged(endpoint: CallEndpoint) { if(Build.VERSION.SDK_INT>=34) { selectedEndpoint=endpoint.identifier.toString();publishAudio() } }
    override fun onMuteStateChanged(isMuted: Boolean) { this.isMuted=isMuted;publishAudio() }
    private fun publishAudio() { if(Build.VERSION.SDK_INT>=34) store.audio(isMuted,endpoints.map { AudioOutput(it.identifier.toString(), it.endpointName.toString()) },selectedEndpoint) }
    fun route(id: String) {
        if(Build.VERSION.SDK_INT>=34) {
            val endpoint=endpoints.find { it.identifier.toString()==id } ?: return
            requestCallEndpointChange(endpoint, mainExecutor, object: OutcomeReceiver<Void,CallEndpointException> {
                override fun onResult(result: Void?) {}
                override fun onError(error: CallEndpointException) { store.error("No se pudo cambiar la salida de audio.") }
            })
        } else { @Suppress("DEPRECATION") setAudioRoute(id.toInt()) }
    }
    override fun onDestroy() { callbacks.forEach { (call,cb) -> call.unregisterCallback(cb) }; notificationIds.values.forEach { notifications.cancel(it) };callbacks.clear();notificationIds.clear();store.reset();super.onDestroy() }
}
@AndroidEntryPoint class CallActionReceiver: BroadcastReceiver() {
    @Inject lateinit var store: CallStore
    override fun onReceive(context: Context, intent: Intent) { val id=intent.getStringExtra("call_id") ?: return; when(intent.action) { "reject" -> store.reject(id); "end" -> store.end(id) } }
}
