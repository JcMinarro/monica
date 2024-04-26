package com.teobaranga.monica.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeDestination
import com.ramcosta.composedestinations.generated.destinations.SetupDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.teobaranga.monica.MonicaBackground
import com.teobaranga.monica.ui.PreviewPixel4
import com.teobaranga.monica.ui.theme.MonicaTheme
import kotlinx.coroutines.flow.collectLatest

@Destination<RootGraph>(start = true)
@Composable
fun Home(navigator: DestinationsNavigator, modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) {
            navigator.navigate(SetupDestination) {
                popUpTo(HomeDestination.route) {
                    inclusive = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigation.collectLatest {
            navigator.navigate(it)
        }
    }

    if (isLoggedIn == true) {
        HomeScreen(
            modifier = modifier,
        )
    } else {
        // TODO Replace with Loading screen
        Box(
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()
    Scaffold(
        modifier = modifier,
        bottomBar = {
            HomeNavigationBar(
                navController = navController,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { contentPadding ->
        if (!LocalInspectionMode.current) {
            DestinationsNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
                navGraph = NavGraphs.rootHome,
                engine = engine,
                navController = navController,
            )
        }
    }
}

@PreviewPixel4
@Composable
private fun PreviewHomeScreen() {
    MonicaTheme {
        MonicaBackground {
            HomeScreen()
        }
    }
}
