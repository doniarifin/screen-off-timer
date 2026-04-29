package com.inod.screenofftimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ModalDialog(
    show: Boolean,
    title: String,
    description: String,
    confirmText: String = "Agree",
    dismissText: String = "Cancel",
    leftButton: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onLeftButton: (() -> Unit)? = null,
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                colors = CardDefaults.cardColors(
                ),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    //text
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    //desc
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = TextUnit.Unspecified
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (leftButton != null && onLeftButton != null) {
                            TextButton(onClick = onLeftButton) {
                                Text(leftButton, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        //actions btn
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    dismissText, color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            TextButton(onClick = onConfirm) {
                                Text(confirmText, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

        }
    }
}