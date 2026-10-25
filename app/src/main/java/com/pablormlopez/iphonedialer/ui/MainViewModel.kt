package com.pablormlopez.iphonedialer.ui
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablormlopez.iphonedialer.data.*
import com.pablormlopez.iphonedialer.telephony.PhoneGateway
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
@HiltViewModel class MainViewModel @Inject constructor(val system: SystemRepository, val preferences: SettingsStore, val speeds: SpeedDialDao, val phone: PhoneGateway, @ApplicationContext private val context: Context): ViewModel() {
    val contacts=MutableStateFlow<List<Contact>>(emptyList())
    val recents=MutableStateFlow<List<Recent>>(emptyList())
    val blocked=MutableStateFlow<List<Blocked>>(emptyList())
    val message=MutableStateFlow<String?>(null)
    val diagnosticReport=MutableStateFlow<String?>(null)
    fun loadDiagnostics()=work { diagnosticReport.value=kotlinx.coroutines.withContext(Dispatchers.IO) { com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics.read(context) } }
    fun clearDiagnostics()=work { kotlinx.coroutines.withContext(Dispatchers.IO) { com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics.clear(context) };diagnosticReport.value=null }
    val prefs=preferences.flow.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),Preferences())
    val speedDials=speeds.observe().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    private var refreshJob: Job?=null
    init { viewModelScope.launch { system.changes().debounce(250).collect { refresh() } } }
    fun has(permission: String) = ContextCompat.checkSelfPermission(context,permission)==PackageManager.PERMISSION_GRANTED
    fun refresh() {
        if(!phone.isDefault()) blocked.value=emptyList()
        refreshJob?.cancel();refreshJob=viewModelScope.launch {
            if(has(Manifest.permission.READ_CONTACTS)) runCatching { system.contacts() }.onSuccess { contacts.value=it }.onFailure { if(it is CancellationException) throw it; contacts.value=emptyList();message.value="No se pudo leer la agenda." } else contacts.value=emptyList()
            if(phone.isDefault() && has(Manifest.permission.READ_CALL_LOG)) runCatching { system.recents() }.onSuccess { recents.value=it }.onFailure { if(it is CancellationException) throw it;recents.value=emptyList();message.value="No se pudo leer el historial." } else recents.value=emptyList()
        }
    }
    fun work(block: suspend ()->Unit) { viewModelScope.launch { try { block() } catch(e: CancellationException) { throw e } catch(e: Exception) { message.value=e.message ?: "La operación no se pudo completar." } } }
    fun loadBlocked()=work { blocked.value=system.blocked() }
}
