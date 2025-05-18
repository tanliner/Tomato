package com.lin.tomato

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lin.tomato.service.PermissionHandler
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
                MainScreen()
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