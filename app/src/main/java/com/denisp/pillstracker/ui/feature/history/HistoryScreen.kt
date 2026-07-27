package com.denisp.pillstracker.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.model.TrackerSnapshot
import com.denisp.pillstracker.notifications.NotificationScheduler
import com.denisp.pillstracker.ui.theme.AppScreenHeader
import com.denisp.pillstracker.ui.theme.AppSurfaceCard
import java.time.LocalDate

private enum class HistorySection(val title: String) {
    STATISTICS("Статистика"),
    INTAKES("Приёмы"),
}

@Composable
fun HistoryScreen(
    snapshot: TrackerSnapshot,
    repository: TrackerRepository,
    scheduler: NotificationScheduler,
) {
    var section by remember { mutableStateOf(HistorySection.STATISTICS) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
        ) {
            AppScreenHeader(
                title = "История",
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
            )
            HistorySectionSelector(
                selected = section,
                onSelected = { section = it },
                modifier = Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (section) {
                    HistorySection.STATISTICS -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 20.dp,
                                top = 14.dp,
                                end = 20.dp,
                                bottom = 40.dp,
                            ),
                        ) {
                            item {
                                HistoryStatistics(
                                    snapshot = snapshot,
                                    repository = repository,
                                    onDateSelected = { date ->
                                        selectedDate = date
                                        section = HistorySection.INTAKES
                                    },
                                )
                            }
                        }
                    }

                    HistorySection.INTAKES -> {
                        HistoryIntakesContent(
                            snapshot = snapshot,
                            repository = repository,
                            scheduler = scheduler,
                            selectedDate = selectedDate,
                            onSelectedDateChanged = { selectedDate = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySectionSelector(
    selected: HistorySection,
    onSelected: (HistorySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HistorySection.entries.forEach { section ->
                val isSelected = selected == section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            shape = CircleShape,
                        )
                        .clickable { onSelected(section) }
                        .padding(vertical = 11.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = section.title,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}
