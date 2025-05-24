package com.lin.tomato.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Timer : Screen("timer/{mode}") {
        fun createRoute(mode: String) = "timer/$mode"
        
        const val WORK_MODE = "work"
        const val DRY_RUN_MODE = "dry_run"
    }

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                Home.route -> Home
                Timer.route.substringBefore("/") -> Timer
                else -> Home
            }
        }
    }
}
