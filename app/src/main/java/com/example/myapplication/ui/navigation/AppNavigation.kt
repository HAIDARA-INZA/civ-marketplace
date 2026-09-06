package com.example.myapplication.ui.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.screens.client.*

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    forceLogout: Boolean = false,
    onLogoutHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Redirection vers Login quand le token expire (401)
    LaunchedEffect(forceLogout) {
        if (forceLogout) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            onLogoutHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { destination ->
                    if (destination.startsWith("verify_email")) {
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Main.createRoute(destination)) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToVerify = { email ->
                    navController.navigate(Screen.VerifyEmail.createRoute(Uri.encode(email)))
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToVerify = { email ->
                    navController.navigate(Screen.VerifyEmail.createRoute(Uri.encode(email)))
                }
            )
        }

        composable(Screen.VerifyEmail.route) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            VerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() },
                onNavigateToReset = { email ->
                    navController.navigate(Screen.ResetPassword.createRoute(Uri.encode(email)))
                }
            )
        }

        composable(Screen.ResetPassword.route) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            ResetPasswordScreen(
                email = email,
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Screen.Main.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "client"
            MainShell(
                role = role,
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.ClientOrders.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToVendorStore = { vendorId, vendorName ->
                    navController.navigate(Screen.VendorStore.createRoute(vendorId, Uri.encode(vendorName)))
                },
                onNavigateToChat = { id, name ->
                    navController.navigate(Screen.Chat.createRoute(id, Uri.encode(name)))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { id, name ->
                    navController.navigate(Screen.Chat.createRoute(id, Uri.encode(name)))
                }
            )
        }

        composable(
            route = Screen.VendorStore.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.IntType },
                navArgument("vendorName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            VendorStoreScreen(
                vendorId = backStackEntry.arguments?.getInt("vendorId") ?: 0,
                vendorName = Uri.decode(backStackEntry.arguments?.getString("vendorName") ?: "Boutique"),
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { productId -> navController.navigate(Screen.ProductDetail.createRoute(productId)) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(onNavigateToCheckout = {
                navController.navigate(Screen.Checkout.route)
            })
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onPaymentFinished = { navController.popBackStack() },
                onRetry = {
                    // Relancer startCheckout — le ViewModel se remet en Idle
                    navController.navigate(Screen.Checkout.route) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ClientOrders.route) {
            ClientOrdersScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val userName = Uri.decode(backStackEntry.arguments?.getString("userName") ?: "")
            ChatConversationScreen(
                userId = userId,
                userName = userName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
