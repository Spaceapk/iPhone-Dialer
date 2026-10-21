package com.pablormlopez.iphonedialer.telephony
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
data class LiveCall(val id: String, val name: String, val address: String, val state: Int, val connectedAt: Long, val canHold: Boolean, val canMerge: Boolean, val canSwap: Boolean, val conference: Boolean)
data class AudioOutput(val id: String, val label: String)
data class LiveState(val calls: List<LiveCall> = emptyList(), val muted: Boolean=false, val outputs: List<AudioOutput> = emptyList(), val selectedOutput: String="", val canAdd: Boolean=false, val error: String?=null)
@Singleton class CallStore @Inject constructor() {
    private val mutable = MutableStateFlow(LiveState())
    val state = mutable.asStateFlow()
    internal var service: DialerInCallService? = null
    internal val calls = linkedMapOf<String, Call>()
    internal fun refresh() {
        val visible = calls.filterValues { it.parent == null }.map { (id, call) ->
            val d=call.details
            val visibleNumber=d.handlePresentation == android.telecom.TelecomManager.PRESENTATION_ALLOWED
            val number=if(visibleNumber) d.handle?.schemeSpecificPart.orEmpty() else "Número privado"
            LiveCall(id, if(d.callerDisplayNamePresentation == android.telecom.TelecomManager.PRESENTATION_ALLOWED) d.callerDisplayName?.toString().orEmpty().ifBlank { number.ifBlank { "Llamada" } } else number.ifBlank { "Llamada" }, number, call.state, d.connectTimeMillis,
                d.can(Call.Details.CAPABILITY_HOLD), d.can(Call.Details.CAPABILITY_MERGE_CONFERENCE) || call.conferenceableCalls.isNotEmpty(), d.can(Call.Details.CAPABILITY_SWAP_CONFERENCE), d.hasProperty(Call.Details.PROPERTY_CONFERENCE))
        }
        mutable.value = mutable.value.copy(calls=visible, canAdd=service?.canAddCall() == true)
    }
    internal fun audio(muted: Boolean, outputs: List<AudioOutput>, selected: String) { mutable.value=mutable.value.copy(muted=muted, outputs=outputs, selectedOutput=selected) }
    internal fun error(message: String?) { mutable.value=mutable.value.copy(error=message) }
    fun clearError()=error(null)
    private fun act(id: String, action: (Call)->Unit) { try { calls[id]?.let(action) ?: error("La llamada ya no está disponible.") } catch(e: RuntimeException) { error("Android no pudo completar la acción.") } }
    fun answer(id: String)=act(id) { it.answer(VideoProfile.STATE_AUDIO_ONLY) }
    fun reject(id: String)=act(id) { it.reject(false, null) }
    fun end(id: String)=act(id) { it.disconnect() }
    fun hold(id: String)=act(id) { if(it.state==Call.STATE_HOLDING) it.unhold() else if(it.details.can(Call.Details.CAPABILITY_HOLD)) it.hold() }
    fun merge(id: String)=act(id) { if(it.details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) it.mergeConference() else it.conferenceableCalls.firstOrNull()?.let { other -> it.conference(other) } }
    fun swap(id: String)=act(id) { if(it.details.can(Call.Details.CAPABILITY_SWAP_CONFERENCE)) it.swapConference() }
    fun tone(id: String, digit: Char)=act(id) { it.playDtmfTone(digit) }
    fun stopTone(id: String)=act(id) { it.stopDtmfTone() }
    fun mute() { service?.setMuted(!mutable.value.muted) }
    fun route(id: String) { service?.route(id) }
    internal fun reset() { calls.clear(); mutable.value=LiveState(); service=null }
}
