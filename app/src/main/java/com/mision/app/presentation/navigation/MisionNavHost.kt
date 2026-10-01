package com.mision.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mision.app.domain.repository.SettingsRepository
import com.mision.app.presentation.components.LoadingState
import com.mision.app.presentation.components.MisionBackground
import com.mision.app.presentation.screen.home.HomeScreen
import com.mision.app.presentation.screen.missions.MissionsScreen
import com.mision.app.presentation.screen.onboarding.OnboardingScreen
import com.mision.app.presentation.screen.pet.PetScreen
import com.mision.app.presentation.screen.profile.ProfileScreen
import com.mision.app.presentation.screen.settings.SettingsScreen
import com.mision.app.presentation.screen.shop.ShopScreen
import com.mision.app.presentation.theme.Dimens
import kotlinx.coroutines.flow.first

/** App root: resolves where to start (onboarding vs. home) and hosts the graph. */
@Composable
fun MisionAppRoot(settingsRepository: SettingsRepository) {
    val startDestination by produceState<Destination?>(initialValue = null, settingsRepository) {
        value = runCatching {
            if (settingsRepository.settings.first().onboardingCompleted) Destination.HOME else Destination.ONBOARDING
        }.getOrDefault(Destination.HOME)
    }

    when (val start = startDestination) {
        null -> MisionBackground { LoadingState(message = "Preparando tu día…") }
        else -> MisionNavHost(startDestination = start)
    }
}

/** Navigation graph: four tabs plus shop, settings and onboarding. */
@Composable
private fun MisionNavHost(startDestination: Destination) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = TopLevelTab.entries.firstOrNull { it.destination.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (currentTab != null) {
                MisionBottomBar(
                    current = currentTab,
                    onSelect = { tab -> navController.selectTab(tab.destination) },
                )
            }
        },
        // Each screen handles the status bar itself (edge to edge background).
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            enterTransition = { fadeIn(tween(Dimens.AnimMedium)) },
            exitTransition = { fadeOut(tween(Dimens.AnimFast)) },
            popEnterTransition = { fadeIn(tween(Dimens.AnimMedium)) },
            popExitTransition = { fadeOut(tween(Dimens.AnimFast)) },
        ) {
            composable(Destination.ONBOARDING.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Destination.HOME.route) {
                            popUpTo(Destination.ONBOARDING.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Destination.HOME.route) {
                HomeScreen(
                    onOpenMissions = { navController.selectTab(Destination.MISSIONS) },
                    onOpenPet = { navController.selectTab(Destination.PET) },
                    onOpenShop = { navController.openDetail(Destination.SHOP) },
                    onOpenSettings = { navController.openDetail(Destination.SETTINGS) },
                )
            }
            composable(Destination.MISSIONS.route) { MissionsScreen() }
            composable(Destination.PET.route) {
                PetScreen(onOpenShop = { navController.openDetail(Destination.SHOP) })
            }
            composable(Destination.PROFILE.route) {
                ProfileScreen(onOpenSettings = { navController.openDetail(Destination.SETTINGS) })
            }
            composable(Destination.SHOP.route) {
                ShopScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Tab selection: keeps one entry per tab and restores its state. */
private fun NavHostController.selectTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(Destination.HOME.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Pushes a full screen route (shop, settings) on top of the current one. */
private fun NavHostController.openDetail(destination: Destination) {
    navigate(destination.route) { launchSingleTop = true }
}

@Composable
private fun MisionBottomBar(current: TopLevelTab, onSelect: (TopLevelTab) -> Unit) {
    NavigationBar {
        TopLevelTab.entries.forEach { tab ->
            val selected = tab == current
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
