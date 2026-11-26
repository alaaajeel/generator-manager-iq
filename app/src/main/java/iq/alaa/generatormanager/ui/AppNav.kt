package iq.alaa.generatormanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.ui.screens.EditSubscriberScreen
import iq.alaa.generatormanager.ui.screens.FinanceScreen
import iq.alaa.generatormanager.ui.screens.HomeScreen
import iq.alaa.generatormanager.ui.screens.ReportsScreen
import iq.alaa.generatormanager.ui.screens.SettingsScreen
import iq.alaa.generatormanager.ui.screens.SubscriberDetailScreen
import iq.alaa.generatormanager.ui.screens.SubscribersScreen
import iq.alaa.generatormanager.ui.screens.TariffScreen
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Petrol

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "الرئيسية", Icons.Filled.Home),
    Tab("subscribers", "المشتركين", Icons.Filled.Groups),
    Tab("finance", "المالية", Icons.Filled.AccountBalanceWallet),
    Tab("reports", "التقارير", Icons.Filled.BarChart),
    Tab("settings", "الإعدادات", Icons.Filled.Settings),
)

@Composable
fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        bottomBar = {
            if (showBar) NavigationBar(containerColor = Petrol) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = { nav.goTab(t.route) },
                        icon = { Icon(t.icon, t.label) },
                        label = { Text(t.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold, selectedTextColor = Gold,
                            unselectedIconColor = Color.White.copy(alpha = 0.8f),
                            unselectedTextColor = Color.White.copy(alpha = 0.8f),
                            indicatorColor = Color.White.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") {
                HomeScreen(
                    vm,
                    onAddSubscriber = { nav.navigate("edit/0") },
                    onCollect = { nav.goTab("subscribers") },
                    onExpense = { nav.navigate("expense") },
                    onTariff = { nav.navigate("tariff") }
                )
            }
            composable("subscribers") {
                SubscribersScreen(vm, onOpen = { nav.navigate("subscriber/$it") }, onAdd = { nav.navigate("edit/0") }, onBack = null)
            }
            composable("finance") { FinanceScreen(vm, startWithAdd = false, onBack = null) }
            composable("expense") { FinanceScreen(vm, startWithAdd = true, onBack = { nav.popBackStack() }) }
            composable("reports") { ReportsScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
            composable("tariff") { TariffScreen(vm, onBack = { nav.popBackStack() }) }
            composable(
                "subscriber/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { e ->
                val id = e.arguments?.getLong("id") ?: 0L
                SubscriberDetailScreen(vm, id, startWithPay = false, onEdit = { nav.navigate("edit/$id") }, onBack = { nav.popBackStack() })
            }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { e ->
                val id = e.arguments?.getLong("id") ?: 0L
                EditSubscriberScreen(vm, id, onDone = { nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
        }
    }
}

private fun NavHostController.goTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
