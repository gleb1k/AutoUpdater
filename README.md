# AutoUpdater

Библиотека для автоматического обновления Android-приложений без зависимости от сторонних сервисов (Google Play, RuStore). Позволяет проверять наличие обновлений, скачивать APK и запускать установку с минимальным вмешательством пользователя.

---

## Основные возможности

- Проверка обновлений по расписанию (периодические и одноразовые задачи).
- Скачивание APK с поддержкой кастомных загрузчиков.
- Установка обновлений через системный установщик.
- Настраиваемая система уведомлений для пользователя.
- Debug-экран с информацией об обновлениях и настройками (например, скачивание только по Wi-Fi).
- Гибкая архитектура с возможностью замены компонентов и расширения.

---

## Инициализация

### С дефолтными настройками

```kotlin 
AutoUpdater.init(applicationContext)
```

### С кастомной конфигурацией

```kotlin
val config = AutoUpdaterConfiguration(
    notifier = CustomNotifier(...),
    updateCheckerWorkerRunner = CustomWorkerRunner(...),
    installerWorkerRunner = CustomInstallerRunner(...),
    apkDownloader = CustomApkDownloader(...),
    appVersionHelper = CustomAppVersionHelper(...),
    workManagerConfigurator = CustomWorkManagerConfigurator(...)
)

AutoUpdater.init(applicationContext, config)
```

Также можно использоовать объекты отдельно

## Использование

### Запуск процесса обновления

```kotlin
val updateConfig = UpdateConfig(
    isPeriodic = true,
    repeatInterval = 12,
    timeUnit = TimeUnit.HOURS,
    checkerParameters = ...
)

AutoUpdater.startInstallProcess(updateConfig)
```

## Debug-экран

<p align="center">
  <img src="https://github.com/user-attachments/assets/fcb70439-6c3a-4700-a863-63d59dca37ac" width="30%" />
</p>

Экран реализован на **Jetpack Compose** с использованием **MVI архитектуры**. Позволяет:

- Просматривать текущую версию приложения.
- Видеть дату последней проверки обновлений и загрузки.
- Включать/отключать скачивание только по Wi-Fi.
- Запускать проверку обновлений вручную.
- Запускать установку обновления, если оно найдено.

---

## Расширяемость

Библиотека построена модульно: ключевые компоненты реализованы через интерфейсы и могут быть заменены на кастомные реализации через конфигурацию.


