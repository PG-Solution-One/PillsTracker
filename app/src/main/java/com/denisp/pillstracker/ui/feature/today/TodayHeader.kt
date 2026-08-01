package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.R
import com.denisp.pillstracker.ui.FullDateFormatter
import java.time.LocalDate

@Composable
internal fun TodayGreetingHeader(
    userName: String,
    date: LocalDate,
    period: GreetingPeriod,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = greetingText(userName, period),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = date.format(FullDateFormatter).replaceFirstChar { it.uppercase() },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GreetingArtwork(period)
    }
}

@Composable
private fun GreetingArtwork(period: GreetingPeriod) {
    val colors = MaterialTheme.colorScheme
    val accent = when (period) {
        GreetingPeriod.MORNING -> colors.primary
        GreetingPeriod.DAY -> colors.secondary
        GreetingPeriod.EVENING -> colors.tertiary
        GreetingPeriod.NIGHT -> colors.tertiary
    }
    val backdropColor = lerp(colors.surfaceContainerHighest, accent, 0.12f)

    Surface(
        modifier = Modifier.size(112.dp),
        shape = RoundedCornerShape(32.dp),
        color = backdropColor,
        tonalElevation = 3.dp,
        shadowElevation = 5.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant.copy(alpha = 0.78f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(greetingArtwork(period)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun greetingText(userName: String, period: GreetingPeriod): String {
    val greeting = when (period) {
        GreetingPeriod.MORNING -> "Доброе утро"
        GreetingPeriod.DAY -> "Добрый день"
        GreetingPeriod.EVENING -> "Добрый вечер"
        GreetingPeriod.NIGHT -> "Доброй ночи"
    }
    val name = userName.trim()
    return if (name.isEmpty()) "$greeting!" else "$greeting,\n$name!"
}

private fun greetingArtwork(period: GreetingPeriod): Int = when (period) {
    GreetingPeriod.MORNING -> R.drawable.greeting_morning
    GreetingPeriod.DAY -> R.drawable.greeting_day
    GreetingPeriod.EVENING -> R.drawable.greeting_evening
    GreetingPeriod.NIGHT -> R.drawable.greeting_night
}
