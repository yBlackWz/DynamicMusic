package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.permissions.PermissionUtils
import com.example.service.DynamicIslandOverlayService
import com.example.ui.DynamicIslandNavigation
import com.example.ui.NavRoutes
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialRoute = intent.getStringExtra("route") ?: NavRoutes.MAIN

        // Se o serviço estiver habilitado e com permissão, inicializa o overlay
        val app = applicationContext as? DynamicIslandApplication
        if (app != null && PermissionUtils.hasOverlayPermission(this)) {
            DynamicIslandOverlayService.start(this)
        }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                LaunchedEffect(intent) {
                    val targetRoute = intent.getStringExtra("route")
                    if (targetRoute != null && targetRoute != NavRoutes.MAIN) {
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    DynamicIslandNavigation(
                        navController = navController,
                        initialRoute = initialRoute
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
