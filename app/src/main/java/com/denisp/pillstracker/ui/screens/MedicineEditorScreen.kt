package com.denisp.pillstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.DateFormatter
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.RussianLocale
import com.denisp.pillstracker.ui.TimeFormatter
import com.denisp.pillstracker.ui.components.MedicineAppearance
import com.denisp.pillstracker.ui.toComposeColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private data class EditableScheduleTime(
    val minuteOfDay: Int,
    val dayMask: Int,
)

private enum class CourseEndMode(val title: String) {
    WITHOUT_END("Без окончания"),
    END_DATE("До даты"),
    DAYS_COUNT("Количество дней"),
}

private data class EditorStep(
    val title: String,
    val subtitle: String,
)

private val editorSteps = listOf(
    EditorStep("Лекарство", "Название, форма и цвет"),
    EditorStep("Дозировка", "Сколько принимать и сколько осталось"),
    EditorStep("Курс", "Период и схема приёма"),
    EditorStep("Время", "Когда напоминать"),
    EditorStep("Инструкция", "Еда, заметка и проверка"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineEditorScreen(
    initialMedicine: Medicine?,
    onBack: () -> Unit,
    onSave: (Medicine) -> Unit,
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var showValidation by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(initialMedicine?.name.orEmpty()) }
    var form by remember { mutableStateOf(initialMedicine?.form ?: MedicineForm.TABLET) }
    var pillShape by remember {
        mutableStateOf(initialMedicine?.pillShape ?: PillShape.ROUND)
    }
    var colorArgb by remember { mutableLongStateOf(initialMedicine?.colorArgb ?: MedicinePalette.first()) }
    var secondaryColorArgb by remember {
        mutableStateOf(initialMedicine?.secondaryColorArgb)
    }
    var dosageAmount by remember {
        mutableStateOf(initialMedicine?.dosageAmount?.displayAmount().orEmpty())
    }
    var dosageUnit by remember {
        mutableStateOf(initialMedicine?.dosageUnit ?: DosageUnit.MG)
    }
    var tabletsPerIntake by remember {
        mutableStateOf(initialMedicine?.tabletsPerIntake?.displayAmount() ?: "1")
    }
    var packageSize by remember {
        mutableStateOf(initialMedicine?.packageSize?.displayAmount().orEmpty())
    }
    var remaining by remember {
        mutableStateOf(initialMedicine?.remaining?.displayAmount().orEmpty())
    }
    var startEpochDay by remember {
        mutableLongStateOf((initialMedicine?.startDate ?: LocalDate.now()).toEpochDay())
    }
    var endEpochDay by remember {
        mutableLongStateOf(initialMedicine?.endDate?.toEpochDay() ?: LocalDate.now().plusDays(6).toEpochDay())
    }
    var courseEndMode by remember {
        mutableStateOf(
            if (initialMedicine?.endDate == null) CourseEndMode.WITHOUT_END else CourseEndMode.END_DATE,
        )
    }
    var courseDays by remember {
        mutableStateOf(
            initialMedicine?.endDate?.let {
                (ChronoUnit.DAYS.between(initialMedicine.startDate, it) + 1).toString()
            } ?: "7",
        )
    }
    var scheduleKind by remember {
        mutableStateOf(initialMedicine?.scheduleKind ?: ScheduleKind.DAILY)
    }
    val times = remember {
        mutableStateListOf<EditableScheduleTime>().apply {
            val source = initialMedicine?.times.orEmpty()
            if (source.isEmpty()) {
                add(EditableScheduleTime(8 * 60, ALL_DAYS_MASK))
            } else {
                addAll(source.map { EditableScheduleTime(it.minuteOfDay, it.dayMask) })
            }
        }
    }
    var mealTiming by remember {
        mutableStateOf(initialMedicine?.mealTiming ?: MealTiming.ANY)
    }
    var note by remember { mutableStateOf(initialMedicine?.note.orEmpty()) }

    val dosage = dosageAmount.replace(',', '.').toDoubleOrNull()
    val tablets = tabletsPerIntake.replace(',', '.').toDoubleOrNull()
    val pack = packageSize.replace(',', '.').toDoubleOrNull()
    val stock = remaining.replace(',', '.').toDoubleOrNull()
    val days = courseDays.toLongOrNull()
    val startDate = LocalDate.ofEpochDay(startEpochDay)
    val calculatedEndDate = when (courseEndMode) {
        CourseEndMode.WITHOUT_END -> null
        CourseEndMode.END_DATE -> LocalDate.ofEpochDay(endEpochDay)
        CourseEndMode.DAYS_COUNT -> days?.takeIf { it > 0 }?.let { startDate.plusDays(it - 1) }
    }

    fun isStepValid(step: Int): Boolean = when (step) {
        0 -> name.isNotBlank()
        1 -> dosage != null && dosage > 0 && tablets != null && tablets > 0 && pack != null && pack > 0 &&
            stock != null && stock >= 0
        2 -> calculatedEndDate?.isBefore(startDate) != true &&
            (courseEndMode != CourseEndMode.DAYS_COUNT || days != null && days > 0)
        3 -> scheduleKind == ScheduleKind.AS_NEEDED ||
            times.isNotEmpty() && times.all {
                scheduleKind != ScheduleKind.SELECTED_DAYS || it.dayMask != 0
            }
        else -> true
    }

    fun save() {
        onSave(
            Medicine(
                id = initialMedicine?.id ?: 0,
                name = name.trim(),
                form = form,
                pillShape = pillShape,
                colorArgb = colorArgb,
                secondaryColorArgb = secondaryColorArgb,
                dosageAmount = dosage ?: 0.0,
                dosageUnit = dosageUnit,
                tabletsPerIntake = tablets ?: 1.0,
                packageSize = pack ?: 0.0,
                remaining = stock ?: 0.0,
                mealTiming = mealTiming,
                note = note.trim(),
                startDate = startDate,
                endDate = calculatedEndDate,
                scheduleKind = scheduleKind,
                state = initialMedicine?.state ?: MedicineState.ACTIVE,
                times = if (scheduleKind == ScheduleKind.AS_NEEDED) {
                    emptyList()
                } else {
                    times.map {
                        ScheduleTime(
                            medicineId = initialMedicine?.id ?: 0,
                            minuteOfDay = it.minuteOfDay,
                            dayMask = if (scheduleKind == ScheduleKind.SELECTED_DAYS) {
                                it.dayMask
                            } else {
                                ALL_DAYS_MASK
                            },
                        )
                    }
                },
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialMedicine == null) "Новое лекарство" else "Редактирование") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Закрыть") }
                },
            )
        },
        bottomBar = {
            EditorNavigation(
                currentStep = currentStep,
                stepsCount = editorSteps.size,
                onPrevious = {
                    showValidation = false
                    currentStep--
                },
                onNext = {
                    if (isStepValid(currentStep)) {
                        showValidation = false
                        if (currentStep == editorSteps.lastIndex) save() else currentStep++
                    } else {
                        showValidation = true
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                StepHeader(currentStep)
                when (currentStep) {
                    0 -> BasicMedicineStep(
                        name = name,
                        onNameChanged = { name = it },
                        form = form,
                        onFormChanged = {
                            form = it
                            if (it == MedicineForm.CAPSULE) {
                                pillShape = PillShape.CAPSULE
                                if (secondaryColorArgb == null) {
                                    secondaryColorArgb = MedicinePalette[1]
                                }
                            }
                        },
                        pillShape = pillShape,
                        onPillShapeChanged = {
                            pillShape = it
                            if (it == PillShape.CAPSULE && secondaryColorArgb == null) {
                                secondaryColorArgb = MedicinePalette[1]
                            }
                        },
                        colorArgb = colorArgb,
                        onColorChanged = { colorArgb = it },
                        secondaryColorArgb = secondaryColorArgb,
                        onSecondaryColorChanged = { secondaryColorArgb = it },
                        showError = showValidation,
                    )
                    1 -> DosageStep(
                        dosageAmount = dosageAmount,
                        onDosageAmountChanged = { dosageAmount = it },
                        dosageUnit = dosageUnit,
                        onDosageUnitChanged = { dosageUnit = it },
                        tabletsPerIntake = tabletsPerIntake,
                        onTabletsChanged = { tabletsPerIntake = it },
                        packageSize = packageSize,
                        onPackageChanged = {
                            packageSize = it
                            if (initialMedicine == null) remaining = it
                        },
                        remaining = remaining,
                        onRemainingChanged = { remaining = it },
                        showError = showValidation,
                    )
                    2 -> CourseStep(
                        startDate = startDate,
                        onPickStartDate = {
                            showDatePicker(context, startDate) {
                                startEpochDay = it.toEpochDay()
                                if (LocalDate.ofEpochDay(endEpochDay).isBefore(it)) {
                                    endEpochDay = it.toEpochDay()
                                }
                            }
                        },
                        endMode = courseEndMode,
                        onEndModeChanged = { courseEndMode = it },
                        endDate = LocalDate.ofEpochDay(endEpochDay),
                        onPickEndDate = {
                            showDatePicker(context, LocalDate.ofEpochDay(endEpochDay)) {
                                endEpochDay = it.toEpochDay()
                            }
                        },
                        courseDays = courseDays,
                        onCourseDaysChanged = { courseDays = it.filter(Char::isDigit) },
                        scheduleKind = scheduleKind,
                        onScheduleKindChanged = { scheduleKind = it },
                        showError = showValidation,
                    )
                    3 -> TimeStep(
                        scheduleKind = scheduleKind,
                        times = times,
                        onAdd = { times.add(EditableScheduleTime(8 * 60, ALL_DAYS_MASK)) },
                        onChangeTime = { index ->
                            val current = times[index]
                            showTimePicker(context, current.minuteOfDay) {
                                times[index] = current.copy(minuteOfDay = it)
                            }
                        },
                        onToggleDay = { index, day ->
                            val current = times[index]
                            val bit = dayMask(day)
                            times[index] = current.copy(dayMask = current.dayMask xor bit)
                        },
                        onRemove = { index -> if (times.size > 1) times.removeAt(index) },
                        showError = showValidation,
                    )
                    4 -> DetailsStep(
                        mealTiming = mealTiming,
                        onMealTimingChanged = { mealTiming = it },
                        note = note,
                        onNoteChanged = { note = it },
                        name = name,
                        dosage = "${(dosage ?: 0.0).displayAmount()} ${dosageUnit.title}",
                        tabletsPerIntake = tablets ?: 0.0,
                        scheduleKind = scheduleKind,
                        times = times,
                        startDate = startDate,
                        endDate = calculatedEndDate,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StepHeader(currentStep: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            editorSteps.indices.forEach { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (index <= currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            CircleShape,
                        ),
                )
            }
        }
        Text(
            "Шаг ${currentStep + 1} из ${editorSteps.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            editorSteps[currentStep].title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            editorSteps[currentStep].subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EditorNavigation(
    currentStep: Int,
    stepsCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularArrowButton(
                symbol = "←",
                contentDescription = "Назад",
                enabled = currentStep > 0,
                onClick = onPrevious,
            )
            CircularArrowButton(
                symbol = if (currentStep == stepsCount - 1) "✓" else "→",
                contentDescription = if (currentStep == stepsCount - 1) "Сохранить" else "Далее",
                enabled = true,
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun CircularArrowButton(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(
            symbol,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier,
        )
    }
}

@Composable
private fun BasicMedicineStep(
    name: String,
    onNameChanged: (String) -> Unit,
    form: MedicineForm,
    onFormChanged: (MedicineForm) -> Unit,
    pillShape: PillShape,
    onPillShapeChanged: (PillShape) -> Unit,
    colorArgb: Long,
    onColorChanged: (Long) -> Unit,
    secondaryColorArgb: Long?,
    onSecondaryColorChanged: (Long?) -> Unit,
    showError: Boolean,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Название лекарства") },
        placeholder = { Text("Например, Витамин D") },
        singleLine = true,
        isError = showError && name.isBlank(),
        supportingText = if (showError && name.isBlank()) {
            { Text("Введите название") }
        } else {
            null
        },
    )
    SelectionField(
        label = "Форма",
        selected = form,
        options = MedicineForm.entries,
        onSelected = onFormChanged,
        title = MedicineForm::title,
    )
    SelectionField(
        label = "Форма таблетки",
        selected = pillShape,
        options = PillShape.entries,
        onSelected = onPillShapeChanged,
        title = PillShape::title,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Два цвета", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Для капсул и двухцветных таблеток",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = secondaryColorArgb != null,
            onCheckedChange = {
                onSecondaryColorChanged(if (it) MedicinePalette[1] else null)
            },
        )
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        MedicineAppearance(
            shape = pillShape,
            primaryColorArgb = colorArgb,
            secondaryColorArgb = secondaryColorArgb,
            size = 54.dp,
        )
    }
    MedicineColorPicker(
        title = if (secondaryColorArgb == null) "Цвет лекарства" else "Первая половина",
        selectedColor = colorArgb,
        onColorChanged = onColorChanged,
    )
    if (secondaryColorArgb != null) {
        MedicineColorPicker(
            title = "Вторая половина",
            selectedColor = secondaryColorArgb,
            onColorChanged = { onSecondaryColorChanged(it) },
        )
    }
}

@Composable
private fun DosageStep(
    dosageAmount: String,
    onDosageAmountChanged: (String) -> Unit,
    dosageUnit: DosageUnit,
    onDosageUnitChanged: (DosageUnit) -> Unit,
    tabletsPerIntake: String,
    onTabletsChanged: (String) -> Unit,
    packageSize: String,
    onPackageChanged: (String) -> Unit,
    remaining: String,
    onRemainingChanged: (String) -> Unit,
    showError: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = dosageAmount,
            onValueChange = onDosageAmountChanged,
            modifier = Modifier.weight(1f),
            label = { Text("Дозировка") },
            placeholder = { Text("Например, 60") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = showError && (dosageAmount.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0,
        )
        SelectionField(
            label = "Единица",
            selected = dosageUnit,
            options = DosageUnit.entries,
            onSelected = onDosageUnitChanged,
            title = DosageUnit::title,
            modifier = Modifier.width(128.dp),
        )
    }
    DecimalField(
        value = tabletsPerIntake,
        onValueChanged = onTabletsChanged,
        label = "Таблеток за один приём",
        showError = showError,
    )
    DecimalField(
        value = packageSize,
        onValueChanged = onPackageChanged,
        label = "Таблеток в полной упаковке",
        showError = showError,
    )
    DecimalField(
        value = remaining,
        onValueChanged = onRemainingChanged,
        label = "Сейчас осталось",
        showError = showError,
    )
    Card(shape = RoundedCornerShape(18.dp)) {
        Text(
            "Напоминание о покупке появится, когда останется не больше трёх приёмов.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CourseStep(
    startDate: LocalDate,
    onPickStartDate: () -> Unit,
    endMode: CourseEndMode,
    onEndModeChanged: (CourseEndMode) -> Unit,
    endDate: LocalDate,
    onPickEndDate: () -> Unit,
    courseDays: String,
    onCourseDaysChanged: (String) -> Unit,
    scheduleKind: ScheduleKind,
    onScheduleKindChanged: (ScheduleKind) -> Unit,
    showError: Boolean,
) {
    DateButton("Дата начала", startDate, onPickStartDate)
    SelectionField(
        label = "Продолжительность курса",
        selected = endMode,
        options = CourseEndMode.entries,
        onSelected = onEndModeChanged,
        title = CourseEndMode::title,
    )
    when (endMode) {
        CourseEndMode.WITHOUT_END -> Unit
        CourseEndMode.END_DATE -> DateButton("Дата окончания", endDate, onPickEndDate)
        CourseEndMode.DAYS_COUNT -> OutlinedTextField(
            value = courseDays,
            onValueChange = onCourseDaysChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Количество дней") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = showError && (courseDays.toLongOrNull() ?: 0) <= 0,
        )
    }
    HorizontalDivider()
    SelectionField(
        label = "Схема приёма",
        selected = scheduleKind,
        options = ScheduleKind.entries,
        onSelected = onScheduleKindChanged,
        title = ScheduleKind::title,
    )
    if (scheduleKind == ScheduleKind.EVERY_OTHER_DAY) {
        Text(
            "Отсчёт «через день» начинается с даты начала курса.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimeStep(
    scheduleKind: ScheduleKind,
    times: List<EditableScheduleTime>,
    onAdd: () -> Unit,
    onChangeTime: (Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    showError: Boolean,
) {
    if (scheduleKind == ScheduleKind.AS_NEEDED) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Без фиксированного времени", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Приём можно будет отметить вручную на главном экране.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    times.forEachIndexed { index, schedule ->
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { onChangeTime(index) }) {
                        Text(
                            LocalTime.of(schedule.minuteOfDay / 60, schedule.minuteOfDay % 60)
                                .format(TimeFormatter),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    if (times.size > 1) {
                        TextButton(onClick = { onRemove(index) }) { Text("Удалить") }
                    }
                }
                if (scheduleKind == ScheduleKind.SELECTED_DAYS) {
                    Text("Дни для этого времени", fontWeight = FontWeight.Medium)
                    DaySelector(schedule.dayMask) { day -> onToggleDay(index, day) }
                    if (showError && schedule.dayMask == 0) {
                        Text("Выберите хотя бы один день", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
        Text("+ Добавить время")
    }
}

@Composable
private fun DetailsStep(
    mealTiming: MealTiming,
    onMealTimingChanged: (MealTiming) -> Unit,
    note: String,
    onNoteChanged: (String) -> Unit,
    name: String,
    dosage: String,
    tabletsPerIntake: Double,
    scheduleKind: ScheduleKind,
    times: List<EditableScheduleTime>,
    startDate: LocalDate,
    endDate: LocalDate?,
) {
    SelectionField(
        label = "Связь с едой",
        selected = mealTiming,
        options = MealTiming.entries,
        onSelected = onMealTimingChanged,
        title = MealTiming::title,
    )
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        label = { Text("Заметка") },
        placeholder = { Text("Например, запивать стаканом воды") },
    )
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Проверьте назначение", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            SummaryLine("Лекарство", name)
            SummaryLine("Дозировка", "$dosage · ${tabletsPerIntake.displayAmount()} шт.")
            SummaryLine(
                "Курс",
                if (endDate == null) {
                    "с ${startDate.format(DateFormatter)}, без окончания"
                } else {
                    "${startDate.format(DateFormatter)} — ${endDate.format(DateFormatter)}"
                },
            )
            SummaryLine(
                "Расписание",
                if (scheduleKind == ScheduleKind.AS_NEEDED) {
                    scheduleKind.title
                } else {
                    "${scheduleKind.title}, ${times.size} раз(а) в день"
                },
            )
        }
    }
}

@Composable
private fun DaySelector(mask: Int, onToggle: (Int) -> Unit) {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            val selected = mask and dayMask(index + 1) != 0
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                shape = CircleShape,
                modifier = Modifier.clickable { onToggle(index + 1) },
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun DateButton(label: String, date: LocalDate, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(date.format(DateFormatter), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DecimalField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    showError: Boolean,
) {
    val parsed = value.replace(',', '.').toDoubleOrNull()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = showError && (parsed == null || parsed < 0),
    )
}

@Composable
private fun <T> SelectionField(
    label: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    title: (T) -> String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = modifier,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                    Text(title(selected), style = MaterialTheme.typography.bodyLarge)
                }
                Text("⌄")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(title(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MedicineColorPicker(
    title: String,
    selectedColor: Long,
    onColorChanged: (Long) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MedicinePalette.forEach { color ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (color == selectedColor) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            Color.Transparent
                        },
                        CircleShape,
                    )
                    .padding(3.dp)
                    .background(color.toComposeColor(), CircleShape)
                    .clickable { onColorChanged(color) },
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initial: LocalDate,
    onSelected: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onSelected(LocalDate.of(year, month + 1, day))
        },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

private fun showTimePicker(
    context: android.content.Context,
    initialMinute: Int,
    onSelected: (Int) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onSelected(hour * 60 + minute) },
        initialMinute / 60,
        initialMinute % 60,
        true,
    ).show()
}
