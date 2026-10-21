package com.pablormlopez.iphonedialer.diagnostics

import android.content.Context
import android.os.Build
import com.pablormlopez.iphonedialer.BuildConfig
import java.io.File
import java.util.Collections
import java.util.IdentityHashMap

/** Private, bounded local report. Never writes exception messages, contacts or call addresses. */
object AppDiagnostics {
    private const val FILE = "diagnostics/last-error.txt"
    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            record(app, "uncaught", error)
            if (previous != null) previous.uncaughtException(thread, error)
            else { android.os.Process.killProcess(android.os.Process.myPid()); kotlin.system.exitProcess(1) }
        }
    }
    fun sanitizedStack(error: Throwable): String = buildString {
        val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
        var current: Throwable? = error
        var depth = 0
        while (current != null && depth++ < 4 && seen.add(current)) {
            appendLine(current.javaClass.name)
            current.stackTrace.take(40).forEach { frame ->
                appendLine("  at ${frame.className}.${frame.methodName}(${frame.fileName ?: "unknown"}:${frame.lineNumber})")
            }
            current = current.cause
        }
    }.take(24_000)
    @Synchronized fun record(context: Context, event: String, error: Throwable) {
        runCatching {
            val destination = File(context.filesDir, FILE)
            destination.parentFile?.mkdirs()
            destination.writeText(buildString {
                appendLine("Informe técnico local · iPhone Dialer")
                appendLine("Versión: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                appendLine("Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
                appendLine("Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Evento: $event")
                appendLine("No contiene nombres, números, agenda ni mensajes de excepción.")
                append(sanitizedStack(error))
            })
        }
    }
    fun read(context: Context): String? = runCatching { File(context.filesDir, FILE).takeIf { it.exists() }?.readText() }.getOrNull()
    fun clear(context: Context) { File(context.filesDir, FILE).delete() }
}
