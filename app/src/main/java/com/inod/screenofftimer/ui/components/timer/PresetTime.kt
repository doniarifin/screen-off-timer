package com.inod.screenofftimer.ui.components.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inod.screenofftimer.viewmodel.TimerViewModel

@Composable
fun PresetTime() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val viewModel: TimerViewModel = viewModel()

        val isRunning by viewModel.getRunning.collectAsState()
        val timeLeftSeconds = viewModel.leftSeconds
        val totalMinutes = (timeLeftSeconds / 60)

        FlowRow(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            listOf(5, 10, 30, 45).forEach { min ->
                val isSelected = totalMinutes == min

                FilledTonalButton(
                    onClick = { viewModel.setTimer(min) },
                    enabled = !isRunning,
                    modifier = Modifier
                        .width(140.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "$min",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}