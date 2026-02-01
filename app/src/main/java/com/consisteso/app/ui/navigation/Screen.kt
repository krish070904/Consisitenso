package com.consisteso.app.ui.navigation

/**
 * Navigation destinations
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object RuleCreation : Screen("rule_creation")
    object RuleList : Screen("rule_list")
    object Settings : Screen("settings")
}
