package de.psdev.devdrawer.widgets.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.comparator
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
import de.psdev.devdrawer.profiles.ProfileWithAppCount
import de.psdev.devdrawer.settings.ISortOrderSettings
import de.psdev.devdrawer.widgets.IWidgetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = WidgetEditorViewModel.Factory::class)
class WidgetEditorViewModel @AssistedInject constructor(
    @Assisted private val widgetId: Int,
    private val widgetRepository: IWidgetRepository,
    widgetProfileRepository: IWidgetProfileRepository,
    packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService,
    private val sortOrderSettings: ISortOrderSettings,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(widgetId: Int): WidgetEditorViewModel
    }

    private val editableWidgetState: MutableStateFlow<Widget?> = MutableStateFlow(null)
    private val installedPackages: MutableStateFlow<List<PackageHashInfo>> = MutableStateFlow(emptyList())

    init {
        // Initialise the editable copy from the persisted widget on first load.
        // Done here rather than inside the combine transform to avoid side effects in a pure combiner.
        viewModelScope.launch {
            val initial = widgetRepository.widgetFlow(widgetId).filterNotNull().first()
            editableWidgetState.value = initial
        }
        viewModelScope.launch {
            installedPackages.value = appsService.installedPackages(includeSystemApps = true)
        }
    }

    private val currentWidget = combine(widgetRepository.widgetFlow(widgetId), editableWidgetState) { persisted, editable ->
        editable ?: persisted
    }

    /** The first apps and the app count; only recomputed when the profile, sort order, filters or apps change. */
    private val preview = combine(
        currentWidget.map { it?.profileId to it?.sortOrder }.distinctUntilChanged(),
        packageFilterRepository.allFiltersFlow(),
        installedPackages
    ) { (profileId, widgetSortOrder), filters, packages ->
        val matched = packages.matching(filters.filter { it.profileId == profileId })
        val sortOrder = widgetSortOrder ?: sortOrderSettings.defaultSortOrder()
        appsService.appInfos(matched).sortedWith(sortOrder.comparator()).take(PREVIEW_APP_COUNT) to matched.size
    }

    val state: StateFlow<WidgetEditorViewState> = combine(
        widgetRepository.widgetFlow(widgetId),
        currentWidget,
        widgetProfileRepository.widgetProfilesFlow(),
        combine(packageFilterRepository.allFiltersFlow(), installedPackages) { filters, packages ->
            filters.groupBy { it.profileId }.mapValues { (_, profileFilters) -> packages.matching(profileFilters).size }
        },
        preview
    ) { persistedWidget, widget, widgetProfiles, appCounts, (previewApps, previewAppCount) ->
        WidgetEditorViewState(
            persistedWidget = persistedWidget,
            editableWidget = widget,
            profiles = widgetProfiles.map { ProfileWithAppCount(it, appCounts[it.id] ?: 0) },
            previewApps = previewApps,
            previewAppCount = previewAppCount,
            defaultSortOrder = sortOrderSettings.defaultSortOrder()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetEditorViewState.Empty)

    /** Typing updates the field at once; the name is saved when typing pauses or the editor closes. */
    fun onNameChanged(newName: String) {
        editableWidgetState.update { it?.copy(name = newName) }
        pendingNameSave?.cancel()
        pendingNameSave = viewModelScope.launch {
            delay(NAME_SAVE_DELAY_MS)
            persist()
        }
    }

    fun onHeaderColorSelected(headerColor: WidgetHeaderColor) {
        editableWidgetState.update { it?.copy(headerColor = headerColor) }
        persist()
    }

    /** Null resets the widget to the sort order from Settings. */
    fun onSortOrderSelected(sortOrder: SortOrder?) {
        editableWidgetState.update { it?.copy(sortOrder = sortOrder) }
        persist()
    }

    fun onWidgetProfileSelected(widgetProfile: WidgetProfile) {
        editableWidgetState.update { it?.copy(profileId = widgetProfile.id) }
        persist()
    }

    /** Saves a name that is still waiting for typing to pause; called when the editor closes. */
    fun flushPendingChanges() {
        if (pendingNameSave?.isActive == true) {
            pendingNameSave?.cancel()
            persist()
        }
    }

    private var pendingNameSave: Job? = null

    private val saveMutex = Mutex()

    /**
     * Writes the latest edit, keeping the saved name while the field is blank. Writes run one at a time so a
     * quick second change can't be overtaken by the first, and they finish even if the editor closes.
     */
    private fun persist() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                saveMutex.withLock {
                    val edited = editableWidgetState.value ?: return@withLock
                    val saved = widgetRepository.findById(widgetId) ?: return@withLock
                    val widget = edited.copy(name = edited.name.trim().ifEmpty { saved.name })
                    if (widget != saved) widgetRepository.update(widget)
                }
            }
        }
    }

    private companion object {
        const val PREVIEW_APP_COUNT = 3
        const val NAME_SAVE_DELAY_MS = 500L
    }
}
