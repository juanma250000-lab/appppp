package com.mision.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** Every route of the app. Bottom navigation uses the first four. */
sealed class Destination(
    val route: String,
    val label: String,
    val emoji: String,
    val icon: ImageVector? = null,
) {
    data object Onboarding : Destination("bienvenida", "Bienvenida", "✨")

    data object Home :
        Destination("inicio", "Inicio", "🏠", Icons.Filled.Home)

    data object Missions :
        Destination("misiones", "Misiones", "✅", Icons.Filled.CheckCircle)

    data object Pet :
        Destination("mascota", "Mascota", "🐾", Icons.Filled.Pets)

    data object Profile :
        Destination("perfil", "Perfil", "👤", Icons.Filled.Person)

    data object Shop : Destination("tienda", "Tienda", "🛍️")

    data object Settings : Destination("ajustes", "Ajustes", "⚙️")
}

/** The four sections shown in the bottom bar, in display order. */
val bottomDestinations = listOf(
    Destination.Home,
    Destination.Missions,
    Destination.Pet,
    Destination.Profile,
)
