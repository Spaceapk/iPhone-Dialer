package com.pablormlopez.iphonedialer.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
@Entity data class SpeedDial(@PrimaryKey val slot: Int, val name: String, val number: String)
@Dao interface SpeedDialDao {
    @Query("SELECT * FROM SpeedDial ORDER BY slot") fun observe(): Flow<List<SpeedDial>>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(item: SpeedDial)
    @Query("DELETE FROM SpeedDial WHERE slot = :slot") suspend fun remove(slot: Int)
}
@Database(entities=[SpeedDial::class], version=1, exportSchema=false)
abstract class LocalDatabase: RoomDatabase() { abstract fun speedDials(): SpeedDialDao }
private val Context.settings by preferencesDataStore("settings")
data class Preferences(val theme: String="system", val accent: String="green", val glass: Boolean=true, val haptics: Boolean=true, val start: Int=3, val textScale: Float=1f, val animations: Boolean=true)
@Singleton class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    val flow: Flow<Preferences> = context.settings.data.catch { if (it is java.io.IOException) emit(emptyPreferences()) else throw it }.map { Preferences(it[stringPreferencesKey("theme")] ?: "system", it[stringPreferencesKey("accent")] ?: "green", it[booleanPreferencesKey("glass")] ?: true, it[booleanPreferencesKey("haptics")] ?: true, it[intPreferencesKey("start")] ?: 3, it[floatPreferencesKey("scale")] ?: 1f, it[booleanPreferencesKey("animations")] ?: true) }
    suspend fun set(key: String, value: String) { context.settings.edit { it[stringPreferencesKey(key)] = value } }
    suspend fun set(key: String, value: Boolean) { context.settings.edit { it[booleanPreferencesKey(key)] = value } }
    suspend fun start(value: Int) { context.settings.edit { it[intPreferencesKey("start")] = value.coerceIn(0,4) } }
    suspend fun scale(value: Float) { context.settings.edit { it[floatPreferencesKey("scale")] = value.coerceIn(1f,1.3f) } }
}
@Module @InstallIn(SingletonComponent::class) object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): LocalDatabase = Room.databaseBuilder(context, LocalDatabase::class.java, "local.db").build()
    @Provides fun speedDialDao(db: LocalDatabase) = db.speedDials()
}
