package com.example.gestionventas.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionventas.ui.venta.CapturaRespaldoScreen
import com.example.gestionventas.ui.venta.ComisionesScreen
import com.example.gestionventas.ui.venta.DetalleRevisionScreen
import com.example.gestionventas.ui.venta.EscanerQrScreen
import com.example.gestionventas.ui.venta.ListaVentasScreen
import com.example.gestionventas.ui.venta.LoginScreen
import com.example.gestionventas.ui.venta.LoginViewModel
import com.example.gestionventas.ui.venta.VentaFormScreen
import com.example.gestionventas.ui.venta.VentaViewModel

/**
 * Flujo: login -> lista -> (form | revision | comisiones) y, desde el form, camara / qr.
 * Un único VentaViewModel (alcance Activity) se comparte entre todas las pantallas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val ventaViewModel: VentaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable("login") {
            LoginScreen(vm = loginViewModel, onLoginOk = { usuario ->
                ventaViewModel.iniciarSesion(usuario)
                navController.navigate("lista") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable("lista") { ListaVentasScreen(navController, ventaViewModel) }
        composable("form") { VentaFormScreen(navController, ventaViewModel) }
        composable(
            route = "revision/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetalleRevisionScreen(navController, ventaViewModel, id)
        }
        composable("comisiones") { ComisionesScreen(navController, ventaViewModel) }
        composable("camara") { CapturaRespaldoScreen(navController, ventaViewModel) }
        composable("qr") { EscanerQrScreen(navController, ventaViewModel) }
    }
}
