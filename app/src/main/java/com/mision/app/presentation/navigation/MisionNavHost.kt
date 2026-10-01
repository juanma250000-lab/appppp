package com.mision.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mision.app.AppContainer
import com.mision.app.presentation.LocalNavBottomPadding
import com.mision.app.presentation.components.GlassSurface
import com.mision.app.presentation.components.LoadingOverlay
import com.mision.app.presentation.components.MisionBackdrop
import com.mision.app.presentation.screen.home.HomeScreen
import com.mision.app.presentation.screen.missions.MissionsScreen
import com.mision.app.presentation.screen.onboarding.OnboardingScreen
import com.mision.app.presentation.screen.pet.PetScreen
import com.mision.app.presentation.screen.profile.ProfileScreen
import com.mision.app.presentation.screen.settings.SettingsScreen
import com.mision.app.presentation.screen.shop.ShopScreen
import com.mision.app.presentation.theme.AppGradients
import com.mision.app.presentation.theme.Dimens
import kotlinx.coroutines.flow.first

/**
 * App root: resolves where to start (onboarding vs. home) and hosts every
 * route plus the floating glass bottom bar.
 */
@Composable
fun MisionAppRoot(container: AppContainer) {
    val startRoute by produceState<String?>(initialValue = null, key1 = container) {
        value = runCatching {
            if (container.settingsRepository.settings.first().onboardingCompleted) {
                Destination.Home.route
            } else {
                Destination.Onboarding.route
            }
        }.getOrDefault(Destination.Home.route)
    }

    if (startRoute == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            MisionBackdrop()
            LoadingOverlay(
                message = "Preparando tu día…",
                modifier = Modifier.align(Alignment.Center),
            )
        }
    } else {
        MisionNavHost(startDestination = startRoute.orEmpty())
    }
}

/** Navigation graph for the four tabs plus shop, settings and onboarding. */
@Composable
private fun MisionNavHost(startDestination: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    val navBarInset: Dp =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val reservedBottom = Dimens.BottomBarHeight + Dimens.Space2xl + navBarInset

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalNavBottomPadding provides if (showBottomBar) reservedBottom else 0.dp,
        ) {
            NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(Dimens.AnimMedium)) },
            exitTransition = { fadeOut(tween(Dimens.AnimFast)) },
            popEnterTransition = { fadeIn(tween(Dimens.AnimMedium)) },
            popExitTransition = { fadeOut(tween(Dimens.AnimFast)) },
        ) {
            composable(Destination.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(Destination.Home.route) {
                HomeScreen(
                    onOpenMissions = { selectTab(navController, Destination.Missions) },
                    onOpenShop = { navController.openDetail(Destination.Shop) },
                    onOpenSettings = { navController.openDetail(Destination.Settings) },
                    onOpenPet = { selectTab(navController, Destination.Pet) },
                )
            }

            composable(Destination.Missions.route) {
                MissionsScreen()
            }

            composable(Destination.Pet.route) {
                PetScreen(onOpenShop = { navController.openDetail(Destination.Shop) })
            }

            composable(Destination.Profile.route) {
                ProfileScreen(onOpenSettings = { navController.openDetail(Destination.Settings) })
            }

            composable(Destination.Shop.route) {
                ShopScreen(onBack = { navController.popBackStack() })
            }

            composable(Destination.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = tween(Dimens.AnimMedium)) { it / 2 } +
                fadeIn(tween(Dimens.AnimMedium)),
            exit = slideOutVertically(animationSpec = tween(Dimens.AnimFast)) { it / 2 } +
                fadeOut(tween(Dimens.AnimFast)),
        ) {
            MisionBottomBar(
                currentRoute = currentRoute,
                navBarInset = navBarInset,
                onSelect = { destination -> selectTab(navController, destination) },
            )
        }
    }
}

/** Tab selection: keeps one entry per tab and restores its state. */
private fun selectTab(navController: NavHostController, destination: Destination) {
    navController.navigate(destination.route) {
        popUpTo(Destination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Pushes a full screen route (shop, settings) on top of the current one. */
private fun NavHostController.openDetail(destination: Destination) {
    navigate(destination.route) { launchSingleTop = true }
}

/** Floating glass bar with the four sections of the app. */
@Composable
private fun MisionBottomBar(
    currentRoute: String?,
    navBarInset: Dp,
    onSelect: (Destination) -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .padding(horizontal = Dimens.ScreenHorizontalPadding)
            .padding(bottom = Dimens.SpaceMd + navBarInset),
        shape = RoundedCornerShape(Dimens.RadiusXl),
        elevation = Dimens.GlassElevationHigh,
        contentPadding = PaddingValues(
            horizontal = Dimens.SpaceSm,
            vertical = Dimens.SpaceSm,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomDestinations.forEach { destination ->
                BottomBarItem(
                    destination = destination,
                    selected = destination.route == currentRoute,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

/** Single tab: tinted icon pill over its label. */
@Composable
private fun BottomBarItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    val labelColor = if (selected) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.RadiusSm))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = Dimens.SpaceSm, vertical = Dimens.SpaceXs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(
                    if (selected) {
                        Brush.linearGradient(AppGradients.primary)
                    } else {
                        Brush.linearGradient(
                            listOf(Color.Transparent, Color.Transparent),
                        )
                    },
                    shape,
                )
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceXs),
            contentAlignment = Alignment.Center,
        ) {
            if (destination.icon != null) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconLg),
                )
            } else {
                Text(text = destination.emoji, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
