# Архитектура Pills Tracker

Проект использует простую слоистую структуру с группировкой UI по пользовательским
функциям. Зависимости направлены от интерфейса к data/domain, а Android-компоненты
изолированы в собственных пакетах.

```text
com.denisp.pillstracker
├── MainActivity.kt
├── PillsTrackerApplication.kt
├── data
│   ├── TrackerRepository.kt
│   └── local
│       ├── TrackerDatabase.kt
│       └── ThemePreferences.kt
├── domain
│   └── ScheduleCalculator.kt
├── model
│   ├── Intake.kt
│   ├── Medicine.kt
│   ├── Schedule.kt
│   └── ThemeMode.kt
├── notifications
│   ├── NotificationScheduler.kt
│   └── receiver
│       ├── AlarmReceiver.kt
│       ├── BootReceiver.kt
│       └── NotificationActionReceiver.kt
└── ui
    ├── PillsTrackerApp.kt
    ├── ColorExtensions.kt
    ├── DateTimeFormats.kt
    ├── MedicinePalette.kt
    ├── components
    │   └── MedicineAppearance.kt
    ├── feature
    │   ├── editor
    │   ├── history
    │   ├── medicines
    │   ├── settings
    │   └── today
    └── theme
        └── PillsTrackerTheme.kt
```

## Ответственность слоёв

- `model` — структуры данных и enum без Android-зависимостей.
- `domain` — чистая бизнес-логика расписаний.
- `data/local` — SQLite и локальные пользовательские настройки.
- `data` — единая точка доступа UI к данным.
- `notifications` — планирование системных напоминаний.
- `notifications/receiver` — входные точки Android BroadcastReceiver.
- `ui/feature` — экраны и компоненты конкретной пользовательской функции.
- `ui/components` — переиспользуемые компоненты нескольких экранов.
- `ui/theme` — тема и цветовая схема приложения.

## Правила добавления файлов

1. Новые экраны размещаются в отдельной папке внутри `ui/feature`.
2. Компонент остаётся внутри feature, пока не используется несколькими экранами.
3. Общая бизнес-логика не размещается в Composable-функциях.
4. Android API не используется в `model` и по возможности не используется в `domain`.
5. Изменения схемы SQLite сопровождаются миграцией и увеличением версии базы.
6. Новая функциональность и исправления фиксируются в `CHANGELOG.md`.
