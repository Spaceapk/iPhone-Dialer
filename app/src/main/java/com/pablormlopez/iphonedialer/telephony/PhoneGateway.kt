package com.pablormlopez.iphonedialer.telephony
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.role.RoleManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.pablormlopez.iphonedialer.domain.Search
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
data class SimAccount(val handle: PhoneAccountHandle, val label: String)
@Singleton class PhoneGateway @Inject constructor(@ApplicationContext private val context: Context) {
    private val telecom get() = context.getSystemService(TelecomManager::class.java)
    fun isDefault() = context.getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_DIALER)
    fun accounts(): List<SimAccount> {
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED) throw SecurityException("Autoriza primero el acceso a las cuentas SIM.")
        return telecom.callCapablePhoneAccounts.map { SimAccount(it, telecom.getPhoneAccount(it)?.label?.toString() ?: "Cuenta telefónica") }
    }
    fun voicemail() {
        check(isDefault()) { "Activa el marcador predeterminado." }
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED) throw SecurityException("Autoriza primero la llamada al buzón.")
        telecom.placeCall(Uri.parse("voicemail:"),Bundle())
    }
    fun place(number: String, account: PhoneAccountHandle? = null) {
        check(isDefault()) { "Configura primero la aplicación de teléfono predeterminada." }
        require(Search.validDialString(number)) { "Número no válido." }
        val extras = Bundle().apply { if(account != null) putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, account) }
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED) throw SecurityException("Autoriza primero la llamada.")
        telecom.placeCall(Uri.fromParts("tel", number.filterNot { it in " ()-." }, null), extras)
    }
}
