package com.ferm.nexusforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.backend.LocaleHelper
import com.ferm.nexusforge.backend.LocaleHelper.onAttach
import com.ferm.nexusforge.backend.MyAppNav3
import com.ferm.nexusforge.backend.SecurityCheck
import com.ferm.nexusforge.ui.theme.LocalDarkTheme
import com.ferm.nexusforge.ui.theme.NexusForgeTheme
import com.ferm.nexusforge.viewmodels.ThemeViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(onAttach(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        LocaleHelper.applyLocale(this)
        
        if (!performSecurityChecks()) {
            finish()
            exitProcess(0)
            return
        }
        
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val systemDarkTheme = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                themeViewModel.init(this@MainActivity, systemDarkTheme)
            }
            
            key(themeViewModel.isDarkTheme) {
                CompositionLocalProvider(LocalDarkTheme provides themeViewModel.isDarkTheme) {
                    NexusForgeTheme(darkTheme = themeViewModel.isDarkTheme) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MyAppNav3(themeViewModel = themeViewModel)
                        }
                    }
                }
            }
        }
    }
    
    private fun performSecurityChecks(): Boolean {
        if (!SecurityCheck.verifyAppIntegrity(this)) {
            return false
        }
        
        if (!SecurityCheck.isDeviceSecure()) {
            // Можно вернуть false для блокировки на root устройствах
            // return false
        }
        
        return true
    }
}
