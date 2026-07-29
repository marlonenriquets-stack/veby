package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.KinemaxSurfaceVariant
import com.example.ui.theme.KinemaxTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsCenterBottomSheet(
    notifications: List<InAppNotification>,
    unreadCount: Int,
    onDismiss: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onClearAll: () -> Unit,
    onNotificationClick: (InAppNotification) -> Unit,
    onSimulatePushClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KinemaxSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("notifications_center_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = KinemaxAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = KinemaxAccent,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$unreadCount",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = onMarkAllAsRead) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todas leídas",
                                tint = KinemaxAccent
                            )
                        }
                        IconButton(onClick = onClearAll) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar notificaciones",
                                tint = KinemaxTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // OneSignal SDK status banner & simulation trigger
            Surface(
                color = KinemaxSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Push SDK OneSignal Activo",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = KinemaxAccent
                        )
                        Text(
                            text = "Recibe avisos en tiempo real e In-App Messages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary
                        )
                    }
                    Button(
                        onClick = onSimulatePushClick,
                        colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Probar", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = KinemaxTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tienes notificaciones por ahora",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KinemaxTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationItemRow(
                            notification = notif,
                            onClick = {
                                onNotificationClick(notif)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: InAppNotification,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(notification.timestampMs))

    Surface(
        color = if (notification.isRead) KinemaxSurfaceVariant.copy(alpha = 0.5f) else KinemaxSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            val iconVector = when (notification.type) {
                "music" -> Icons.Default.MusicNote
                "promo" -> Icons.Default.Star
                else -> Icons.Default.Notifications
            }

            Surface(
                color = if (notification.isRead) KinemaxTextSecondary.copy(alpha = 0.2f) else KinemaxAccent.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (notification.isRead) KinemaxTextSecondary else KinemaxAccent,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (notification.isRead) KinemaxTextSecondary else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = KinemaxTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KinemaxTextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.songId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = KinemaxAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Toca para reproducir canción asociada",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KinemaxAccent
                        )
                    }
                }
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KinemaxAccent)
                )
            }
        }
    }
}
