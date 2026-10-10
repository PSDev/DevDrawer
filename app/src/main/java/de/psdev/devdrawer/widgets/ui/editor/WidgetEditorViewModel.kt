package de.psdev.devdrawer.widgets.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.analytics.TrackingService
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WidgetEditorViewModel.Factory::class)
class WidgetEditorViewModel @AssistedInject constructor(
    @Assisted private val widgetId: Int,
    private val widgetRepository: IWidgetRepository,
    widgetProfileRepository: IWidgetProfileRepository,
    packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService,
    private val sortOrderSettings: ISortOrderSettings,
    private val trackingService: TrackingService
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

    val state: StateFlow<WidgetEditorViewState> = combine(
        widgetRepository.widgetFlow(widgetId),
        widgetProfileRepository.widgetProfilesFlow(),
        packageFilterRepository.allFiltersFlow(),
        editableWidgetState,
        installedPackages
    ) { persistedWidget, widgetProfiles, filters, editableWidget, packages ->
        val filtersByProfile = filters.groupBy { it.profileId }
        val currentWidget = editableWidget ?: persistedWidget
        val defaultSortOrder = sortOrderSettings.defaultSortOrder()
        val matched = currentWidget?.let { packages.matching(filtersByProfile[it.profileId].orEmpty()) }.orEmpty()
        val sortOrder = currentWidget?.sortOrder ?: defaultSortOrder
        WidgetEditorViewState(
            persistedWidget = persistedWidget,
            editableWidget = currentWidget,
            profiles = widgetProfiles.map { profile ->
                ProfileWithAppCount(profile, packages.matching(filtersByProfile[profile.id].orEmpty()).size)
            },
            previewApps = appsService.appInfos(matched).sortedWith(sortOrder.comparator()).take(PREVIEW_APP_COUNT),
            previewAppCount = matched.size,
            defaultSortOrder = defaultSortOrder
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetEditorViewState.Empty)

    fun onNameChanged(newName: String) {
        editableWidgetState.update { it?.copy(name = newName) }
    }

    fun onHeaderColorSelected(headerColor: WidgetHeaderColor) {
        editableWidgetState.update { it?.copy(headerColor = headerColor) }
    }

    /** Null resets the widget to the sort order from Settings. */
    fun onSortOrderSelected(sortOrder: SortOrder?) {
        editableWidgetState.update { it?.copy(sortOrder = sortOrder) }
    }

    fun onWidgetProfileSelected(widgetProfile: WidgetProfile) {
        editableWidgetState.update { it?.copy(profileId = widgetProfile.id) }
    }

    fun saveChanges() {
        editableWidgetState.value?.let {
            viewModelScope.launch {
                widgetRepository.update(it)
            }
        }
    }

    fun deleteWidget(widget: Widget) {
        viewModelScope.launch {
            widgetRepository.delete(widget)
            trackingService.trackAction(Events.WIDGET_DELETED)
        }
    }

    private companion object {
        const val PREVIEW_APP_COUNT = 3
    }
}
