package com.pablormlopez.iphonedialer.ui
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.pablormlopez.iphonedialer.data.Preferences
object DialerPalette { val CallGreen = Color(0xFF16803C) }
private tailrec fun Context.hostActivity(): Activity? = when(this) {
    is Activity -> this
    is ContextWrapper -> baseContext.hostActivity()
    else -> null
}
@Composable fun DialerTheme(prefs: Preferences, content: @Composable ()->Unit) {
    val dark = when(prefs.theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val accent = when(prefs.accent) {
        "green" -> if(dark) Color(0xFF66DB8F) else Color(0xFF16723B)
        "violet" -> if(dark) Color(0xFFCAA9FF) else Color(0xFF7040AC)
        else -> if(dark) Color(0xFF80B8FF) else Color(0xFF0062CC)
    }
    val background = if(dark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val surface = if(dark) Color(0xFF1C1C1E) else Color.White
    val secondary = if(dark) Color(0xFFB2B2B8) else Color(0xFF636366)
    val base = if(dark) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(primary=accent,onPrimary=if(dark) Color.Black else Color.White,
        background=background,onBackground=if(dark) Color.White else Color(0xFF1C1C1E),
        surface=surface,onSurface=if(dark) Color.White else Color(0xFF1C1C1E),
        surfaceVariant=if(dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),onSurfaceVariant=secondary,
        primaryContainer=if(dark) Color(0xFF18324C) else Color(0xFFE4EEFA),onPrimaryContainer=accent,
        secondaryContainer=if(dark) Color(0xFF303033) else Color(0xFFE5E5EA),onSecondaryContainer=accent,
        outline=if(dark) Color(0xFF77777D) else Color(0xFF8E8E93),outlineVariant=if(dark) Color(0xFF38383A) else Color(0xFFD1D1D6),
        surfaceContainerLowest=background,surfaceContainerLow=surface,surfaceContainer=surface,
        surfaceContainerHigh=surface,surfaceContainerHighest=surface,surfaceTint=Color.Transparent)
    val activity = LocalContext.current.hostActivity() as? androidx.activity.ComponentActivity
    SideEffect {
        val bars = if(dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                   else SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT)
        activity?.enableEdgeToEdge(statusBarStyle=bars,navigationBarStyle=bars)
    }
    val density=LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density,density.fontScale*prefs.textScale)) {
        MaterialTheme(colorScheme=scheme,shapes=Shapes(small=RoundedCornerShape(8.dp),medium=RoundedCornerShape(12.dp),large=RoundedCornerShape(16.dp)),content=content)
    }
}
