package com.pablormlopez.iphonedialer.data
import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.provider.ContactsContract as CC
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class SystemRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val resolver get() = context.contentResolver
    fun changes() = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) { override fun onChange(selfChange: Boolean) { trySend(Unit) } }
        runCatching { resolver.registerContentObserver(CC.AUTHORITY_URI, true, observer) }
        runCatching { resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer) }
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
    suspend fun contacts(): List<Contact> = withContext(Dispatchers.IO) {
        val numbers = mutableMapOf<Long, MutableList<String>>()
        resolver.query(CC.CommonDataKinds.Phone.CONTENT_URI, arrayOf(CC.CommonDataKinds.Phone.CONTACT_ID, CC.CommonDataKinds.Phone.NUMBER), null, null, null)?.use { c ->
            while(c.moveToNext()) numbers.getOrPut(c.getLong(0)) { mutableListOf() }.add(c.getString(1) ?: "")
        }
        buildList {
            resolver.query(CC.Contacts.CONTENT_URI, arrayOf(CC.Contacts._ID, CC.Contacts.LOOKUP_KEY, CC.Contacts.DISPLAY_NAME_PRIMARY, CC.Contacts.STARRED, CC.Contacts.PHOTO_THUMBNAIL_URI), null, null, "${CC.Contacts.SORT_KEY_PRIMARY} ASC")?.use { c ->
                while(c.moveToNext()) add(Contact(c.getLong(0), c.getString(1) ?: "", c.getString(2) ?: "Sin nombre", numbers[c.getLong(0)]?.distinct() ?: emptyList(), c.getInt(3)==1, c.getString(4)))
            }
        }
    }
    suspend fun favorite(contact: Contact) = withContext(Dispatchers.IO) {
        resolver.update(ContentUris.withAppendedId(CC.Contacts.CONTENT_URI, contact.id), ContentValues().apply { put(CC.Contacts.STARRED, if(contact.starred) 0 else 1) }, null, null)
    }
    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) { resolver.delete(CC.Contacts.getLookupUri(contact.id, contact.lookup), null, null) }
    suspend fun recents(): List<Recent> = withContext(Dispatchers.IO) {
        buildList {
            resolver.query(CallLog.Calls.CONTENT_URI, arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.NUMBER_PRESENTATION), null, null, "${CallLog.Calls.DATE} DESC")?.use { c ->
                while(c.moveToNext()) add(Recent(c.getLong(0), c.getString(1) ?: "", c.getString(2), c.getInt(3), c.getLong(4), c.getLong(5), c.getInt(6)))
            }
        }
    }
    suspend fun deleteRecents(ids: Set<Long>) = withContext(Dispatchers.IO) {
        ids.chunked(100).forEach { chunk -> resolver.delete(CallLog.Calls.CONTENT_URI, "${CallLog.Calls._ID} IN (${chunk.joinToString(",") { "?" }})", chunk.map { it.toString() }.toTypedArray()) }
    }
    suspend fun blocked(): List<Blocked> = withContext(Dispatchers.IO) {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return@withContext emptyList()
        buildList { resolver.query(BlockedNumberContract.BlockedNumbers.CONTENT_URI, arrayOf(android.provider.BaseColumns._ID, BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER), null, null, null)?.use { c -> while(c.moveToNext()) add(Blocked(c.getLong(0), c.getString(1) ?: "")) } }
    }
    suspend fun block(number: String) = withContext(Dispatchers.IO) {
        check(BlockedNumberContract.canCurrentUserBlockNumbers(context)) { "Android no autoriza el bloqueo en este perfil." }
        resolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, ContentValues().apply { put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number) })
    }
    suspend fun unblock(id: Long) = withContext(Dispatchers.IO) { resolver.delete(ContentUris.withAppendedId(BlockedNumberContract.BlockedNumbers.CONTENT_URI, id), null, null) }
    suspend fun exportVcf(destination: Uri, contacts: List<Contact>) = withContext(Dispatchers.IO) {
        require(contacts.isNotEmpty()) { "No hay contactos para exportar." }
        requireNotNull(resolver.openOutputStream(destination, "wt")).use { output ->
            contacts.forEach { contact ->
                val uri = Uri.withAppendedPath(CC.Contacts.CONTENT_VCARD_URI, Uri.encode(contact.lookup))
                requireNotNull(resolver.openInputStream(uri)).use { it.copyTo(output) }
            }
        }
    }
}
