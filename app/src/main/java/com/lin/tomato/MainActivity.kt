package com.lin.tomato

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lin.tomato.navigation.Screen
import com.lin.tomato.service.PermissionHandler
import com.lin.tomato.ui.screens.HomeScreen
import com.lin.tomato.ui.screens.MainScreen
import com.lin.tomato.ui.theme.TomatoTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHandler = PermissionHandler(this)
        permissionHandler.checkAndRequestNotificationPermission()

        setContent {
            TomatoTheme {
                TomatoApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) {
            return
        }
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
        if (notificationId != -1) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationId)
        }
    }
}

@Composable
fun TomatoApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTimer = { mode ->
                    navController.navigate(Screen.Timer.createRoute(mode))
                }
            )
        }
        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: Screen.Timer.WORK_MODE
            MainScreen(timerMode = mode)
        }
    }
}