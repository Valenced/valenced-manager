package com.aliucord.manager.ui.screens.patchopts.components.options

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SelectPatchOption(
    icon: Painter,
    name: String,
    description: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember(::MutableInteractionSource)

    IconPatchOption(
        icon = icon,
        name = name,
        description = description,
        modifier = modifier.fillMaxWidth()
    ) {
        FilledTonalButton(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            interactionSource = interactionSource,
        ) {
            Text(
                text = selectedOption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            expanded = false
                            onOptionSelected(option)
                        },
                        enabled = enabled,
                    )
                }
            }
        }
    }
}
