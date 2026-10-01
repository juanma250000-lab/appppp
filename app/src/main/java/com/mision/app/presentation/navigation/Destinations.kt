package com.mision.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.graphics.vector.ImageVector

/** Every route of the app. */
enum class Destination(val route: String) {
    ONBOARDING("bienvenida"),
    HOME("inicio"),
    MISSIONS("misiones"),
    PET("mascota"),
    PROFILE("perfil"),
    SHOP("tienda"),
    SETTINGS("ajustes"),
}

/** The four sections of the bottom bar, in display order. */
enum class TopLevelTab(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(Destination.HOME, "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    MISSIONS(Destination.MISSIONS, "Misiones", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    PET(Destination.PET, "Mascota", Icons.Filled.Pets, Icons.Outlined.Pets),
    PROFILE(Destination.PROFILE, "Perfil", Icons.Filled.Person, Icons.Outlined.Person),
}
