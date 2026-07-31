package com.denisp.pillstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.denisp.pillstracker.ui.asTime
import kotlinx.coroutines.delay

@Composable
internal fun rememberMinuteNow(): Long {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            delay(MILLIS_PER_MINUTE - now % MILLIS_PER_MINUTE)
            nowMillis = System.currentTimeMillis()
        }
    }
    return nowMillis
}

@Composable
internal fun OverdueDoseLabel(
    scheduledAt: Long,
    modifier: Modifier = Modifier,
) {
    val errorColor = MaterialTheme.colorScheme.error
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = errorColor,
                    fontWeight = FontWeight.SemiBold,
                ),
            ) {
                append("Просрочено")
            }
            withStyle(SpanStyle(color = secondaryColor)) {
                append(" · время приёма ${scheduledAt.asTime()}")
            }
        },
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
    )
}

private const val MILLIS_PER_MINUTE = 60_000L
