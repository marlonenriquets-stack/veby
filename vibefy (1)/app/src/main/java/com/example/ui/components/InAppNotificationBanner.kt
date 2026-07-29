package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InAppNotification
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationBanner(
    notification: InAppNotification?,
    onDismiss: () -> Unit,
    onNotificationClick: (InAppNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(5000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { notif ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onNotificationClick(notif)
                        onDismiss()
                    }
                    .testTag("in_app_notification_banner"),
                color = KinemaxSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, KinemaxAccent.copy(alpha = 0.8f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconVector = when (notif.type) {
                        "music" -> Icons.Default.MusicNote
                        "promo" -> Icons.Default.Star
                        else -> Icons.Default.Notifications
                    }

                    Surface(
                        color = KinemaxAccent.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = KinemaxAccent,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PUSH / IN-APP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KinemaxAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(KinemaxAccent)
                            )
                        }

                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar notificación",
                            tint = KinemaxTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
