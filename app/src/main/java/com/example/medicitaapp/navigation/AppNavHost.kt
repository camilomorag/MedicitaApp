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
import com.example.medicitaapp.admin.PharmacistHomeScreen
import com.example.medicitaapp.admin.PharmacistLoginScreen
import com.example.medicitaapp.admin.ReviewFormulaScreen
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
                    navController.navigate("pharmacist_login")
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
                onVerTurno = { navController.navigate(AppRoutes.QUEUE) }
            )
        }

        composable(AppRoutes.UPLOAD) {
            UploadFormulaScreen(
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
                        popUpTo(AppRoutes.HOME)
                    }
                }
            )
        }

        composable("pharmacist_login") {
            PharmacistLoginScreen(
                onLoginSuccess = {
                    navController.navigate("pharmacist_home")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("pharmacist_home") {
            PharmacistHomeScreen(
                onReviewFormula = {
                    navController.navigate("review_formula")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("review_formula") {
            ReviewFormulaScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}