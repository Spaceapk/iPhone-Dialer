package com.pablormlopez.iphonedialer.telephony
import android.app.*
import android.content.*
import android.os.*
import android.telecom.*
import com.pablormlopez.iphonedialer.ui.InCallActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
@AndroidEntryPoint class DialerInCallService: InCallService() {
    @Inject lateinit var store: CallStore
    @Inject lateinit var notificationFactory: CallNotificationFactory
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
    private fun notifyCall(id: String, call: Call) {
        val notificationId = notificationIds[call] ?: return
        if (call.parent != null || call.state == Call.STATE_DISCONNECTED) {
            notifications.cancel(notificationId)
            return
        }
        val live = store.state.value.calls.find { it.id == id } ?: return
        try {
            val fullScreen = call.state == Call.STATE_RINGING &&
                (Build.VERSION.SDK_INT < 34 || notifications.canUseFullScreenIntent())
            notifications.notify(notificationId, notificationFactory.build(live, fullScreen))
        } catch (e: SecurityException) {
            com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics.record(this, "notification.permission", e)
            store.error("Permite las notificaciones para mostrar las llamadas entrantes.")
        } catch (e: IllegalArgumentException) {
            com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics.record(this, "notification.rejected", e)
            store.error("Android rechazó el aviso de llamada. Hay un informe técnico local en Configuración.")
        }
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
