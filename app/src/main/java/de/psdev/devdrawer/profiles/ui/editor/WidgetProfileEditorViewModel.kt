package de.psdev.devdrawer.profiles.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.comparator
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = WidgetProfileEditorViewModel.Factory::class)
class WidgetProfileEditorViewModel @AssistedInject constructor(
    @Assisted private val profileId: String,
    /** Just created from the Profiles tab; discarded again if left without changes. */
    @Assisted private val isNew: Boolean,
    private val database: DevDrawerDatabase,
    private val packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(profileId: String, isNew: Boolean): WidgetProfileEditorViewModel
    }

    /** The name as typed; saved once typing pauses or the editor closes. */
    private val widgetNameState: MutableStateFlow<String?> = MutableStateFlow(null)
    private var pendingNameSave: Job? = null

    private val dbFiltersFlow = database.packageFilterDao().findAllByProfileFlow(profileId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val installedPackages = MutableStateFlow<List<PackageHashInfo>>(emptyList())

    init {
        viewModelScope.launch {
            installedPackages.value = appsService.installedPackages(includeSystemApps = true)
        }
    }

    /** The filters with their matching apps and per-filter counts, computed together so they never disagree. */
    private val matches = combine(dbFiltersFlow, installedPackages) { filters, packages ->
        val apps = appsService.appInfos(packages.matching(filters)).sortedWith(SortOrder.NAME.comparator())
        FilterMatches(filters, apps, filters.associate { it.id to packages.matching(listOf(it)).size })
    }

    val state = combine(
        database.widgetProfileDao().widgetProfileWithIdObservable(profileId),
        widgetNameState,
        matches,
        database.widgetDao().findAllFlow().map { widgets ->
            widgets.filter { it.profileId == profileId }.map { it.name }.sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
    ) { widgetProfile, name, (filters, matchingApps, filterAppCounts), usedByWidgets ->
        WidgetProfileEditorViewState(
            widgetProfile = widgetProfile,
            widgetName = name ?: widgetProfile?.name.orEmpty(),
            packageFilters = filters,
            matchingApps = matchingApps,
            filterAppCounts = filterAppCounts,
            usedByWidgets = usedByWidgets,
            isMissing = widgetProfile == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetProfileEditorViewState.Empty)

    private val patternState = MutableStateFlow("")

    /** What the package name pattern being entered would match; recomputed as it is typed. */
    val patternPreview: StateFlow<PatternPreview> = combine(patternState, installedPackages) { pattern, packages ->
        val filter = PackageFilter(type = FilterType.PACKAGE_NAME, filter = pattern.trim(), profileId = profileId)
        val matched = if (filter.isValidPattern) packages.matching(listOf(filter)) else emptyList()
        PatternPreview(
            pattern = pattern,
            isValid = filter.isValidPattern,
            matchCount = matched.size,
            apps = appsService.appInfos(matched).sortedWith(SortOrder.NAME.comparator()).take(PATTERN_PREVIEW_APP_COUNT)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PatternPreview())

    fun onPatternChanged(pattern: String) {
        patternState.value = pattern
    }

    fun onNameChanged(name: String) {
        widgetNameState.value = name
        pendingNameSave?.cancel()
        pendingNameSave = viewModelScope.launch {
            delay(NAME_SAVE_DELAY_MS)
            saveName(name)
        }
    }

    /** Saves a name still waiting for typing to pause; called whenever the editor leaves the screen. */
    fun onEditorHidden() {
        val name = widgetNameState.value
        if (pendingNameSave?.isActive == true && name != null) {
            pendingNameSave?.cancel()
            saveName(name)
        }
    }

    /**
     * The editor was closed for good (its back stack entry is gone): discard a new profile that was left untouched
     * (no filters, no name typed), so backing out of "Create new profile" leaves nothing behind. Not done when the
     * editor is merely hidden, e.g. by switching tabs, since it comes back.
     */
    override fun onCleared() {
        if (!isNew || !widgetNameState.value.isNullOrBlank()) return
        // viewModelScope is already cancelled here; the write must still happen, after any pending ones.
        CoroutineScope(Dispatchers.IO).launch {
            writeMutex.withLock {
                val profile = database.widgetProfileDao().findById(profileId) ?: return@withLock
                if (database.packageFilterDao().findAllByProfile(profileId).isEmpty()) {
                    database.widgetProfileDao().delete(profile)
                }
            }
        }
    }

    fun addPackageFilter(packageFilter: PackageFilter) {
        write { packageFilterRepository.save(packageFilter) }
    }

    fun deleteFilter(packageFilter: PackageFilter) {
        write { packageFilterRepository.delete(packageFilter) }
    }

    /** Undoes [deleteFilter]. */
    fun restoreFilter(packageFilter: PackageFilter) = addPackageFilter(packageFilter)

    /** A blank name keeps the saved one. */
    private fun saveName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        write {
            val profile = database.widgetProfileDao().findById(profileId) ?: return@write
            if (profile.name != trimmed) database.widgetProfileDao().updateWithTimestamp(profile.copy(name = trimmed))
        }
    }

    private val writeMutex = Mutex()

    /**
     * Edits are written straight away, one at a time and in order (so closing the editor right after adding a
     * filter sees that filter), and finish even if the editor closes meanwhile.
     */
    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch { withContext(NonCancellable) { writeMutex.withLock { block() } } }
    }

    private data class FilterMatches(
        val filters: List<PackageFilter>,
        val apps: List<AppInfo>,
        val appCounts: Map<String, Int>
    )

    private companion object {
        const val NAME_SAVE_DELAY_MS = 500L
        const val PATTERN_PREVIEW_APP_COUNT = 5
    }
}
