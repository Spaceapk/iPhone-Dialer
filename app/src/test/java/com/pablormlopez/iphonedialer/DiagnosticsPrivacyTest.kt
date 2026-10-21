package com.pablormlopez.iphonedialer
import com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics
import org.junit.Assert.*
import org.junit.Test
class DiagnosticsPrivacyTest {
    @Test fun errorMessagesAndContactDataNeverEnterReport() {
        val e=IllegalArgumentException("Contacto Privado +50588881234",SecurityException("cuenta@privada.example"))
        val text=AppDiagnostics.sanitizedStack(e)
        assertTrue(text.contains("IllegalArgumentException"));assertTrue(text.contains("SecurityException"))
        assertFalse(text.contains("Contacto Privado"));assertFalse(text.contains("+50588881234"));assertFalse(text.contains("cuenta@privada.example"))
    }
    @Test fun traceIsBounded() {
        val e=RuntimeException("do not retain")
        e.stackTrace=Array(10000) { StackTraceElement("test.Component","action","Component.kt",it) }
        assertTrue(AppDiagnostics.sanitizedStack(e).length<=24000)
        assertTrue(AppDiagnostics.sanitizedStack(e).lines().size<=42)
    }
}
