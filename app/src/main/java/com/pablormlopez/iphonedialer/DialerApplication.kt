package com.pablormlopez.iphonedialer
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
@HiltAndroidApp class DialerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.pablormlopez.iphonedialer.diagnostics.AppDiagnostics.install(this)
    }
}
