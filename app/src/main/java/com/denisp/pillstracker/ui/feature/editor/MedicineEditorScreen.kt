package com.denisp.pillstracker.ui.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denisp.pillstracker.model.ALL_DAYS_MASK
import com.denisp.pillstracker.model.Medicine
import com.denisp.pillstracker.model.MedicineForm
import com.denisp.pillstracker.model.PillShape
import com.denisp.pillstracker.model.dayMask
import com.denisp.pillstracker.model.displayAmount
import com.denisp.pillstracker.ui.components.AppDatePickerDialog
import com.denisp.pillstracker.ui.components.AppTimePickerDialog
import com.denisp.pillstracker.ui.theme.AppSpacing
import java.time.LocalDate

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
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val isEditing = initialMedicine != null
    val initialDraft = remember(initialMedicine) { MedicineEditorDraft.from(initialMedicine) }
    var draft by remember(initialMedicine) { mutableStateOf(initialDraft) }
    var currentStep by remember(initialMedicine) { mutableIntStateOf(0) }
    var selectedEditSection by remember(initialMedicine) {
        mutableStateOf<MedicineEditSection?>(null)
    }
    var showValidation by remember { mutableStateOf(false) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<CourseDatePickerTarget?>(null) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }
    val editorScrollState = rememberScrollState()
    val hasChanges = draft != initialDraft
    val activeStep = selectedEditSection?.stepIndex ?: currentStep

    LaunchedEffect(activeStep) {
        editorScrollState.scrollTo(0)
    }

    fun requestClose() {
        if (isEditing && hasChanges) {
            showDiscardConfirmation = true
        } else {
            onBack()
        }
    }

    fun save() {
        val invalidStep = draft.firstInvalidStep()
        if (invalidStep == null) {
            onSave(draft.toMedicine(initialMedicine))
        } else {
            showValidation = true
            if (isEditing) {
                selectedEditSection = MedicineEditSection.fromStep(invalidStep)
            } else {
                currentStep = invalidStep
            }
        }
    }

    fun returnToOverview() {
        showValidation = false
        selectedEditSection = null
    }

    BackHandler {
        when {
            isEditing && selectedEditSection != null -> returnToOverview()
            isEditing -> requestClose()
            currentStep > 0 -> {
                showValidation = false
                currentStep--
            }
            else -> onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = if (isEditing) "Редактирование" else "Новое лекарство",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.width(88.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (isEditing && selectedEditSection != null) {
                            IconButton(onClick = ::returnToOverview) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "К разделам",
                                )
                            }
                        } else {
                            TextButton(
                                onClick = if (isEditing) ::requestClose else onBack,
                                modifier = Modifier.heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                Text(
                                    text = "Закрыть",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                },
                actions = { Spacer(Modifier.width(88.dp)) },
            )
        },
        bottomBar = {
            if (!isImeVisible) {
                if (isEditing) {
                    EditSaveNavigation(
                        enabled = hasChanges,
                        onSave = ::save,
                    )
                } else {
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
                            if (draft.isStepValid(currentStep)) {
                                showValidation = false
                                if (currentStep == editorSteps.lastIndex) save() else currentStep++
                            } else {
                                showValidation = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .verticalScroll(editorScrollState)
                    .padding(
                        start = AppSpacing.Screen,
                        top = AppSpacing.Lg,
                        end = AppSpacing.Screen,
                        bottom = if (isImeVisible) AppSpacing.Sm else AppSpacing.Lg,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Lg),
            ) {
                if (isEditing && selectedEditSection == null) {
                    MedicineEditOverview(
                        draft = draft,
                        onSelect = { section ->
                            showValidation = false
                            selectedEditSection = section
                        },
                    )
                } else {
                    if (isEditing) {
                        EditSectionHeader(requireNotNull(selectedEditSection))
                    } else {
                        StepHeader(currentStep)
                    }
                    when (activeStep) {
                        0 -> BasicMedicineStep(
                            name = draft.name,
                            onNameChanged = { draft = draft.copy(name = it) },
                            form = draft.form,
                            onFormChanged = { selectedForm ->
                                draft = draft.copy(
                                    form = selectedForm,
                                    secondaryColorArgb = secondaryColorAfterFormChange(
                                        selectedForm = selectedForm,
                                        currentSecondaryColor = draft.secondaryColorArgb,
                                    ),
                                    pillShape = if (
                                        selectedForm == MedicineForm.TABLET &&
                                        draft.pillShape == PillShape.CAPSULE
                                    ) {
                                        PillShape.ROUND
                                    } else {
                                        draft.pillShape
                                    },
                                )
                            },
                            pillShape = draft.pillShape,
                            onPillShapeChanged = { draft = draft.copy(pillShape = it) },
                            colorArgb = draft.colorArgb,
                            onColorChanged = { draft = draft.copy(colorArgb = it) },
                            secondaryColorArgb = draft.secondaryColorArgb,
                            onSecondaryColorChanged = {
                                draft = draft.copy(secondaryColorArgb = it)
                            },
                            backgroundColorArgb = draft.backgroundColorArgb,
                            onBackgroundColorChanged = {
                                draft = draft.copy(backgroundColorArgb = it)
                            },
                            showError = showValidation,
                        )

                        1 -> DosageStep(
                            dosageAmount = draft.dosageAmount,
                            onDosageAmountChanged = { draft = draft.copy(dosageAmount = it) },
                            dosageUnit = draft.dosageUnit,
                            onDosageUnitChanged = { draft = draft.copy(dosageUnit = it) },
                            tabletsPerIntake = draft.tabletsPerIntake,
                            onTabletsChanged = { draft = draft.copy(tabletsPerIntake = it) },
                            packageSize = draft.packageSize,
                            onPackageChanged = {
                                draft = draft.copy(
                                    packageSize = it,
                                    remaining = if (isEditing) draft.remaining else it,
                                )
                            },
                            remaining = draft.remaining,
                            onRemainingChanged = { draft = draft.copy(remaining = it) },
                            trackStock = draft.trackStock,
                            onTrackStockChanged = { draft = draft.copy(trackStock = it) },
                            showError = showValidation,
                        )

                        2 -> CourseStep(
                            startDate = draft.startDate,
                            onPickStartDate = { datePickerTarget = CourseDatePickerTarget.START },
                            endMode = draft.courseEndMode,
                            onEndModeChanged = { draft = draft.copy(courseEndMode = it) },
                            endDate = LocalDate.ofEpochDay(draft.endEpochDay),
                            onPickEndDate = { datePickerTarget = CourseDatePickerTarget.END },
                            courseDays = draft.courseDays,
                            onCourseDaysChanged = {
                                draft = draft.copy(courseDays = it.filter(Char::isDigit))
                            },
                            scheduleKind = draft.scheduleKind,
                            onScheduleKindChanged = { draft = draft.copy(scheduleKind = it) },
                            showError = showValidation,
                        )

                        3 -> TimeStep(
                            scheduleKind = draft.scheduleKind,
                            times = draft.times,
                            onAdd = {
                                draft = draft.copy(
                                    times = draft.times +
                                        EditableScheduleTime(8 * 60, ALL_DAYS_MASK),
                                )
                            },
                            onChangeTime = { index -> editingTimeIndex = index },
                            onToggleDay = { index, day ->
                                draft = draft.copy(
                                    times = draft.times.mapIndexed { currentIndex, current ->
                                        if (currentIndex == index) {
                                            current.copy(dayMask = current.dayMask xor dayMask(day))
                                        } else {
                                            current
                                        }
                                    },
                                )
                            },
                            onRemove = { index ->
                                if (draft.times.size > 1) {
                                    draft = draft.copy(
                                        times = draft.times.filterIndexed { currentIndex, _ ->
                                            currentIndex != index
                                        },
                                    )
                                }
                            },
                            showError = showValidation,
                        )

                        4 -> DetailsStep(
                            mealTiming = draft.mealTiming,
                            onMealTimingChanged = { draft = draft.copy(mealTiming = it) },
                            note = draft.note,
                            onNoteChanged = { draft = draft.copy(note = it) },
                            name = draft.name,
                            dosage = "${(draft.dosage ?: 0.0).displayAmount()} " +
                                draft.dosageUnit.title,
                            tabletsPerIntake = draft.tablets ?: 0.0,
                            scheduleKind = draft.scheduleKind,
                            times = draft.times,
                            startDate = draft.startDate,
                            endDate = draft.endDate,
                        )
                    }
                }
                if (!isImeVisible) {
                    Spacer(Modifier.height(AppSpacing.Xl))
                }
            }
        }
    }

    datePickerTarget?.let { target ->
        val selectedDate = when (target) {
            CourseDatePickerTarget.START -> draft.startDate
            CourseDatePickerTarget.END -> LocalDate.ofEpochDay(draft.endEpochDay)
        }
        AppDatePickerDialog(
            title = target.title,
            selectedDate = selectedDate,
            minDate = if (target == CourseDatePickerTarget.END) draft.startDate else null,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { selected ->
                draft = when (target) {
                    CourseDatePickerTarget.START -> draft.copy(
                        startEpochDay = selected.toEpochDay(),
                        endEpochDay = if (
                            LocalDate.ofEpochDay(draft.endEpochDay).isBefore(selected)
                        ) {
                            selected.toEpochDay()
                        } else {
                            draft.endEpochDay
                        },
                    )
                    CourseDatePickerTarget.END -> draft.copy(endEpochDay = selected.toEpochDay())
                }
                datePickerTarget = null
            },
        )
    }

    editingTimeIndex?.let { index ->
        draft.times.getOrNull(index)?.let { current ->
            AppTimePickerDialog(
                title = if (draft.times.size > 1) {
                    "Время приёма ${index + 1}"
                } else {
                    "Время приёма"
                },
                initialMinuteOfDay = current.minuteOfDay,
                onDismiss = { editingTimeIndex = null },
                onTimeSelected = { selectedMinute ->
                    draft = draft.copy(
                        times = draft.times.mapIndexed { currentIndex, time ->
                            if (currentIndex == index) {
                                time.copy(minuteOfDay = selectedMinute)
                            } else {
                                time
                            }
                        },
                    )
                    editingTimeIndex = null
                },
            )
        }
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmation = false },
            title = { Text("Отменить изменения?") },
            text = { Text("Внесённые изменения не будут сохранены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                        onBack()
                    },
                ) {
                    Text("Отменить изменения")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmation = false }) {
                    Text("Продолжить редактирование")
                }
            },
        )
    }
}
