package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxRed
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun ProfileScreen(
    user: User?,
    onShowPremiumUpgrade: () -> Unit,
    onAudioSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground),
        contentPadding = PaddingValues(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 120.dp)
    ) {
        // User Profile Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(KinemaxAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar de usuario",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.nombre?.ifEmpty { "Usuario Vibefy" } ?: "Usuario Vibefy",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = user?.email ?: "usuario@kinemax.store",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KinemaxTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subscription Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (user?.esPremium == true) KinemaxGold.copy(alpha = 0.2f) else KinemaxSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user?.esPremium == true) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (user?.esPremium == true) KinemaxGold else KinemaxTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user?.esPremium == true) "PLAN KINEMAX PREMIUM" else "PLAN KINEMAX FREE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (user?.esPremium == true) KinemaxGold else KinemaxTextSecondary
                        )
                    }
                }
            }
        }

        // Account Permissions & Benefits Card
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Estado de tu Cuenta",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatusRow(
                        icon = Icons.Default.Star,
                        title = "Membresía Premium",
                        value = if (user?.esPremium == true) "Activa" else "Free",
                        isActive = user?.esPremium == true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusRow(
                        icon = Icons.Default.VolumeOff,
                        title = "Sin Anuncios (quita_anuncios)",
                        value = if (user?.quitaAnuncios == true) "Habilitado" else "Deshabilitado",
                        isActive = user?.quitaAnuncios == true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusRow(
                        icon = Icons.Default.Download,
                        title = "Descargas Offline (permite_descargas)",
                        value = if (user?.permiteDescargas == true) "Habilitado" else "Bloqueado",
                        isActive = user?.permiteDescargas == true
                    )
                }
            }
        }

        // Upgrade Promotion Card for Free users
        if (user?.esPremium == false) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KinemaxAccent.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Pásate a Vibefy Premium!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Descarga música sin límite y disfruta sin anuncios comerciales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onShowPremiumUpgrade,
                            colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Actualizar Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Settings / App Info
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Ajustes de la App",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(KinemaxBackground)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ajustes de Audio", fontWeight = FontWeight.Bold)
                            Text("Crossfade, Ecualizador y Presets", style = MaterialTheme.typography.bodySmall, color = KinemaxTextSecondary)
                        }
                        Button(
                            onClick = onAudioSettingsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent)
                        ) {
                            Text("Ajustar", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones Push (OneSignal)", fontWeight = FontWeight.Bold)
                            Text("Activo para alertas y novedades", style = MaterialTheme.typography.bodySmall, color = KinemaxTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Versión de la App", fontWeight = FontWeight.Bold)
                            Text("Vibefy v1.0 (Media3 ExoPlayer)", style = MaterialTheme.typography.bodySmall, color = KinemaxTextSecondary)
                        }
                    }
                }
            }
        }

        // Logout Button
        item {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KinemaxRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) KinemaxAccent else KinemaxTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isActive) KinemaxAccent else KinemaxGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) KinemaxAccent else KinemaxGold
            )
        }
    }
}
