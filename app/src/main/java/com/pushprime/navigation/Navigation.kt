package com.pushprime.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Coaching : Screen("coaching")
    object Compete : Screen("compete")
    object GroupSession : Screen("group_session")
    object Motivation : Screen("motivation")
}
