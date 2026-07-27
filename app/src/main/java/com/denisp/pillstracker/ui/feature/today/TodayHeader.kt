package com.denisp.pillstracker.ui.feature.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Image(
            painter = painterResource(greetingArtwork(period)),
            contentDescription = null,
            modifier = Modifier.size(104.dp),
            contentScale = ContentScale.Fit,
        )
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
