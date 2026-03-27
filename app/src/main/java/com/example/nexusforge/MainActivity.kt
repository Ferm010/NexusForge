package com.example.nexusforge

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nexusforge.backend.MyAppNav3
import com.example.nexusforge.backend.SecurityCheck
import com.example.nexusforge.frontend.RegNamePage
import com.example.nexusforge.ui.theme.NexusForgeTheme
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Проверка безопасности при запуске
        if (!performSecurityChecks()) {
            // Приложение скомпрометировано - закрываем
            finish()
            exitProcess(0)
            return
        }
        
        enableEdgeToEdge()
        setContent {
            NexusForgeTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    //MyApp()
                    MyAppNav3()
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NexusForgeTheme {
        Greeting("Android")
    }
}