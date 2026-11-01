package com.aliucord.manager.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.valenced.manager.R

@Composable
fun ShizukuInfoDialog(
    onDismissRequest: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_tools),
                contentDescription = "Shizuku",
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Shizuku Permission Required",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    "To use Shizuku, you must manually grant permission:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    "1. Open the Shizuku app\n" +
                    "2. Grant permissions in the Shizuku app\n" +
                    "3. Relaunch Shiggy Manager to apply changes\n\n",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        val intent = android.content.Intent().apply {
                            setClassName(
                                "moe.shizuku.manager.authorization.RequestPermissionActivity",
                                "moe.shizuku.privileged.api"
                            )
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val fallback = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (fallback != null) {
                                context.startActivity(fallback)
                            }
                        }
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Open Shizuku App")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Okay")
                }
            }
        },
        dismissButton = {}
    )
}
