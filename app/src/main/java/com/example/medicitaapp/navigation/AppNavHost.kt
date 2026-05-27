package com.example.medicitaapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.medicitaapp.DeliverySuccessScreen
import com.example.medicitaapp.ForgotPasswordScreen
import com.example.medicitaapp.HomeScreen
import com.example.medicitaapp.LoginScreen
import com.example.medicitaapp.QueueStatusScreen
import com.example.medicitaapp.RegisterScreen
import com.example.medicitaapp.UploadFormulaScreen
import com.example.medicitaapp.admin.PharmacistLoginScreen
import com.example.medicitaapp.admin.PharmacistRequestsScreen
import com.example.medicitaapp.admin.PharmacistMedicinesScreen
import com.example.medicitaapp.admin.MedicineDetailScreen
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
                },
                onForgotPassword = {
                    navController.navigate(AppRoutes.FORGOT_PASSWORD)
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
            if (authViewModel.currentUser == null) {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.HOME) { inclusive = true }
                }
            } else {
                HomeScreen(
                    userName = authViewModel.currentUser?.nombre ?: "Usuario",
                    onSubirFormula = { navController.navigate(AppRoutes.UPLOAD) },
                    onVerTurno = { navController.navigate(AppRoutes.QUEUE) },
                    onVerNotificaciones = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                    onVerPerfil = { navController.navigate(AppRoutes.PROFILE) }
                    // authViewModel = authViewModel  // ← Elimina esta línea
                )
            }
        }

        composable(AppRoutes.UPLOAD) {
            if (authViewModel.currentUser == null) {
                navController.navigate(AppRoutes.LOGIN)
            } else {
                UploadFormulaScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.QUEUE) {
            if (authViewModel.currentUser == null) {
                navController.navigate(AppRoutes.LOGIN)
            } else {
                var turno by remember { mutableStateOf("Sin turno") }
                var ubicacion by remember { mutableStateOf("Sin ubicación") }
                var tiempoEspera by remember { mutableStateOf(0) }
                var posicionCola by remember { mutableStateOf(0) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    try {
                        val userRequests = authViewModel.getUserRequests()
                        val ultimaSolicitud = userRequests.firstOrNull { it.estado == "aceptada" || it.estado == "lista" }
                        if (ultimaSolicitud != null) {
                            turno = ultimaSolicitud.turno.ifEmpty { "Sin turno" }
                            ubicacion = ultimaSolicitud.ubicacion.ifEmpty { "Sin ubicación" }
                            tiempoEspera = ultimaSolicitud.tiempoEspera
                            posicionCola = ultimaSolicitud.posicionCola
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavHost", "Error cargando turno: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    QueueStatusScreen(
                        onBack = { navController.popBackStack() },
                        turno = turno,
                        ubicacion = ubicacion,
                        tiempoEspera = tiempoEspera,
                        posicionCola = posicionCola
                    )
                }
            }
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
            if (authViewModel.currentUser == null) {
                navController.navigate(AppRoutes.LOGIN)
            } else {
                UserProfileScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppRoutes.NOTIFICATIONS) {
            if (authViewModel.currentUser == null) {
                navController.navigate(AppRoutes.LOGIN)
            } else {
                NotificationsScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.PHARMACIST_LOGIN) {
            PharmacistLoginScreen(
                onLoginSuccess = {
                    authViewModel.loginAsPharmacist()
                    navController.navigate(AppRoutes.PHARMACIST_REQUESTS)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PHARMACIST_REQUESTS) {
            if (!authViewModel.isPharmacistLoggedIn) {
                navController.navigate(AppRoutes.PHARMACIST_LOGIN)
            } else {
                PharmacistRequestsScreen(
                    authViewModel = authViewModel,
                    onOpenRequest = { requestId ->
                        navController.navigate("${AppRoutes.REVIEW_FORMULA}/$requestId")
                    },
                    onManageMedicines = {
                        navController.navigate(AppRoutes.PHARMACIST_MEDICINES)
                    },
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AppRoutes.PHARMACIST_MEDICINES) {
            if (!authViewModel.isPharmacistLoggedIn) {
                navController.navigate(AppRoutes.PHARMACIST_LOGIN)
            } else {
                PharmacistMedicinesScreen(
                    authViewModel = authViewModel,
                    onOpenMedicine = { medicineId ->
                        navController.navigate("medicine_detail/$medicineId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("medicine_detail/{medicineId}") { backStackEntry ->
            val medicineId = backStackEntry.arguments
                ?.getString("medicineId")
                ?.toIntOrNull() ?: 0
            MedicineDetailScreen(
                authViewModel = authViewModel,
                medicineId = medicineId,
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

        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}