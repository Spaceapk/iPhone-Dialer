package com.pablormlopez.iphonedialer.ui
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.pablormlopez.iphonedialer.data.Preferences
@Composable fun DialerTheme(prefs: Preferences, content: @Composable ()->Unit) {
    val dark= when(prefs.theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val accent=when(prefs.accent) { "blue" -> if(dark) Color(0xFF91BAFF) else Color(0xFF245DB0); "violet" -> if(dark) Color(0xFFD3B8FF) else Color(0xFF7544B5); else -> if(dark) Color(0xFF8BDFC2) else Color(0xFF006A50) }
    val base=if(dark) darkColorScheme(primary=accent, background=Color(0xFF101917), surface=Color(0xFF19231F), surfaceVariant=Color(0xFF28362F), onSurfaceVariant=Color(0xFFC2D1C8)) else lightColorScheme(primary=accent, background=Color(0xFFF3F6F4), surface=Color.White, surfaceVariant=Color(0xFFE4ECE6), onSurfaceVariant=Color(0xFF45564A))
    val container=androidx.compose.ui.graphics.lerp(base.surface,accent,.13f)
    val scheme=base.copy(onPrimary=if(dark) Color(0xFF10231D) else Color.White,primaryContainer=container,onPrimaryContainer=accent,secondaryContainer=container,onSecondaryContainer=accent,surfaceTint=accent)
    val density=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density,density.fontScale*prefs.textScale)) { MaterialTheme(colorScheme=scheme,content=content) }
}
