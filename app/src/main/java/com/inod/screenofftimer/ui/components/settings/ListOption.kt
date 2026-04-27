package com.inod.screenofftimer.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.ChipPadding

@Composable
fun ListOption(
    title: String? = null,
    description: String? = null,
    enabled: Boolean? = true,
    icon: ImageVector? = null,
    contentIcon: String? = null,
    padding: PaddingValues? = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    onClick: () -> Unit,
    bgColor: Color? = MaterialTheme.colorScheme.surfaceContainerHigh,
    trailing: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape= RoundedCornerShape(5.dp))
            .background(bgColor!!)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled!!,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { onClick() }
                .padding(padding!!),
            verticalAlignment = Alignment.CenterVertically) {

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentIcon,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (title !== null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(20.dp))
                    if (description != null) {
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }

            }

            trailing?.invoke()
            bottomContent?.invoke()
        }

    }
}