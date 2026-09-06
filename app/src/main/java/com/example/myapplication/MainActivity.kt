package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.di.AuthEventBus
import com.example.myapplication.di.PaymentResultBus
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.screens.SettingsState
import com.example.myapplication.ui.screens.UserViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.collectAsState
import com.example.myapplication.data.local.TokenManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleWaveIntent(intent)

        setContent {
            val userViewModel: UserViewModel = hiltViewModel()
            val darkModeStored by tokenManager.getDarkMode().collectAsState(initial = null)
            
            // Clair par défaut. Le choix explicite de l'utilisateur reste prioritaire.
            val darkTheme = darkModeStored ?: false

            // Logout automatique sur 401
            var forceLogout by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                AuthEventBus.logoutEvent.collect { forceLogout = true }
            }

            val systemUiController = rememberSystemUiController()
            DisposableEffect(systemUiController, darkTheme) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !darkTheme
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent,
                    darkIcons = !darkTheme
                )
                onDispose {}
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                AppNavigation(
                    forceLogout = forceLogout,
                    onLogoutHandled = { forceLogout = false }
                )
            }
        }
    }

    /**
     * Appelé quand l'app est déjà en mémoire (launchMode="singleTask")
     * et qu'un deep link Wave est reçu pendant que l'utilisateur est
     * sur CheckoutScreen.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWaveIntent(intent)
    }

    /**
     * Analyse l'URI Wave et émet le résultat dans PaymentResultBus.
     * Format : civapp://payment/return?status=success|failed|cancelled
     */
    private fun handleWaveIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "civapp" && uri.host == "payment") {
            val status = when (uri.getQueryParameter("status")?.lowercase()) {
                "success"   -> PaymentResultBus.Status.SUCCESS
                "failed"    -> PaymentResultBus.Status.FAILED
                "cancelled" -> PaymentResultBus.Status.CANCELLED
                else        -> PaymentResultBus.Status.CANCELLED
            }
            PaymentResultBus.emit(status)
        }
    }
}
