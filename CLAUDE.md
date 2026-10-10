# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About the Project

DevDrawer is an Android app (published on Google Play) that adds a home screen widget listing the developer's installed
apps for quick launching, uninstalling, and reinstalling. It supports multiple widgets with independent configurations,
app filtering by package name/signature/regex, and dark mode.

- **Package**: `de.psdev.devdrawer`
- **Min SDK**: 26 | **Target/Compile SDK**: 37
- **Language**: Kotlin | **JVM target**: 21 (JDK 21 toolchain; CI uses Temurin 21)
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

# Print current version
./gradlew printVersion
```

Unit tests live in `app/src/test/` (JUnit 4, Robolectric, MockK, kotlinx-coroutines-test; repository fakes in
`fakes/`). Compose UI (`createComposeRule`), Glance (`runGlanceAppWidgetUnitTest`) and Room migration
(`MigrationTestHelper`) tests also run there under Robolectric; the schema exports are added to debug assets for them.
There is no `src/androidTest/` source set and no instrumentation-test dependencies.

## Architecture

The app follows **MVVM** with a Repository layer and uses **Jetpack Compose** for all UI.

### Navigation

Navigation uses **Jetpack Navigation 3** (`androidx.navigation3`). Routes are `@Serializable` data objects/classes
implementing `NavKey`, defined in `Routes.kt`. `DevDrawerHost.kt` maps each route to its screen composable via
`entryProvider { entry<RouteType> { ... } }` and renders them with `NavDisplay`. A `Navigator` (instantiated via
`remember { Navigator(navigationState) }` in `MainActivity`, passed down as a parameter) wraps the back stack mutations.
The root composable `DevDrawerApp` (`DevDrawerApp.kt`) owns the `Scaffold`, top bar, and bottom nav bar with three
top-level routes: `WidgetListRoute`, `WidgetProfilesRoute`, and `SettingsRoute`. `AboutRoute` and the detail routes
`WidgetEditorRoute(id)` / `WidgetSetupRoute(widgetId)` / `WidgetProfileEditorRoute(id)` are also defined in `Routes.kt`.
`widgetLaunchFor()` (`WidgetLaunch.kt`) decides which route a widget intent opens; `MainActivity` sets the
configure result (`RESULT_OK` when setup finishes) when the launcher placed or reconfigures a widget.

### Data Layer

Room database (`DevDrawerDatabase`, version 4) with three entities and DAOs:

| Entity          | DAO                | Purpose                                                   |
|-----------------|--------------------|-----------------------------------------------------------|
| `Widget`        | `WidgetDao`        | Home screen widgets: name, profile, header colour, sort   |
| `WidgetProfile` | `WidgetProfileDao` | Named filter profiles                                     |
| `PackageFilter` | `PackageFilterDao` | Per-profile filter rules (package name, regex, signature) |

DB schema migrations live in `Migrations.kt`. Room schema JSON exports go to `/schemas/`. `Widget.color` is the legacy
ARGB header colour, kept so the table needs no rebuild; `headerColor` (`WidgetHeaderColor`) replaces it.

Which apps a profile shows is decided in one place: `matching()` and `SortOrder.comparator()` in `apps/AppMatching.kt`,
used by the widget, the Widgets list, both editors and the setup screen. `IAppsService` reads installed packages.

### Dependency Injection

Hilt throughout. Key modules:

- `ApplicationModule` — provides `SharedPreferences`
- `DatabaseModule` — provides the Room DB and DAOs
- `RepositoryModule` — binds the repository interfaces (`IWidgetRepository`, `IWidgetProfileRepository`,
  `IPackageFilterRepository`), `IAppsService` and `ISortOrderSettings` to their implementations
- `WidgetModule` — provides `WidgetContentLoader`; `WidgetEntryPoint` gives the Glance widget access to it

### Widget System

The home-screen widget is built with **Jetpack Glance**.

- `DDWidgetProvider` — `GlanceAppWidgetReceiver` for `DevDrawerGlanceWidget`; **never rename this class** as it breaks
  existing placed widgets
- `appwidget/glance/` — `DevDrawerGlanceWidget` loads the widget's content, `DevDrawerWidgetContent` draws it (header
  palette from `ui/theme/WidgetHeaderPalette.kt`, empty state with "Choose apps"), `RefreshAction` reloads it
- `WidgetContentLoader` — a widget's matching apps in its sort order (its own, else the Settings default); app icons are
  scaled to the 40 dp they are drawn at, because every icon travels in the widget's RemoteViews
- `ClickHandlingActivity` — trampoline activity for widget item taps (launch, uninstall, app details)
- `UpdateWidgetsWorker` — `WorkManager` worker to refresh all widgets
- `AppInstallationReceiver` — `BroadcastReceiver` for `PACKAGE_ADDED`/`PACKAGE_REMOVED` events that triggers widget
  refresh

### Key Package Layout

```
de.psdev.devdrawer/
├── apps/              # Installed apps and profile matching
├── appwidget/          # Glance widget, receiver, content loader, click handler
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
        ├── list/       # Widget list screen
        └── setup/      # Setup when a widget is placed: which apps it shows
```


## Build & Release

- Dependency versions are managed in the version catalog `gradle/libs.versions.toml` (no dependency-updates plugin;
  Renovate is configured via `renovate.json`)
- `com.google.android.material:material` is required by the XML themes (`themes.xml`), even though all screens are
  Compose

- Signing config is read from `release.properties` (local) or CI env vars (`keystore_password`, `keystore_alias`,
  `keystore_alias_password`) when `CI=true`
- `release.properties.sample` shows the expected format
- Google Play publishing via `com.github.triplet.play` plugin; requires `google-play-api.json` or
  `ANDROID_PUBLISHER_CREDENTIALS` env var
- Firebase services require `google-services.json` in `app/src/debug/` and `app/src/release/` (not committed; injected
  by CI secrets)
- Versioning is driven by `gradle/versioning.gradle` (git-based version codes)
