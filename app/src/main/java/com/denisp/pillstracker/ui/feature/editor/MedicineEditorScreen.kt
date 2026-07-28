package com.denisp.pillstracker.ui.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.DosageUnit
import com.denisp.pillstracker.model.DEFAULT_MEDICINE_BACKGROUND_ARGB
import com.denisp.pillstracker.model.MealTiming
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.MedicineState
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.ScheduleKind
import com.denisp.pillstracker.model.ScheduleTime
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.MedicinePalette
import com.denisp.pillstracker.ui.components.AppDatePickerDialog
import com.denisp.pillstracker.ui.components.AppTimePickerDialog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private enum class CourseDatePickerTarget(val title: String) {
    START("Дата начала курса"),
    END("Дата окончания курса"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineEditorScreen(
    initialMedicine: Medicine?,
    onBack: () -> Unit,
    onSave: (Medicine) -> Unit,
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var showValidation by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<CourseDatePickerTarget?>(null) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }

    var name by remember { mutableStateOf(initialMedicine?.name.orEmpty()) }
    var form by remember { mutableStateOf(initialMedicine?.form ?: MedicineForm.TABLET) }
    var pillShape by remember { mutableStateOf(initialMedicine?.pillShape ?: PillShape.ROUND) }
    var colorArgb by remember { mutableLongStateOf(initialMedicine?.colorArgb ?: MedicinePalette.first()) }
    var secondaryColorArgb by remember { mutableStateOf(initialMedicine?.secondaryColorArgb) }
    var secondaryColorAutomaticallyEnabled by remember { mutableStateOf(false) }
    var backgroundColorArgb by remember {
        mutableLongStateOf(initialMedicine?.backgroundColorArgb ?: DEFAULT_MEDICINE_BACKGROUND_ARGB)
    }
    var dosageAmount by remember {
        mutableStateOf(initialMedicine?.dosageAmount?.displayAmount().orEmpty())
    }
    var dosageUnit by remember { mutableStateOf(initialMedicine?.dosageUnit ?: DosageUnit.MG) }
    var tabletsPerIntake by remember {
        mutableStateOf(initialMedicine?.tabletsPerIntake?.displayAmount() ?: "1")
    }
    var packageSize by remember { mutableStateOf(initialMedicine?.packageSize?.displayAmount().orEmpty()) }
    var remaining by remember { mutableStateOf(initialMedicine?.remaining?.displayAmount().orEmpty()) }
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
    var mealTiming by remember { mutableStateOf(initialMedicine?.mealTiming ?: MealTiming.ANY) }
    var note by remember {
        mutableStateOf(limitMedicineNote(initialMedicine?.note.orEmpty()))
    }

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
        1 -> dosage != null && dosage > 0 && tablets != null && tablets > 0 &&
            pack != null && pack > 0 && stock != null && stock >= 0
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
                backgroundColorArgb = backgroundColorArgb,
                dosageAmount = dosage ?: 0.0,
                dosageUnit = dosageUnit,
                tabletsPerIntake = tablets ?: 1.0,
                packageSize = pack ?: 0.0,
                remaining = stock ?: 0.0,
                mealTiming = mealTiming,
                note = limitMedicineNote(note.trim()),
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

    BackHandler {
        if (currentStep > 0) {
            showValidation = false
            currentStep--
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (initialMedicine == null) "Новое лекарство" else "Редактирование",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier
                            .width(88.dp)
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = "Закрыть",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                actions = { Spacer(Modifier.width(88.dp)) },
            )
        },
        bottomBar = {
            EditorNavigation(
                currentStep = currentStep,
                stepsCount = editorSteps.size,
                onPrevious = {
                    if (currentStep > 0) {
                        showValidation = false
                        currentStep--
                    }
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
                            val previousForm = form
                            val colorTransition = secondaryColorAfterFormChange(
                                previousForm = previousForm,
                                selectedForm = it,
                                currentSecondaryColor = secondaryColorArgb,
                                wasAutomaticallyEnabledForCapsule = secondaryColorAutomaticallyEnabled,
                                defaultSecondaryColor = MedicinePalette[1],
                            )
                            secondaryColorArgb = colorTransition.color
                            secondaryColorAutomaticallyEnabled =
                                colorTransition.automaticallyEnabledForCapsule
                            form = it
                            if (it == MedicineForm.TABLET && pillShape == PillShape.CAPSULE) {
                                pillShape = PillShape.ROUND
                            }
                        },
                        pillShape = pillShape,
                        onPillShapeChanged = { pillShape = it },
                        colorArgb = colorArgb,
                        onColorChanged = { colorArgb = it },
                        secondaryColorArgb = secondaryColorArgb,
                        onSecondaryColorChanged = {
                            secondaryColorArgb = it
                            secondaryColorAutomaticallyEnabled = false
                        },
                        backgroundColorArgb = backgroundColorArgb,
                        onBackgroundColorChanged = { backgroundColorArgb = it },
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
                        onPickStartDate = { datePickerTarget = CourseDatePickerTarget.START },
                        endMode = courseEndMode,
                        onEndModeChanged = { courseEndMode = it },
                        endDate = LocalDate.ofEpochDay(endEpochDay),
                        onPickEndDate = { datePickerTarget = CourseDatePickerTarget.END },
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
                        onChangeTime = { index -> editingTimeIndex = index },
                        onToggleDay = { index, day ->
                            val current = times[index]
                            times[index] = current.copy(dayMask = current.dayMask xor dayMask(day))
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

    datePickerTarget?.let { target ->
        val selectedDate = when (target) {
            CourseDatePickerTarget.START -> startDate
            CourseDatePickerTarget.END -> LocalDate.ofEpochDay(endEpochDay)
        }
        AppDatePickerDialog(
            title = target.title,
            selectedDate = selectedDate,
            minDate = if (target == CourseDatePickerTarget.END) startDate else null,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { selected ->
                when (target) {
                    CourseDatePickerTarget.START -> {
                        startEpochDay = selected.toEpochDay()
                        if (LocalDate.ofEpochDay(endEpochDay).isBefore(selected)) {
                            endEpochDay = selected.toEpochDay()
                        }
                    }

                    CourseDatePickerTarget.END -> endEpochDay = selected.toEpochDay()
                }
                datePickerTarget = null
            },
        )
    }

    editingTimeIndex?.let { index ->
        val current = times[index]
        AppTimePickerDialog(
            title = if (times.size > 1) "Время приёма ${index + 1}" else "Время приёма",
            initialMinuteOfDay = current.minuteOfDay,
            onDismiss = { editingTimeIndex = null },
            onTimeSelected = { selectedMinute ->
                times[index] = current.copy(minuteOfDay = selectedMinute)
                editingTimeIndex = null
            },
        )
    }
}
