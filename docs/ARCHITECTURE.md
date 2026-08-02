# Архитектура Pills Tracker

Проект использует слоистую структуру с группировкой интерфейса по пользовательским
функциям. Зависимости направлены от UI к `domain` и `data`; модели не зависят от
Android, а платформенные точки входа и уведомления изолированы.

```text
com.denisp.pillstracker
├── MainActivity.kt
├── PillsTrackerApplication.kt
├── application
│   ├── IntakeNotificationGateway.kt
│   └── IntakeStatusActions.kt
├── model
│   ├── Intake.kt
│   ├── InterfaceMode.kt
│   ├── Medicine.kt
│   ├── Schedule.kt
│   ├── ThemeMode.kt
│   └── UserProfile.kt
├── domain
│   ├── DoseTimingPolicy.kt
│   ├── IntakeRules.kt
│   ├── ScheduleCalculator.kt
│   └── StockRules.kt
├── data
│   ├── TrackerRepository.kt
│   └── local
│       ├── ProfilePreferences.kt
│       ├── ThemePreferences.kt
│       └── TrackerDatabase.kt
├── notifications
│   ├── AlarmSchedulingPolicy.kt
│   ├── ExactAlarmAccess.kt
│   ├── NotificationScheduler.kt
│   ├── ReminderPolicy.kt
│   ├── ReminderStateStore.kt
│   └── receiver
│       ├── AlarmReceiver.kt
│       ├── BootReceiver.kt
│       └── NotificationActionReceiver.kt
└── ui
    ├── MainNavigation.kt
    ├── PillsTrackerApp.kt
    ├── DateTimeFormats.kt
    ├── MedicinePalette.kt
    ├── components
    │   ├── AgePickerField.kt
    │   ├── AppDateTimePickerDialogs.kt
    │   ├── DoseTimingUi.kt
    │   ├── ExactAlarmPermissionUi.kt
    │   ├── GroupedIntakeCard.kt
    │   ├── IntakeStatusControls.kt
    │   ├── InterfaceModeToggle.kt
    │   ├── MedicineAppearance.kt
    │   ├── MedicineReminderOverlay.kt
    │   ├── ProfileDatePickerDialog.kt
    │   ├── ScheduledTimeBadge.kt
    │   └── SwipeableIntakeCard.kt
    ├── theme
    │   ├── AppDesignTokens.kt
    │   ├── AppDesignSystem.kt
    │   └── PillsTrackerTheme.kt
    └── feature
        ├── editor
        │   ├── MedicineEditorScreen.kt
        │   ├── MedicineEditorDraft.kt
        │   ├── MedicineEditorModels.kt
        │   ├── MedicineEditorRules.kt
        │   ├── MedicineFormCarousel.kt
        │   ├── MedicineFormCarouselState.kt
        │   ├── MedicineColorPicker.kt
        │   ├── MedicineEditOverview.kt
        │   ├── EditorMedicineStep.kt
        │   ├── EditorDosageStep.kt
        │   ├── EditorScheduleSteps.kt
        │   ├── EditorReviewStep.kt
        │   ├── EditorFields.kt
        │   └── EditorNavigation.kt
        ├── history
        ├── medicines
        │   ├── MedicinesScreen.kt
        │   ├── MedicineCatalogCard.kt
        │   ├── MedicineDetailsScreen.kt
        │   ├── MedicineActionsSheet.kt
        │   ├── DeleteMedicineDialog.kt
        │   └── RefillMedicineDialog.kt
        ├── onboarding
        ├── settings
        └── today
```

## Направление зависимостей

```text
MainActivity / Application
          ↓
PillsTrackerApp
          ↓
Feature Screen / Android Receiver
          ↓
Application Actions
       ↙       ↘ uses
Repository   IntakeNotificationGateway
    ↓                    ↑ implements
TrackerDatabase   NotificationScheduler
    ↓                    ↓
Model + Domain   Android Alarm/Notification API

NotificationScheduler → TrackerRepository + Android Alarm/Notification API
```

- `model` содержит структуры данных и enum без Android-зависимостей.
- `domain` содержит чистые правила приёма и расчёт расписаний.
- `application` координирует пользовательские действия между репозиторием и
  портом уведомлений, не завися от UI или конкретной Android-реализации.
- `data/local` отвечает за SQLite и небольшие локальные настройки.
- `data` предоставляет единую точку доступа к данным и публикует снимок состояния.
- `notifications` планирует системные напоминания и преобразует события Android в
  вызовы application-сценариев; `NotificationScheduler` реализует порт уведомлений.
