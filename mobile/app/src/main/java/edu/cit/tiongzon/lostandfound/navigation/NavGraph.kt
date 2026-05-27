package edu.cit.tiongzon.lostandfound.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.cit.tiongzon.lostandfound.feature.auth.ui.LoginScreen
import edu.cit.tiongzon.lostandfound.feature.auth.ui.RegisterScreen
import edu.cit.tiongzon.lostandfound.feature.admin.ui.AdminScreen
import edu.cit.tiongzon.lostandfound.feature.catalog.ui.CatalogScreen
import edu.cit.tiongzon.lostandfound.feature.catalog.ui.ItemDetailScreen
import edu.cit.tiongzon.lostandfound.feature.claims.ui.FileClaimScreen
import edu.cit.tiongzon.lostandfound.feature.claims.ui.ManageClaimScreen
import edu.cit.tiongzon.lostandfound.feature.chat.ui.GlobalChatScreen
import edu.cit.tiongzon.lostandfound.feature.chat.ui.PrivateChatScreen
import edu.cit.tiongzon.lostandfound.feature.dashboard.ui.DashboardScreen
import edu.cit.tiongzon.lostandfound.feature.home.ui.HomeScreen
import edu.cit.tiongzon.lostandfound.feature.chat.ui.MessagesScreen
import edu.cit.tiongzon.lostandfound.feature.profile.ui.ProfileScreen
import edu.cit.tiongzon.lostandfound.feature.report.ui.ReportItemScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{token}") { fun createRoute(token: String) = "home/$token" }
    object Catalog : Screen("catalog/{token}") { fun createRoute(token: String) = "catalog/$token" }
    object ItemDetail : Screen("item-detail/{itemId}/{token}") { fun createRoute(itemId: Long, token: String) = "item-detail/$itemId/$token" }
    object FileClaim : Screen("file-claim/{itemId}/{token}") { fun createRoute(itemId: Long, token: String) = "file-claim/$itemId/$token" }
    object Report : Screen("report/{token}") { fun createRoute(token: String) = "report/$token" }
    object Dashboard : Screen("dashboard/{token}") { fun createRoute(token: String) = "dashboard/$token" }
    object GlobalChat : Screen("global-chat/{token}") { fun createRoute(token: String) = "global-chat/$token" }
    object Messages : Screen("messages/{token}") { fun createRoute(token: String) = "messages/$token" }
    object PrivateChat : Screen("private-chat/{userId}/{itemId}/{token}") {
        fun createRoute(userId: Long, itemId: Long, token: String) = "private-chat/$userId/$itemId/$token"
    }
    object Admin : Screen("admin/{token}") { fun createRoute(token: String) = "admin/$token" }
    object Profile : Screen("profile/{token}") { fun createRoute(token: String) = "profile/$token" }
    object ManageClaim : Screen("manage-claim/{claimId}/{token}") { fun createRoute(claimId: Long, token: String) = "manage-claim/$claimId/$token" }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { token ->
                    navController.navigate(Screen.Home.createRoute(token)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            HomeScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Catalog.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            CatalogScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) },
                onItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId, token))
                }
            )
        }

        composable(Screen.ItemDetail.route) { back ->
            val itemId = back.arguments?.getString("itemId")?.toLongOrNull() ?: 0L
            val token = back.arguments?.getString("token") ?: ""
            ItemDetailScreen(
                itemId = itemId,
                token = token,
                onBack = { navController.popBackStack() },
                onNavigate = { navigateMobile(navController, token, it) },
                onMessageReporter = { reporterId ->
                    navController.navigate(Screen.PrivateChat.createRoute(reporterId, itemId, token))
                },
                onFileClaim = {
                    navController.navigate(Screen.FileClaim.createRoute(itemId, token))
                },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.FileClaim.route) { back ->
            val itemId = back.arguments?.getString("itemId")?.toLongOrNull() ?: 0L
            val token = back.arguments?.getString("token") ?: ""
            FileClaimScreen(
                token = token,
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Report.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            ReportItemScreen(
                token = token,
                onBack = { navController.popBackStack() },
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Dashboard.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            DashboardScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.GlobalChat.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            GlobalChatScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Messages.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            MessagesScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) },
                onConversationClick = { userId, itemId ->
                    navController.navigate(Screen.PrivateChat.createRoute(userId, itemId, token))
                }
            )
        }

        composable(Screen.PrivateChat.route) { back ->
            val userId = back.arguments?.getString("userId")?.toLongOrNull() ?: 0L
            val itemId = back.arguments?.getString("itemId")?.toLongOrNull() ?: 0L
            val token = back.arguments?.getString("token") ?: ""
            PrivateChatScreen(
                token = token,
                otherUserId = userId,
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Admin.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            AdminScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) }
            )
        }

        composable(Screen.Profile.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            ProfileScreen(
                token = token,
                onNavigate = { navigateMobile(navController, token, it) },
                onLogout = { logout(navController) },
                onManageClaim = { claimId ->
                    navController.navigate(Screen.ManageClaim.createRoute(claimId, token))
                }
            )
        }

        composable(Screen.ManageClaim.route) { back ->
            val token = back.arguments?.getString("token") ?: ""
            val claimId = back.arguments?.getString("claimId")?.toLongOrNull() ?: 0L
            ManageClaimScreen(
                token = token,
                claimId = claimId,
                onBack = { navController.popBackStack() },
                onLogout = { logout(navController) }
            )
        }
    }
}

private fun navigateMobile(navController: NavHostController, token: String, destination: String) {
    val route = when (destination) {
        "home"       -> Screen.Home.createRoute(token)
        "catalog"    -> Screen.Catalog.createRoute(token)
        "report"     -> Screen.Report.createRoute(token)
        "dashboard"  -> Screen.Dashboard.createRoute(token)
        "globalChat" -> Screen.GlobalChat.createRoute(token)
        "messages"   -> Screen.Messages.createRoute(token)
        "admin"      -> Screen.Admin.createRoute(token)
        "profile"    -> Screen.Profile.createRoute(token)
        else         -> Screen.Catalog.createRoute(token)
    }
    navController.navigate(route) { launchSingleTop = true }
}

private fun logout(navController: NavHostController) {
    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
}
