package com.pablormlopez.iphonedialer.ui
import android.Manifest
import android.os.Build
import android.provider.CallLog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablormlopez.iphonedialer.data.*
import com.pablormlopez.iphonedialer.domain.Search
import java.text.DateFormat
import java.util.Date
@Composable fun RecentScreen(recents: List<Recent>,query: String,onQuery: (String)->Unit,onCall: (String)->Unit,onAdd: (String)->Unit,onDelete: (Set<Long>)->Unit) {
    var type by rememberSaveable { mutableIntStateOf(0) };var selectionMode by rememberSaveable { mutableStateOf(false) };var selected by remember { mutableStateOf<Set<Long>>(emptySet()) };var detail by remember { mutableStateOf<Recent?>(null) }
    val types=listOf(0 to "Todas",CallLog.Calls.MISSED_TYPE to "Perdidas",CallLog.Calls.INCOMING_TYPE to "Entrantes",CallLog.Calls.OUTGOING_TYPE to "Salientes",CallLog.Calls.REJECTED_TYPE to "Rechazadas")
    val filtered=remember(recents,query,type) { recents.filter { (type==0 || it.type==type) && (it.number.contains(query) || it.name.orEmpty().contains(query,true)) } }
    val dateFormat=remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    val groups=remember(filtered,selectionMode) { if(selectionMode) filtered.map { listOf(it) } else filtered.groupBy { Triple(it.number, dateFormat.format(Date(it.date)), it.type) }.values.toList() }
    Column {
        OutlinedTextField(query,onQuery,modifier=Modifier.fillMaxWidth(),singleLine=true,placeholder={Text("Buscar en el historial")},leadingIcon={Icon(Icons.Default.Search,null)})
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { types.forEach { (t,label) -> FilterChip(selected=type==t,onClick={type=t},label={Text(label)}) } }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("${filtered.size} registros",modifier=Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
            TextButton(onClick={selectionMode=!selectionMode;selected=emptySet()}) { Text(if(selectionMode) "Cancelar" else "Seleccionar") }
            if(selected.isNotEmpty()) IconButton(onClick={onDelete(selected);selected=emptySet();selectionMode=false}) { Icon(Icons.Default.Delete,"Eliminar selección") }
        }
        if(filtered.isEmpty()) EmptyPanel("Sin llamadas","Las llamadas reales aparecerán aquí cuando Android las registre.")
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=24.dp)) { items(groups,key={it.first().id}) { group -> val item=group.first();val allowed=item.presentation==android.telecom.TelecomManager.PRESENTATION_ALLOWED && item.number.isNotBlank()
            Row(Modifier.fillMaxWidth().clickable { if(selectionMode) selected=if(item.id in selected) selected-item.id else selected+item.id else if(allowed) onCall(item.number) else detail=item }.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                if(selectionMode) Checkbox(checked=item.id in selected,onCheckedChange={selected=if(it) selected+item.id else selected-item.id})
                Icon(when(item.type) { CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade;CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed;else -> Icons.Default.CallReceived },contentDescription=callType(item.type),tint=if(item.type==CallLog.Calls.MISSED_TYPE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text(if(allowed) item.name ?: item.number else "Número privado",style=MaterialTheme.typography.titleMedium);Text("${callType(item.type)}${if(group.size>1) " · ${group.size} llamadas" else ""}",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(item.date)),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick={detail=item}) { Icon(Icons.Default.Info,"Detalles de la llamada") }
            };HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.4f))
        } }
    }
    detail?.let { item -> AlertDialog(onDismissRequest={detail=null},title={Text(if(item.presentation==android.telecom.TelecomManager.PRESENTATION_ALLOWED) item.name ?: item.number.ifBlank { "Número desconocido" } else "Número privado")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(callType(item.type));Text(DateFormat.getDateTimeInstance().format(Date(item.date)));Text("Duración: ${Search.duration(item.duration)}")
        if(item.presentation==android.telecom.TelecomManager.PRESENTATION_ALLOWED && item.number.isNotBlank()) { TextButton(onClick={detail=null;onAdd(item.number)}) { Text("Crear contacto") };TextButton(onClick={detail=null;onCall(item.number)}) { Text("Devolver llamada") } }
        TextButton(onClick={detail=null;onDelete(setOf(item.id))}) { Text("Eliminar registro") }
    }},confirmButton={TextButton(onClick={detail=null}) { Text("Cerrar") }}) }
}
private fun callType(type: Int)=when(type) { CallLog.Calls.INCOMING_TYPE -> "Entrante";CallLog.Calls.OUTGOING_TYPE -> "Saliente";CallLog.Calls.MISSED_TYPE -> "Perdida";CallLog.Calls.REJECTED_TYPE -> "Rechazada";CallLog.Calls.BLOCKED_TYPE -> "Bloqueada";CallLog.Calls.VOICEMAIL_TYPE -> "Buzón";else -> "Otra llamada" }
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ContactSheet(contact: Contact,vm: MainViewModel,onDismiss: ()->Unit,onCall: (String)->Unit,onEdit: ()->Unit,onFavorite: ()->Unit,onDelete: ()->Unit,onShare: ()->Unit,onShortcut: ()->Unit) {
    var speed by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest=onDismiss) { Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Avatar(contact,contact.name,84);Text(contact.name,style=MaterialTheme.typography.headlineMedium)
        Row { IconButton(onClick=onFavorite) { Icon(if(contact.starred) Icons.Default.Star else Icons.Default.StarBorder,if(contact.starred) "Quitar favorito" else "Añadir favorito") };IconButton(onClick=onEdit) { Icon(Icons.Default.Edit,"Editar contacto") };IconButton(onClick=onShare) { Icon(Icons.Default.Share,"Compartir vCard") } }
        contact.phones.forEach { n -> GroupedPanel(modifier=Modifier.fillMaxWidth()) { Text(n,style=MaterialTheme.typography.titleLarge);Row { TextButton(onClick={onCall(n)}) { Text("Llamar") };TextButton(onClick={speed=n}) { Text("Marcación rápida") } } } }
        TextButton(onClick=onShortcut) { Text("Añadir acceso a inicio") }
        Text("La edición se realiza en el editor autorizado de Android: nombres, fotos, múltiples teléfonos, correos, direcciones y cuenta.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick=onDelete) { Text("Eliminar contacto",color=MaterialTheme.colorScheme.error) };Spacer(Modifier.height(20.dp))
    } }
    speed?.let { number -> AlertDialog(onDismissRequest={speed=null},title={Text("Asignar tecla rápida")},text={Column { (2..9).chunked(4).forEach { slots -> Row { slots.forEach { slot -> TextButton(onClick={vm.work { vm.speeds.save(SpeedDial(slot,contact.name,number));vm.message.value="Tecla $slot asignada." };speed=null}) { Text(slot.toString()) } } } };Text("Sustituye la asignación anterior de esa tecla.",style=MaterialTheme.typography.bodyMedium) }},confirmButton={TextButton(onClick={speed=null}) { Text("Cancelar") }}) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsSheet(vm: MainViewModel,prefs: Preferences,roleHeld: Boolean,onDismiss: ()->Unit,onPermission: (String,String)->Unit,onExport: ()->Unit,onImport: ()->Unit,onFullscreen: ()->Unit,onConfirm: (String,()->Unit)->Unit) {
    val context=androidx.compose.ui.platform.LocalContext.current
    val technicalReport by vm.diagnosticReport.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadDiagnostics() }
    val blocked by vm.blocked.collectAsStateWithLifecycle();val contacts by vm.contacts.collectAsStateWithLifecycle();val speedDials by vm.speedDials.collectAsStateWithLifecycle()
    var number by rememberSaveable { mutableStateOf("") };var showBlocked by rememberSaveable { mutableStateOf(false) };var showDuplicates by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest=onDismiss) { Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("A tu manera",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
        Text("Apariencia",style=MaterialTheme.typography.titleLarge)
        Text("Estilo clásico: fondos sólidos, sin vidrio, desenfoque ni transparencias.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("system" to "Sistema","light" to "Claro","dark" to "Oscuro").forEach { (key,name) -> FilterChip(selected=prefs.theme==key,onClick={vm.work { vm.preferences.set("theme",key) }},label={Text(name)}) } }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("green" to "Verde","blue" to "Azul","violet" to "Violeta").forEach { (key,name) -> FilterChip(selected=prefs.accent==key,onClick={vm.work { vm.preferences.set("accent",key) }},label={Text(name)}) } }
        SettingToggle("Animaciones entre pestañas",prefs.animations) { vm.work { vm.preferences.set("animations",it) } }
        SettingToggle("Vibración del teclado",prefs.haptics) { vm.work { vm.preferences.set("haptics",it) } }
        Text("Tamaño de texto: ${(prefs.textScale*100).toInt()} % (además del ajuste del sistema)")
        Slider(value=prefs.textScale,onValueChange={vm.work { vm.preferences.scale(it) }},valueRange=1f..1.3f,steps=2)
        Text("Pantalla inicial",style=MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Favoritos","Recientes","Contactos","Teclado","Buzón").forEachIndexed { i,name -> FilterChip(selected=prefs.start==i,onClick={vm.work { vm.preferences.start(i) }},label={Text(name)}) } }
        HorizontalDivider();Text("Llamadas y SIM",style=MaterialTheme.typography.titleLarge)
        Text(if(roleHeld) "Marcador predeterminado activo" else "Marcador predeterminado no activado")
        TextButton(onClick={onPermission(Manifest.permission.READ_PHONE_STATE,"Permite mostrar las cuentas SIM disponibles antes de llamar. Los números de teléfono de las SIM no se solicitan.")}) { Text("Habilitar selección de SIM") }
        Text("Sin este permiso, Android usa su selección y preferencias de SIM. Con permiso, se pregunta al llamar si hay varias cuentas.",style=MaterialTheme.typography.bodyMedium)
        if(Build.VERSION.SDK_INT>=33) TextButton(onClick={onPermission(Manifest.permission.POST_NOTIFICATIONS,"Permite avisarte de llamadas entrantes y mostrar los controles de llamada.")}) { Text("Permitir notificaciones de llamadas") }
        if(Build.VERSION.SDK_INT>=34) TextButton(onClick=onFullscreen) { Text("Permitir pantalla completa de llamadas") }
        TextButton(onClick={showBlocked=!showBlocked;if(showBlocked) vm.loadBlocked()}) { Text("Números bloqueados") }
        if(showBlocked) {
            Text("Bloqueo del proveedor oficial de Android. Solo se modifica tras confirmación; depende del rol y del perfil del dispositivo.",style=MaterialTheme.typography.bodyMedium)
            OutlinedTextField(number,{number=it},label={Text("Número para bloquear")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            TextButton(enabled=roleHeld && Search.validDialString(number),onClick={onConfirm("¿Bloquear $number en el proveedor del sistema?") { vm.work { vm.system.block(number);number="";vm.loadBlocked() } }}) { Text("Bloquear número") }
            blocked.forEach { b -> Row(verticalAlignment=Alignment.CenterVertically) { Text(b.number,modifier=Modifier.weight(1f));TextButton(onClick={onConfirm("¿Desbloquear ${b.number}?") { vm.work { vm.system.unblock(b.id);vm.loadBlocked() } }}) { Text("Desbloquear") } } }
        }
        HorizontalDivider();Text("Agenda",style=MaterialTheme.typography.titleLarge)
        TextButton(onClick=onImport) { Text("Importar VCF con la app de contactos") };TextButton(onClick=onExport) { Text("Exportar agenda a VCF") }
        TextButton(onClick={showDuplicates=!showDuplicates}) { Text("Revisar posibles duplicados") }
        if(showDuplicates) {
            val groups=remember(contacts) { contacts.flatMap { c -> c.phones.map { Search.digits(it) to c } }.filter { it.first.isNotBlank() }.groupBy({it.first},{it.second}).values.map { it.distinctBy { c -> c.id } }.filter { it.size>1 } }
            Text("${groups.size} grupos con números coincidentes. No se combinan automáticamente; revísalos en el editor de contactos.",style=MaterialTheme.typography.bodyMedium)
            groups.take(30).forEach { group -> Text(group.joinToString(" · ") { it.name },style=MaterialTheme.typography.bodyMedium) }
        }
        if(speedDials.isNotEmpty()) { Text("Marcación rápida",style=MaterialTheme.typography.titleMedium);speedDials.forEach { s -> Row(verticalAlignment=Alignment.CenterVertically) { Text("${s.slot} · ${s.name}",modifier=Modifier.weight(1f));IconButton(onClick={onConfirm("¿Quitar la asignación de la tecla ${s.slot}?") { vm.work { vm.speeds.remove(s.slot) } }}) { Icon(Icons.Default.Delete,"Quitar marcación rápida") } } } }
        HorizontalDivider();Text("Informe técnico",style=MaterialTheme.typography.titleLarge)
        Text(if(technicalReport==null) "No hay errores registrados en esta instalación." else "Hay un informe local sin contactos ni números. Solo se comparte si lo decides.",style=MaterialTheme.typography.bodyMedium)
        if(technicalReport!=null) {
            TextButton(onClick={
                runCatching { context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT,technicalReport),"Compartir informe técnico")) }.onFailure { vm.message.value="No se pudo abrir el selector para compartir." }
            }) { Text("Compartir informe técnico") }
            TextButton(onClick={onConfirm("¿Borrar el informe técnico local?") { vm.clearDiagnostics() }}) { Text("Borrar informe") }
        }
        HorizontalDivider();Text("Privacidad",style=MaterialTheme.typography.titleLarge)
        Text("Sin anuncios, analítica, rastreadores ni permiso de Internet. La agenda y el historial se consultan en los proveedores de Android. Las preferencias y teclas rápidas permanecen en el almacenamiento privado de la aplicación. La agenda puede sincronizarse por las cuentas ya configuradas en Android. Exportar o compartir transmite datos solo al destino que elijas. No se graba audio ni se transcriben llamadas.")
        Text("${com.pablormlopez.iphonedialer.BuildConfig.VERSION_NAME} · Versión de evaluación",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}
@Composable private fun SettingToggle(label: String,checked: Boolean,onChange: (Boolean)->Unit) { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text(label,modifier=Modifier.weight(1f));Switch(checked,onChange) } }
