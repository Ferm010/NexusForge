package com.example.nexusforge

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.backend.LocaleHelper
import com.example.nexusforge.backend.LocaleHelper.onAttach
import com.example.nexusforge.backend.MyAppNav3
import com.example.nexusforge.backend.SecurityCheck
import com.example.nexusforge.ui.theme.NexusForgeTheme
import com.example.nexusforge.viewmodels.ThemeViewModel
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(onAttach(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем сохранённый язык
        LocaleHelper.applyLocale(this)
        
        // Проверка безопасности при запуске
        if (!performSecurityChecks()) {
            // Приложение скомпрометировано - закрываем
            finish()
            exitProcess(0)
            return
        }
        
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val systemDarkTheme = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                themeViewModel.initTheme(systemDarkTheme)
            }
            
            Crossfade(
                targetState = themeViewModel.isDarkTheme ?: systemDarkTheme,
                animationSpec = tween(durationMillis = 400),
                label = "theme_transition"
            ) { isDark ->
                key(isDark) {
                    NexusForgeTheme(darkTheme = isDark) {
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
        // Проверка целостности приложения
        if (!SecurityCheck.verifyAppIntegrity(this)) {
            Log.e("Security", "App integrity check failed!")
            return false
        }
        
        // Проверка на root/эмулятор (опционально, можно отключить для тестирования)
        if (!SecurityCheck.isDeviceSecure()) {
            Log.w("Security", "Device security warning: rooted or emulator detected")
            // Можно вернуть false для блокировки на root устройствах
            // return false
        }
        
        // В debug режиме выводим текущий хеш подписи
        if (BuildConfig.DEBUG) {
            val currentHash = SecurityCheck.getCurrentSignatureHash(this)
            Log.d("Security", "Current signature hash: $currentHash")
        }
        
        return true
    }
}