- `ui/feature` содержит экраны и компоненты одной пользовательской функции.
- `ui/components` содержит элементы, которые используются несколькими функциями.
- `ui/theme` содержит тему, дизайн-токены и переиспользуемые элементы дизайн-системы.

## Устройство feature

Экран верхнего уровня остаётся координатором: получает зависимости, собирает состояние
и связывает пользовательские действия. Крупные визуальные блоки и расчёты находятся в
отдельных файлах той же feature.

Примеры:

- `today/TodayScreen.kt` — координатор главного экрана;
- `today/TodayUiState.kt` — чистое построение состояния;
- `today/TodayHeader.kt`, `TodayDashboard.kt`, `TodayMedicineCards.kt` — визуальные блоки;
- `medicines/MedicinesScreen.kt` — состояние каталога и маршрутизация действий;
- `medicines/MedicineCatalogCard.kt`, `MedicineActionsSheet.kt`, `DeleteMedicineDialog.kt`,
  `RefillMedicineDialog.kt` — карточка и отдельные модальные UI, построенные на общей
  дизайн-системе;
- `editor/MedicineEditorModels.kt` — временные модели состояния редактора;
- `editor/MedicineEditorRules.kt` — ограничения заметки и переходы цвета без Compose;
- `editor/MedicineFormCarouselState.kt` — чистая циклическая адресация страниц карусели;
- `editor/EditorMedicineStep.kt`, `EditorDosageStep.kt`, `EditorScheduleSteps.kt` —
  визуальные шаги, разделённые по назначению;
- `editor/MedicineFormCarousel.kt` и `MedicineColorPicker.kt` содержат карусель формы
  и палитру отдельно от разметки шага редактора;
- `editor/EditorFields.kt` — переиспользуемые поля внутри редактора;
- `components/AppDateTimePickerDialogs.kt` — общий ручной и визуальный выбор даты и времени;
- `components/InterfaceModeToggle.kt` — общий переключатель обычного и упрощённого интерфейса;
- `settings/ThemeModeSelector.kt` — визуальный выбор темы без логики хранения настроек;
- `history/HistoryStatistics.kt` — координатор статистики;
- `history/HistoryStatisticsModel.kt` — расчёты показателей;
- `history/HistorySummaryCards.kt`, `HistoryTrendCard.kt`, `HistoryCalendar.kt` — визуальные
  блоки статистики.

## Владение состоянием

- Долгоживущие данные лекарств и приёмов принадлежат `TrackerRepository`.
- Пользовательские настройки принадлежат соответствующим `Preferences`.
- Полноэкранные переходы хранятся единым стеком в корневом координаторе, поэтому
  системный жест и кнопка «Назад» возвращают на непосредственный предыдущий экран.
- Временное состояние фильтра или диалога принадлежит ближайшему экрану.
- Запрос открыть конкретное лекарство передаётся через корневой координатор по
  идентификатору и создаёт отдельный экран сведений поверх текущего источника.
- Расчёты, не требующие Android API или Compose, оформляются обычными функциями и
  покрываются локальными unit-тестами.
- Composable-компоненты получают готовые значения и колбэки; они не обращаются к базе
  напрямую.

## Зависимости сборки

- Проект остаётся одним модулем `app`: для текущего размера отдельные Gradle-модули
  добавили бы больше конфигурации, чем изоляции.
- Версии Compose согласуются через BOM; прямыми объявлены только библиотеки, API которых
  используются исходным кодом или тестами.
- Debug tooling и ViewModel Compose не объявляются напрямую, пока в проекте нет Preview
  и `ViewModel`.
- Инструментальные Compose-тесты находятся в `src/androidTest`, чистая логика —
  в `src/test`.
- Релизным артефактом является подписанный APK. Ключ подписи, локальные свойства,
  сертификаты, IDE-файлы и результаты сборки не хранятся в Git.

## Правила развития проекта

1. Новый пользовательский сценарий размещается в отдельной папке `ui/feature`.
2. Экран координирует сценарий, но не хранит реализацию всех его карточек и диалогов.
3. Компонент остаётся внутри feature, пока не используется несколькими экранами.
4. Общая бизнес-логика не размещается в Composable-функциях.
5. Android API не используется в `model` и по возможности не используется в `domain`.
6. Доступ к SQLite выполняется через `TrackerRepository`, а не из UI.
7. Изменение схемы SQLite сопровождается миграцией и увеличением версии базы.
8. Новая чистая логика сопровождается unit-тестом.
9. Новая функциональность и исправления фиксируются в `CHANGELOG.md`.
10. Перед релизом обязательны сверка этой архитектуры с кодом и удаление подтверждённого
    мёртвого кода, ресурсов и зависимостей по [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md).
