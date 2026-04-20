package com.example.medicitaapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.medicitaapp.DeliverySuccessScreen
import com.example.medicitaapp.HomeScreen
import com.example.medicitaapp.LoginScreen
import com.example.medicitaapp.QueueStatusScreen
import com.example.medicitaapp.RegisterScreen
import com.example.medicitaapp.UploadFormulaScreen
import com.example.medicitaapp.admin.PharmacistLoginScreen
import com.example.medicitaapp.admin.PharmacistRequestsScreen
import com.example.medicitaapp.admin.ReviewFormulaScreen
import com.example.medicitaapp.user.NotificationsScreen
import com.example.medicitaapp.user.UserProfileScreen
import com.example.medicitaapp.viewmodel.AuthViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                },
                onGoPharmacist = {
                    navController.navigate(AppRoutes.PHARMACIST_LOGIN)
                }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                userName = authViewModel.currentUser?.nombre ?: "Usuario",
                onSubirFormula = { navController.navigate(AppRoutes.UPLOAD) },
                onVerTurno = { navController.navigate(AppRoutes.QUEUE) },
                onVerNotificaciones = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                onVerPerfil = { navController.navigate(AppRoutes.PROFILE) }
            )
        }

        composable(AppRoutes.UPLOAD) {
            UploadFormulaScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.QUEUE) {
            QueueStatusScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SUCCESS) {
            DeliverySuccessScreen(
                onGoHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(AppRoutes.PROFILE) {
            UserProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PHARMACIST_LOGIN) {
            PharmacistLoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.PHARMACIST_REQUESTS)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PHARMACIST_REQUESTS) {
            PharmacistRequestsScreen(
                authViewModel = authViewModel,
                onOpenRequest = { requestId ->
                    navController.navigate("${AppRoutes.REVIEW_FORMULA}/$requestId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("${AppRoutes.REVIEW_FORMULA}/{requestId}") { backStackEntry ->
            val requestId = backStackEntry.arguments
                ?.getString("requestId")
                ?.toIntOrNull() ?: 0

            ReviewFormulaScreen(
                authViewModel = authViewModel,
                requestId = requestId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}