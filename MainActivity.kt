package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.ApexRiderViewModel
import com.example.ui.screens.CareerScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.RaceScreen
import com.example.ui.theme.ApexRiderTheme
import com.example.ui.theme.ObsidianBg

object Routes {
    const val MENU = "menu"
    const val GARAGE = "garage"
    const val RACE = "race"
    const val CAREER = "career"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ObsidianBg.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(ObsidianBg.toArgb())
        )
        setContent {
            ApexRiderTheme {
                ApexRiderApp()
            }
        }
    }
}

@Composable
fun ApexRiderApp(
    viewModel: ApexRiderViewModel = viewModel()
) {
    val navController = rememberNavController()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val recentRides by viewModel.recentRides.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.MENU
        ) {
            composable(Routes.MENU) {
                MainMenuScreen(
                    playerState = playerState,
                    onStartRide = { navController.navigate(Routes.RACE) },
                    onOpenGarage = { navController.navigate(Routes.GARAGE) },
                    onOpenCareer = { navController.navigate(Routes.CAREER) },
                    onSelectMap = viewModel::selectMap,
                    onToggleSteering = viewModel::setSteeringMode,
                    onToggleTraffic = viewModel::setTrafficMode
                )
            }

            composable(Routes.GARAGE) {
                GarageScreen(
                    playerState = playerState,
                    onBack = { navController.popBackStack() },
                    onBuyBike = viewModel::buyBike,
                    onSelectBike = viewModel::selectBike,
                    onUpgradeStat = viewModel::upgradeStat,
                    onRideWithSelected = {
                        navController.navigate(Routes.RACE) {
                            popUpTo(Routes.MENU)
                        }
                    }
                )
            }

            composable(Routes.RACE) {
                RaceScreen(
                    playerState = playerState,
                    onToggleSteering = viewModel::setSteeringMode,
                    onRideFinished = viewModel::recordRideResult,
                    onExitToMenu = {
                        navController.popBackStack(Routes.MENU, inclusive = false)
                    },
                    onExitToGarage = {
                        navController.navigate(Routes.GARAGE) {
                            popUpTo(Routes.MENU)
                        }
                    }
                )
            }

            composable(Routes.CAREER) {
                CareerScreen(
                    playerState = playerState,
                    recentRides = recentRides,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 68.dp)
        )
    }
}
