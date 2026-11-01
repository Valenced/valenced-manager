package com.aliucord.manager.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliucord.manager.manager.InstallerSetting
import com.aliucord.manager.manager.toDisplayName
import app.valenced.manager.R

@Composable
fun InstallerDialog(
    currentInstaller: InstallerSetting,
    onDismissRequest: () -> Unit,
    onConfirm: (InstallerSetting) -> Unit,
) {
    var selectedInstaller by rememberSaveable { mutableStateOf(currentInstaller) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_tools),
                contentDescription = stringResource(R.string.setting_installer),
                modifier = Modifier.size(32.dp),
            )
        },
        title = { Text(stringResource(R.string.setting_installer)) },
        text = {
            Column {
                for (installer in InstallerSetting.values()) key(installer) {
                    val interactionSource = remember(::MutableInteractionSource)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource,
                                onClick = { selectedInstaller = installer },
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                    ) {
                        // Optionally, you can add a unique icon for each installer type
                        Icon(
                            painter = painterResource(
                                when (installer) {
                                    InstallerSetting.PM -> R.drawable.ic_android
                                    InstallerSetting.ROOT -> R.drawable.ic_hashtag
                                    InstallerSetting.Shizuku -> R.drawable.ic_shizuku
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(26.dp),
                        )

                        Text(
                            text = installer.toDisplayName(),
                            style = MaterialTheme.typography.labelLarge
                                .copy(fontSize = 14.sp)
                        )

                        Spacer(Modifier.weight(1f, true))

                        RadioButton(
                            selected = installer == selectedInstaller,
                            onClick = { selectedInstaller = installer },
                            interactionSource = interactionSource,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedInstaller)
                    onDismissRequest()
                },
            ) {
                Text(stringResource(R.string.action_apply))
            }
        },
    )
}
