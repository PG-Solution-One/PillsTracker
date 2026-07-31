package com.denisp.pillstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.ui.asTime
import java.time.Instant
import java.time.ZoneId

@Composable
internal fun ScheduledTimeBadge(
    scheduledAt: Long,
    modifier: Modifier = Modifier,
) {
    val time = scheduledAt.asTime()
    val hour = Instant.ofEpochMilli(scheduledAt)
        .atZone(ZoneId.systemDefault())
        .hour
    val periodIcon = when (hour) {
        in 5..11 -> Icons.Rounded.LightMode
        in 12..17 -> Icons.Rounded.WbSunny
        in 18..22 -> Icons.Rounded.WbTwilight
        else -> Icons.Rounded.NightsStay
    }

    Surface(
        modifier = modifier
            .widthIn(min = 112.dp)
            .semantics { contentDescription = "Время приёма $time" },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = periodIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = time,
                modifier = Modifier.padding(start = 7.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
