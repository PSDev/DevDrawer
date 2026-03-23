# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About the Project

DevDrawer is an Android app (published on Google Play) that adds a home screen widget listing the developer's installed
apps for quick launching, uninstalling, and reinstalling. It supports multiple widgets with independent configurations,
app filtering by package name/signature/regex, and dark mode.

- **Package**: `de.psdev.devdrawer`
- **Min SDK**: 26 | **Target/Compile SDK**: 36
- **Language**: Kotlin | **JVM target**: 17
- **Debug build suffix**: `.debug` (so debug and release can coexist on device)

## Common Commands

```bash
# Build
./gradlew build

# Assemble debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "de.psdev.devdrawer.SomeTest"

# Lint
./gradlew lint

# Check dependency updates
./gradlew dependencyUpdates

# Print current version
./gradlew printVersion
```

There are no unit or instrumentation test source sets currently (no `src/test/` or `src/androidTest/` directories exist
yet).

## Architecture

The app follows **MVVM** with a Repository layer and uses **Jetpack Compose** for all UI.

### Navigation

Navigation uses **Jetpack Navigation 3** (`androidx.navigation3`). Routes are `@Serializable` data objects/classes
implementing `NavKey`, defined in `Routes.kt`. `DevDrawerHost.kt` maps each route to its screen composable via
`entryProvider { entry<RouteType> { ... } }` and renders them with `NavDisplay`. A `Navigator` (instantiated via
`remember { Navigator(navigationState) }` in `MainActivity`, passed down as a parameter) wraps the back stack mutations.
The root composable `DevDrawerApp` (`DevDrawerApp.kt`) owns the `Scaffold`, top bar, and bottom nav bar with three
top-level routes: `WidgetListRoute`, `WidgetProfilesRoute`, and `SettingsRoute`. `AboutRoute` and the detail routes
`WidgetEditorRoute(id)` / `WidgetProfileEditorRoute(id)` are also defined in `Routes.kt`.

### Data Layer

Room database (`DevDrawerDatabase`, version 3) with three entities and DAOs:

| Entity          | DAO                | Purpose                                                   |
|-----------------|--------------------|-----------------------------------------------------------|
| `Widget`        | `WidgetDao`        | Home screen widget instances                              |
| `WidgetProfile` | `WidgetProfileDao` | Named filter profiles                                     |
| `PackageFilter` | `PackageFilterDao` | Per-profile filter rules (package name, regex, signature) |

DB schema migrations live in `Migrations.kt`. Room schema JSON exports go to `/schemas/`.

### Dependency Injection

Hilt throughout. Key modules:

- `ApplicationModule` — provides `SharedPreferences`
- `DatabaseModule` — provides the Room DB and DAOs

### Widget System

- `DDWidgetProvider` — `AppWidgetProvider` that renders `RemoteViews`; **never rename this class** as it breaks existing
  placed widgets
- `WidgetService` + `WidgetAppsListViewFactory` — `RemoteViewsService` that populates the scrollable app list inside the
  widget
- `ClickHandlingActivity` — trampoline activity for widget item taps
- `UpdateWidgetsWorker` — `WorkManager` worker to refresh all widgets
- `AppInstallationReceiver` — `BroadcastReceiver` for `PACKAGE_ADDED`/`PACKAGE_REMOVED` events that triggers widget
  refresh

### Key Package Layout

```
de.psdev.devdrawer/
├── appwidget/          # Widget provider, service, click handler
├── database/           # Room entities, DAOs, migrations
├── profiles/           # WidgetProfile feature (UI + repository)
│   └── ui/
│       ├── editor/     # Profile editor screen & ViewModels
│       └── list/       # Profile list screen
├── receivers/          # Broadcast receivers
├── settings/           # Settings screen
├── ui/                 # Shared Compose UI (theme, dialogs, loading)
├── utils/              # Extension functions
└── widgets/            # Widget config feature (UI + repository)
    └── ui/
        ├── editor/     # Widget editor screen & ViewModel
        └── list/       # Widget list screen
```


## Build & Release

- Signing config is read from `release.properties` (local) or CI env vars (`keystore_password`, `keystore_alias`,
  `keystore_alias_password`) when `CI=true`
- `release.properties.sample` shows the expected format
- Google Play publishing via `com.github.triplet.play` plugin; requires `google-play-api.json` or
  `ANDROID_PUBLISHER_CREDENTIALS` env var
- Firebase services require `google-services.json` in `app/src/debug/` and `app/src/release/` (not committed; injected
  by CI secrets)
- Versioning is driven by `gradle/versioning.gradle` (git-based version codes)
