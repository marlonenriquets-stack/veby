package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionPlan
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxSurfaceVariant
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun PremiumDialog(
    isOpen: Boolean,
    plans: List<SubscriptionPlan>,
    onDismiss: () -> Unit,
    onSelectPlan: (SubscriptionPlan) -> Unit
) {
    if (!isOpen) return

    // Solo mostramos planes de pago en el paywall (el Free no tiene nada que "comprar")
    val paidPlans = plans.filter { it.precio > 0.0 }
    var selectedPlanId by remember(plans) { mutableStateOf(paidPlans.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .testTag("premium_dialog"),
        containerColor = KinemaxSurface,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(KinemaxGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Vibefy Premium",
                        tint = KinemaxGold,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Vibefy Premium",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    plans.isEmpty() -> {
                        // Aún no han cargado los planes desde el servidor (o el servidor no tiene ninguno)
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KinemaxAccent, modifier = Modifier.size(28.dp))
                        }
                    }
                    paidPlans.isEmpty() -> {
                        Text(
                            text = "No hay planes de pago disponibles todavía. Vuelve a intentarlo más tarde.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KinemaxTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "Elige tu plan:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KinemaxTextSecondary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(paidPlans) { plan ->
                                PlanCard(
                                    plan = plan,
                                    isSelected = plan.id == selectedPlanId,
                                    onClick = { selectedPlanId = plan.id }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val selectedPlan = paidPlans.find { it.id == selectedPlanId }
            Button(
                onClick = { selectedPlan?.let(onSelectPlan) },
                enabled = selectedPlan != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premium_upgrade_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent)
            ) {
                Text(
                    text = if (selectedPlan != null) "Suscribirme a ${selectedPlan.nombre}" else "Selecciona un plan",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ahora no", color = KinemaxTextSecondary)
            }
        }
    )
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) KinemaxAccent.copy(alpha = 0.12f) else KinemaxSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) KinemaxAccent else KinemaxTextSecondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = plan.formattedPrecio + "/${plan.periodo ?: "mes"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KinemaxGold
            )
        }

        if (!plan.descripcion.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = KinemaxTextSecondary
            )
        }

        val beneficios = plan.beneficios ?: emptyList()
        if (beneficios.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            beneficios.forEach { beneficio ->
                PremiumBenefitRow(text = beneficio)
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            if (plan.quitaAnuncios) PremiumBenefitRow(text = "Sin anuncios")
            if (plan.permiteDescargas) PremiumBenefitRow(text = "Descargas offline")
        }

        if (!plan.hasSkuConfigured) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚠️ Este plan aún no tiene SKU de Google Play configurado",
                style = MaterialTheme.typography.labelSmall,
                color = KinemaxGold
            )
        }
    }
}

@Composable
private fun PremiumBenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = KinemaxAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
