package com.pablormlopez.iphonedialer.ui
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract as CC
import android.telecom.Call
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablormlopez.iphonedialer.data.*
import com.pablormlopez.iphonedialer.domain.Search
import com.pablormlopez.iphonedialer.telephony.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
@AndroidEntryPoint class InCallActivity: ComponentActivity() {
    @Inject lateinit var store: CallStore
    @Inject lateinit var preferences: SettingsStore
    private var showDialpad by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);enableEdgeToEdge();handle(intent);setContent { val prefs by preferences.flow.collectAsStateWithLifecycle(Preferences());DialerTheme(prefs.copy(theme="dark")) { InCallScreen(store,showDialpad,{showDialpad=it},{finish()}) } } }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent);setIntent(intent);handle(intent) }
    private fun handle(intent: Intent) { if(intent.getBooleanExtra("dialpad",false)) showDialpad=true;if(intent.getStringExtra("call_action")=="answer") { intent.getStringExtra("call_id")?.let { store.answer(it) };intent.removeExtra("call_action") } }
}
@Composable fun InCallScreen(store: CallStore,showDialpad: Boolean,setDialpad: (Boolean)->Unit,onClose: ()->Unit) {
    val state by store.state.collectAsStateWithLifecycle();val context=LocalContext.current
    var selectedId by remember { mutableStateOf<String?>(null) };var audio by remember { mutableStateOf(false) };var hadCalls by remember { mutableStateOf(false) }
    val selected=state.calls.find { it.state==Call.STATE_RINGING } ?: state.calls.find { it.id==selectedId } ?: state.calls.find { it.state==Call.STATE_ACTIVE } ?: state.calls.firstOrNull()
    LaunchedEffect(state.calls.isEmpty()) { if(state.calls.isNotEmpty()) hadCalls=true else if(hadCalls) onClose() }
    Surface(color=MaterialTheme.colorScheme.background,modifier=Modifier.fillMaxSize()) {
        if(selected==null) Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) { Text("No hay llamadas activas",style=MaterialTheme.typography.headlineMedium);TextButton(onClick=onClose) { Text("Cerrar") } }
        else {
            val call=selected;val contact by produceState<Contact?>(null,call.address) { value=withContext(Dispatchers.IO) { runCatching { if(call.address.isBlank() || call.address=="Número privado") null else context.contentResolver.query(Uri.withAppendedPath(CC.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(call.address)),arrayOf(CC.PhoneLookup._ID,CC.PhoneLookup.LOOKUP_KEY,CC.PhoneLookup.DISPLAY_NAME,CC.PhoneLookup.PHOTO_URI),null,null,null)?.use { c -> if(c.moveToFirst()) Contact(c.getLong(0),c.getString(1).orEmpty(),c.getString(2).orEmpty(),listOf(call.address),false,c.getString(3)) else null } }.getOrNull() } }
            var now by remember { mutableLongStateOf(System.currentTimeMillis()) };LaunchedEffect(call.connectedAt) { while(true) { now=System.currentTimeMillis();delay(1000) } }
            Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)) {
                Text("iPhone Dialer",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Avatar(contact,contact?.name ?: call.name,104)
                Text(contact?.name ?: call.name,style=MaterialTheme.typography.headlineLarge)
                if(call.address.isNotBlank() && (contact!=null || call.name!=call.address)) Text(call.address,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(when(call.state) { Call.STATE_RINGING -> "Llamada entrante";Call.STATE_DIALING,Call.STATE_CONNECTING,Call.STATE_SELECT_PHONE_ACCOUNT -> "Conectando…";Call.STATE_ACTIVE -> if(call.connectedAt>0) Search.duration((now-call.connectedAt)/1000) else "En llamada";Call.STATE_HOLDING -> "En espera";Call.STATE_DISCONNECTING -> "Finalizando…";Call.STATE_DISCONNECTED -> "Llamada finalizada";else -> "Actualizando estado…" },style=MaterialTheme.typography.titleLarge)
                if(state.calls.size>1) state.calls.filter { it.id!=call.id }.forEach { other -> TextButton(onClick={selectedId=other.id}) { Text("${other.name} · ${if(other.state==Call.STATE_HOLDING) "En espera" else "Otra llamada"}") } }
                state.error?.let { error -> Text(error,color=MaterialTheme.colorScheme.error);TextButton(onClick=store::clearError) { Text("Entendido") } }
                if(call.state==Call.STATE_RINGING) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                        Button(onClick={store.reject(call.id)},colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error),modifier=Modifier.heightIn(min=64.dp)) { Icon(Icons.Default.CallEnd,null);Spacer(Modifier.width(8.dp));Text("Rechazar") }
                        Button(onClick={store.answer(call.id)},modifier=Modifier.heightIn(min=64.dp)) { Icon(Icons.Default.Call,null);Spacer(Modifier.width(8.dp));Text("Contestar") }
                    }
                } else {
                    val active=call.state==Call.STATE_ACTIVE
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { Control(Icons.Default.MicOff,"Silencio",selected=state.muted,onClick=store::mute);Control(Icons.Default.Dialpad,"Teclado",enabled=active,selected=showDialpad,onClick={setDialpad(!showDialpad)});Control(Icons.Default.VolumeUp,"Audio",enabled=state.outputs.isNotEmpty(),onClick={audio=true}) }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { Control(Icons.Default.Pause,if(call.state==Call.STATE_HOLDING) "Reanudar" else "Espera",enabled=call.canHold || call.state==Call.STATE_HOLDING,onClick={store.hold(call.id)});Control(Icons.Default.CallMerge,"Fusionar",enabled=call.canMerge,onClick={store.merge(call.id)});Control(Icons.Default.Add,"Añadir",enabled=state.canAdd,onClick={context.startActivity(Intent(context,MainActivity::class.java).setAction(Intent.ACTION_DIAL).setData(Uri.fromParts("tel","",null)))}) }
                    if(call.canSwap) TextButton(onClick={store.swap(call.id)}) { Text("Alternar conferencia") }
                    if(showDialpad && active) DtmfPad(store,call.id)
                    Button(onClick={store.end(call.id)},shape=CircleShape,colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error),modifier=Modifier.size(76.dp),contentPadding=PaddingValues(0.dp)) { Icon(Icons.Default.CallEnd,"Finalizar llamada",modifier=Modifier.size(32.dp)) }
                }
            }
        }
    }
    if(audio) AlertDialog(onDismissRequest={audio=false},title={Text("Salida de audio")},text={Column { state.outputs.forEach { output -> TextButton(onClick={store.route(output.id);audio=false}) { Text("${if(state.selectedOutput==output.id) "✓ " else ""}${output.label}") } } }},confirmButton={TextButton(onClick={audio=false}) { Text("Cerrar") }})
}
@Composable private fun DtmfPad(store: CallStore,id: String) {
    val scope=rememberCoroutineScope()
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) { "123456789*0#".toList().chunked(3).forEach { row -> Row(horizontalArrangement=Arrangement.spacedBy(24.dp)) { row.forEach { digit ->
        Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant,CircleShape).semantics { role=Role.Button;contentDescription="Tono $digit";onClick { scope.launch { store.tone(id,digit);try { delay(180) } finally { store.stopTone(id) } };true } }.pointerInput(id,digit) { detectTapGestures(onPress={store.tone(id,digit);try { tryAwaitRelease() } finally { store.stopTone(id) }}) },contentAlignment=Alignment.Center) { Text(digit.toString(),fontSize=26.sp) }
    } } } }
}
