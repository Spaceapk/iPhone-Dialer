package com.pablormlopez.iphonedialer.ui
import android.Manifest
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon as AndroidIcon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract as CC
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import com.pablormlopez.iphonedialer.R
import com.pablormlopez.iphonedialer.data.*
import com.pablormlopez.iphonedialer.domain.Search
import com.pablormlopez.iphonedialer.telephony.SimAccount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.text.DateFormat
import java.util.Date
@AndroidEntryPoint class MainActivity: ComponentActivity() {
    private val vm: MainViewModel by viewModels()
    private var externalNumber by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);enableEdgeToEdge();handle(intent);setContent { val prefs by vm.prefs.collectAsStateWithLifecycle();DialerTheme(prefs) { MainScreen(vm,externalNumber) { externalNumber=null } } } }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent);setIntent(intent);handle(intent) }
    private fun handle(intent: Intent) { if(intent.data?.scheme=="tel") externalNumber=intent.data?.schemeSpecificPart?.take(128) }
}
private data class PermissionPrompt(val permission: String,val reason: String,val action: ()->Unit)
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MainScreen(vm: MainViewModel, externalNumber: String?, consumed: ()->Unit) {
    val context=LocalContext.current;val lifecycle=androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val prefs by vm.prefs.collectAsStateWithLifecycle();val contacts by vm.contacts.collectAsStateWithLifecycle();val recents by vm.recents.collectAsStateWithLifecycle();val speeds by vm.speedDials.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(-1) };var number by rememberSaveable { mutableStateOf("") };var query by rememberSaveable { mutableStateOf("") }
    var roleHeld by remember { mutableStateOf(vm.phone.isDefault()) };var revision by remember { mutableIntStateOf(0) }
    var settings by rememberSaveable { mutableStateOf(false) };var selected by remember { mutableStateOf<Contact?>(null) };var chooseNumber by remember { mutableStateOf<Contact?>(null) }
    var permissionPrompt by remember { mutableStateOf<PermissionPrompt?>(null) };var permissionAction by remember { mutableStateOf<()->Unit>({}) }
    var confirm by remember { mutableStateOf<Pair<String,()->Unit>?>(null) };var accounts by remember { mutableStateOf<List<SimAccount>>(emptyList()) };var simForNumber by remember { mutableStateOf<String?>(null) }
    var showSimChooser by remember { mutableStateOf(false) }
    var saveNumber by rememberSaveable { mutableStateOf<String?>(null) }
    val message by vm.message.collectAsStateWithLifecycle();val snackbar=remember { SnackbarHostState() }
    val permissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> revision++;vm.refresh();if(granted) permissionAction() else vm.message.value="Permiso denegado. Puedes seguir usando las demás funciones.";permissionAction={} }
    fun request(p: String,reason: String,action: ()->Unit) { if(vm.has(p)) action() else permissionPrompt=PermissionPrompt(p,reason,action) }
    val roleLauncher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { roleHeld=vm.phone.isDefault();revision++;vm.refresh() }
    fun requestRole() { val rm=context.getSystemService(RoleManager::class.java);if(rm.isRoleAvailable(RoleManager.ROLE_DIALER)) roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER)) else vm.message.value="Este dispositivo no ofrece el rol de teléfono." }
    val contactEditor=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.refresh() }
    fun editNumber(n: String, existing: Boolean) {
        val intent=if(existing) Intent(Intent.ACTION_INSERT_OR_EDIT).setType(CC.Contacts.CONTENT_ITEM_TYPE) else Intent(Intent.ACTION_INSERT,CC.Contacts.CONTENT_URI)
        try { contactEditor.launch(intent.putExtra(CC.Intents.Insert.PHONE,n).putExtra("finishActivityOnSaveCompleted",true)) } catch(e: RuntimeException) { vm.message.value="No hay un editor de contactos disponible." }
    }
    fun copyNumber(n: String) { context.getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Número",n));vm.message.value="Número copiado." }
    fun safeIntent(intent: Intent) { try { context.startActivity(intent) } catch(e: RuntimeException) { vm.message.value="No hay una aplicación del sistema disponible para esta acción." } }
    fun call(n: String) {
        if(!roleHeld) { vm.message.value="Elige iPhone Dialer como teléfono predeterminado y vuelve a tocar Llamar.";requestRole();return }
        request(Manifest.permission.CALL_PHONE,"Permite iniciar la llamada solicitada mediante el servicio telefónico de Android.") {
            if(!Search.validDialString(n)) vm.message.value="Introduce un número válido." else if(vm.has(Manifest.permission.READ_PHONE_STATE)) {
                runCatching { vm.phone.accounts() }.onSuccess { list -> if(list.size>1) { accounts=list;simForNumber=n;showSimChooser=true } else runCatching { vm.phone.place(n) }.onFailure { vm.message.value=it.message } }.onFailure { vm.message.value="No se pudieron leer las cuentas telefónicas." }
            } else runCatching { vm.phone.place(n) }.onFailure { vm.message.value=it.message }
        }
    }
    fun callContact(c: Contact) { when(c.phones.size) { 0 -> { selected=c };1 -> call(c.phones.first());else -> chooseNumber=c } }
    val exportLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-vcard")) { uri -> if(uri!=null) vm.work { vm.system.exportVcf(uri,vm.system.contacts());vm.message.value="Contactos exportados al archivo elegido." } }
    val importLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if(uri!=null) safeIntent(Intent(Intent.ACTION_VIEW).setDataAndType(uri,"text/x-vcard").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
    DisposableEffect(lifecycle) { val observer=LifecycleEventObserver { _,event -> if(event==Lifecycle.Event.ON_RESUME) { roleHeld=vm.phone.isDefault();if(!vm.has(Manifest.permission.READ_CONTACTS)) { selected=null;chooseNumber=null };revision++;vm.refresh() } };lifecycle.addObserver(observer);onDispose { lifecycle.removeObserver(observer) } }
    LaunchedEffect(Unit) { val initial=vm.preferences.flow.first();if(tab<0) tab=initial.start }
    LaunchedEffect(externalNumber) { if(externalNumber!=null) { number=externalNumber;tab=3;consumed() } }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it);vm.message.value=null } }
    val names=listOf("Favoritos","Recientes","Contactos","Teclado","Buzón")
    val icons=listOf(Icons.Default.Star,Icons.Default.History,Icons.Default.Contacts,Icons.Default.Dialpad,Icons.Default.Voicemail)
    val filtered=remember(contacts,query) { contacts.filter { Search.matches(it.name,it.phones,query) } }
    Scaffold(containerColor=MaterialTheme.colorScheme.background,snackbarHost={SnackbarHost(snackbar)},bottomBar={
        NavigationBar(containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp,modifier=Modifier.height(72.dp)) { names.forEachIndexed { i,label -> NavigationBarItem(selected=tab==i,onClick={tab=i;query=""},icon={Icon(icons[i],contentDescription=null)},label={Text(label,fontSize=12.sp,maxLines=1)},colors=NavigationBarItemDefaults.colors(indicatorColor=Color.Transparent,selectedIconColor=MaterialTheme.colorScheme.primary,selectedTextColor=MaterialTheme.colorScheme.primary,unselectedIconColor=MaterialTheme.colorScheme.onSurfaceVariant,unselectedTextColor=MaterialTheme.colorScheme.onSurfaceVariant)) } }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.widthIn(max=760.dp).fillMaxWidth().align(Alignment.TopCenter).padding(horizontal=20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top=20.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(names.getOrElse(tab){"Teléfono"},fontSize=34.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                    if(tab==2) IconButton(onClick={safeIntent(Intent(Intent.ACTION_INSERT,CC.Contacts.CONTENT_URI))}) { Icon(Icons.Default.Add,"Crear contacto") }
                    IconButton(onClick={settings=true}) { Icon(Icons.Default.Settings,"Configuración") }
                }
                if(!roleHeld) { GroupedPanel(Modifier.fillMaxWidth().padding(bottom=16.dp)) { Text("Tu teléfono, a tu manera",style=MaterialTheme.typography.titleMedium);Text("Activa el marcador predeterminado para gestionar llamadas reales.");Button(onClick=::requestRole) { Text("Usar como teléfono") } } }
                val content: @Composable (Int)->Unit = { current ->
                    Column(Modifier.fillMaxSize()) {
                    when(current) {
                        0,2 -> {
                            if(!vm.has(Manifest.permission.READ_CONTACTS)) EmptyPanel("Tu agenda permanece privada","Permite leer los contactos solo para mostrarlos en esta pantalla.","Mostrar contactos") { request(Manifest.permission.READ_CONTACTS,"La agenda se lee localmente y no se envía a servidores.") { vm.refresh() } }
                            else {
                                OutlinedTextField(query,{query=it},placeholder={Text("Buscar nombre o número")},leadingIcon={Icon(Icons.Default.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true)
                                Spacer(Modifier.height(12.dp))
                                val list=if(current==0) filtered.filter { it.starred } else filtered
                                if(list.isEmpty()) EmptyPanel(if(current==0) "Tus personas favoritas" else "Sin contactos","${if(current==0) "Abre un contacto y toca la estrella para añadirlo." else "Crea un contacto o modifica la búsqueda."}")
                                LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=24.dp)) {
                                    list.groupBy { it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }.forEach { (letter,items) ->
                                        item("header$letter") { Text(letter,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=14.dp)) }
                                        items(items,key={it.id}) { c -> Row(Modifier.fillMaxWidth().clickable { if(current==0) callContact(c) else selected=c }.padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
                                            Avatar(c,c.name);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)) { Text(c.name,style=MaterialTheme.typography.titleMedium);Text(c.phones.firstOrNull() ?: "Sin teléfono",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium) }
                                            IconButton(onClick={if(current==0) selected=c else callContact(c)}) { Icon(if(current==0) Icons.Default.Info else Icons.Default.Call,if(current==0) "Detalles" else "Llamar",tint=MaterialTheme.colorScheme.primary) }
                                        };HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.35f)) }
                                    }
                                }
                            }
                        }
                        1 -> if(!roleHeld) EmptyPanel("Historial protegido","Activa primero el rol de teléfono predeterminado.") else if(!vm.has(Manifest.permission.READ_CALL_LOG)) EmptyPanel("Tu historial, solo aquí","Se necesita permiso para consultar las llamadas registradas por Android.","Ver historial") { request(Manifest.permission.READ_CALL_LOG,"Permite mostrar el historial local de llamadas. No se envía fuera del dispositivo.") { vm.refresh() } } else RecentScreen(recents,query,{query=it},::call,{ n ->saveNumber=n }) { ids -> confirm="¿Eliminar ${ids.size} registros? Esta acción modifica el historial del sistema." to { request(Manifest.permission.WRITE_CALL_LOG,"Permite eliminar únicamente los registros que acabas de seleccionar.") { vm.work { vm.system.deleteRecents(ids);vm.refresh() } } } }
                        3 -> KeypadScreen(number,{number=it},prefs,speeds,contacts,::call,::callContact,onSave={saveNumber=it},onCopy=::copyNumber)
                        4 -> EmptyPanel("Buzón del operador","Accede al servicio telefónico de tu operador. El buzón visual y las transcripciones no están habilitados: requieren soporte e integración específicos.","Llamar al buzón") {
                            if(!roleHeld) requestRole() else request(Manifest.permission.CALL_PHONE,"Permite llamar al buzón del operador. Pueden aplicarse tarifas del servicio.") { runCatching { vm.phone.voicemail() }.onFailure { vm.message.value="El operador o dispositivo no permite abrir el buzón por este método." } }
                        }
                    }
                    }
                }
                if(prefs.animations) Crossfade(targetState=tab,label="Navegación") { content(it) } else content(tab)
            }
        }
    }
    permissionPrompt?.let { prompt -> ConfirmDialog("Permiso necesario",prompt.reason,"Continuar",{permissionPrompt=null}) { permissionAction=prompt.action;permissionLauncher.launch(prompt.permission) } }
    confirm?.let { item -> ConfirmDialog("Confirmar acción",item.first,"Confirmar",{confirm=null},item.second) }
    chooseNumber?.let { c -> AlertDialog(onDismissRequest={chooseNumber=null},title={Text(c.name)},text={Column { c.phones.forEach { n -> TextButton(onClick={chooseNumber=null;call(n)}) { Text(n) } } }},confirmButton={TextButton(onClick={chooseNumber=null}) { Text("Cerrar") }}) }
    if(showSimChooser) AlertDialog(onDismissRequest={showSimChooser=false;simForNumber=null},title={Text("Elegir SIM")},text={Column { accounts.forEach { a -> TextButton(onClick={showSimChooser=false;simForNumber?.let { n -> runCatching { vm.phone.place(n,a.handle) }.onFailure { vm.message.value=it.message } };simForNumber=null}) { Text(a.label) } };TextButton(onClick={showSimChooser=false;simForNumber?.let { n -> runCatching { vm.phone.place(n) }.onFailure { vm.message.value=it.message } };simForNumber=null}) { Text("Preferencia del sistema") } }},confirmButton={TextButton(onClick={showSimChooser=false}) { Text("Cancelar") }})
    selected?.let { contact ->
        ContactSheet(contact,vm,onDismiss={selected=null},onCall=::call,onEdit={safeIntent(Intent(Intent.ACTION_EDIT,CC.Contacts.getLookupUri(contact.id,contact.lookup)).putExtra("finishActivityOnSaveCompleted",true));selected=null},onFavorite={request(Manifest.permission.WRITE_CONTACTS,"Permite cambiar la estrella de este contacto.") { vm.work { vm.system.favorite(contact);vm.refresh();selected=null } }},onDelete={confirm="¿Eliminar a ${contact.name}? Se modificará la agenda y puede sincronizarse con su cuenta." to { request(Manifest.permission.WRITE_CONTACTS,"Permite eliminar el contacto confirmado de la agenda del sistema.") { vm.work { vm.system.deleteContact(contact);vm.refresh();selected=null } } }},onShare={
            val uri=Uri.withAppendedPath(CC.Contacts.CONTENT_VCARD_URI,Uri.encode(contact.lookup))
            safeIntent(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/x-vcard").putExtra(Intent.EXTRA_STREAM,uri).apply { clipData=ClipData.newRawUri("Contacto",uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) },"Compartir contacto"))
        },onShortcut={val manager=context.getSystemService(ShortcutManager::class.java);if(manager.isRequestPinShortcutSupported && contact.phones.isNotEmpty()) manager.requestPinShortcut(ShortcutInfo.Builder(context,"contact_${contact.id}").setShortLabel(contact.name.take(30)).setIcon(AndroidIcon.createWithResource(context,R.drawable.ic_launcher)).setIntent(Intent(context,MainActivity::class.java).setAction(Intent.ACTION_DIAL).setData(Uri.fromParts("tel",contact.phones.first(),null))).build(),null) else vm.message.value="Este lanzador no admite el acceso directo." })
    }
    saveNumber?.let { n -> AlertDialog(onDismissRequest={saveNumber=null},title={Text("Guardar número")},text={Column {
        if(contacts.any { c -> c.phones.any { Search.digits(n).isNotEmpty() && Search.digits(it)==Search.digits(n) } }) Text("Este número ya aparece en tu agenda. Puedes guardarlo igualmente.")
        TextButton(onClick={saveNumber=null;editNumber(n,false)}) { Text("Crear contacto nuevo") }
        TextButton(onClick={saveNumber=null;editNumber(n,true)}) { Text("Añadir a contacto existente") }
        TextButton(onClick={copyNumber(n);saveNumber=null}) { Text("Copiar número") }
    }},confirmButton={TextButton(onClick={saveNumber=null}) { Text("Cancelar") }}) }
    if(settings) SettingsSheet(vm,prefs,roleHeld,{settings=false},onPermission={p,reason -> request(p,reason) { revision++ } },onExport={if(vm.has(Manifest.permission.READ_CONTACTS)) exportLauncher.launch("contactos.vcf") else request(Manifest.permission.READ_CONTACTS,"Lee tu agenda para exportarla al archivo que elijas.") { exportLauncher.launch("contactos.vcf") }},onImport={importLauncher.launch(arrayOf("text/x-vcard","text/vcard","application/octet-stream"))},onFullscreen={if(Build.VERSION.SDK_INT>=34) safeIntent(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:${context.packageName}")))},onConfirm={text,act -> confirm=text to act})
}
@OptIn(ExperimentalFoundationApi::class)
@Composable fun KeypadScreen(number: String,setNumber: (String)->Unit,prefs: Preferences,speeds: List<SpeedDial>,contacts: List<Contact>,onCall: (String)->Unit,onContact: (Contact)->Unit,onSave: (String)->Unit={},onCopy: (String)->Unit={}) {
    val view=LocalView.current
    val fontScale=LocalDensity.current.fontScale
    val matches=remember(number,contacts) { if(number.isBlank()) emptyList() else contacts.filter { Search.matches(it.name,it.phones,number) }.take(2) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Altura de referencia estable: cambios transitorios de la ventana (p. ej. el IME
        // al volver de otra pestaña) no deben redimensionar ni desplazar las teclas.
        var stableMaxHeight by remember { mutableFloatStateOf(0f) }
        if(maxHeight.value>stableMaxHeight) stableMaxHeight=maxHeight.value
        val layoutHeight=if(stableMaxHeight>0f) stableMaxHeight else maxHeight.value
        val keySize=((layoutHeight-240f)/4f).coerceIn(if(fontScale>1.15f) 64f else 56f,76f)
        Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
                if(number.isNotBlank()) Column(Modifier.height(96.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
                    matches.forEach { c -> TextButton(onClick={onContact(c)}) { Text(c.name,maxLines=1) } }
                }
                Row(Modifier.fillMaxWidth().padding(vertical=12.dp).height(52.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(number.ifBlank { "Introduce un número" },fontSize=when { number.isBlank() -> 20.sp;number.length>20 -> 20.sp;number.length>14 -> 24.sp;else -> 30.sp },fontWeight=FontWeight.Medium,maxLines=1,softWrap=false,overflow=androidx.compose.ui.text.style.TextOverflow.Clip,modifier=Modifier.weight(1f),color=if(number.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    Box(Modifier.size(48.dp).combinedClickable(enabled=number.isNotEmpty(),hapticFeedbackEnabled=false,onClick={setNumber(number.dropLast(1))},onLongClick={setNumber("")}),contentAlignment=Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Backspace,"Borrar dígito") }
                }
                if(number.isNotBlank()) Row(verticalAlignment=Alignment.CenterVertically) {
                    TextButton(onClick={onSave(number)}) { Text("Guardar número") }
                    IconButton(onClick={onCopy(number)}) { Icon(Icons.Default.ContentCopy,"Copiar número") }
                }
                val rows=listOf(listOf("1" to "", "2" to "ABC","3" to "DEF"),listOf("4" to "GHI","5" to "JKL","6" to "MNO"),listOf("7" to "PQRS","8" to "TUV","9" to "WXYZ"),listOf("*" to "","0" to "+","#" to ""))
                rows.forEach { row -> Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceEvenly) { row.forEach { (digit,letters) ->
                    Column(Modifier.size(keySize.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).combinedClickable(hapticFeedbackEnabled=false,onClick={if(number.length<128) setNumber(number+digit);if(prefs.haptics) view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)},onLongClick={if(digit=="0" && number.length<128) setNumber(number+"+") else speeds.firstOrNull { it.slot.toString()==digit }?.let { onCall(it.number) }}),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                        Text(digit,fontSize=if(keySize<64f) 28.sp else 32.sp,lineHeight=if(keySize<64f) 30.sp else 34.sp)
                        Text(letters.ifBlank { " " },fontSize=12.sp,lineHeight=14.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=FontWeight.Medium)
                    }
                } } }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick={onCall(number)},enabled=number.isNotBlank(),shape=CircleShape,colors=ButtonDefaults.buttonColors(containerColor=DialerPalette.CallGreen,contentColor=Color.White),modifier=Modifier.size(68.dp),contentPadding=PaddingValues(0.dp)) { Icon(Icons.Default.Call,"Llamar",modifier=Modifier.size(30.dp)) }
            Spacer(Modifier.height(8.dp))
            Text("Mantén 0 para + · Mantén 2–9 para marcación rápida",style=MaterialTheme.typography.bodyMedium,textAlign=androidx.compose.ui.text.style.TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
    }
}
