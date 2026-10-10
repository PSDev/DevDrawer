package de.psdev.devdrawer.widgets.ui.setup

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
import de.psdev.devdrawer.widgets.IWidgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Asks which apps a widget should show when it is placed (or from its empty state), then creates the profile and
 * filter that answer it and saves the widget.
 */
@HiltViewModel(assistedFactory = WidgetSetupViewModel.Factory::class)
class WidgetSetupViewModel @AssistedInject constructor(
    @Assisted private val widgetId: Int,
    private val application: Application,
    private val widgetRepository: IWidgetRepository,
    private val widgetProfileRepository: IWidgetProfileRepository,
    private val packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService,
    private val trackingService: TrackingService
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(widgetId: Int): WidgetSetupViewModel
    }

    private val mutableState = MutableStateFlow(WidgetSetupState())
    val state: StateFlow<WidgetSetupState> = mutableState.asStateFlow()

    private var installedPackages: List<PackageHashInfo> = emptyList()
    private var filtersByProfile: Map<String, List<PackageFilter>> = emptyMap()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        installedPackages = appsService.installedPackages(includeSystemApps = true)
        val keyCounts = installedPackages.groupingBy { it.signatureHashSha256 }.eachCount()
        val ownApps = appsService.installedPackages(includeSystemApps = false)
            .sortedByDescending { it.lastUpdateTime }
        val apps = appsService.appInfos(ownApps).map { app ->
            SetupApp(
                name = app.name,
                packageName = app.packageName,
                signatureHash = app.signatureHashSha256,
                otherAppsWithSameKey = (keyCounts[app.signatureHashSha256] ?: 1) - 1,
                icon = app.appIcon
            )
        }
        val profiles = widgetProfileRepository.findAll()
        filtersByProfile = profiles.associate { it.id to packageFilterRepository.findAllByProfile(it.id) }
        val currentProfileId = widgetRepository.findById(widgetId)?.profileId
        mutableState.update { state ->
            state.copy(
                isLoading = false,
                apps = apps,
                profiles = profiles.map { SetupProfile(it, installedPackages.matching(filtersByProfile[it.id].orEmpty()).size) },
                selectedProfileId = currentProfileId?.takeIf { id -> profiles.any { it.id == id } }
            ).withMatchCount()
        }
    }

    fun onSourceSelected(source: SetupSource) {
        mutableState.update { it.copy(source = source).withMatchCount() }
    }

    fun onAppSelected(packageName: String) {
        mutableState.update { it.copy(source = SetupSource.MY_APPS, selectedPackageName = packageName).withMatchCount() }
    }

    fun onPatternChanged(pattern: String) {
        mutableState.update { it.copy(source = SetupSource.PATTERN, pattern = pattern).withMatchCount() }
    }

    fun onProfileSelected(profileId: String) {
        mutableState.update { it.copy(source = SetupSource.PROFILE, selectedProfileId = profileId).withMatchCount() }
    }

    /** Saves the choice; [onDone] gets the saved widget. Does nothing until the choice is complete. */
    fun finish(onDone: (Widget) -> Unit) {
        val state = mutableState.value
        if (!state.canFinish) return
        viewModelScope.launch {
            val profile = when (state.source) {
                SetupSource.MY_APPS -> {
                    val app = state.apps.first { it.packageName == state.selectedPackageName }
                    createProfile(
                        name = application.getString(R.string.profile_name_signed_like, app.name),
                        filter = { profileId ->
                            PackageFilter(type = FilterType.SIGNATURE, filter = app.signatureHash, description = app.name, profileId = profileId)
                        }
                    )
                }
                SetupSource.PATTERN -> {
                    val pattern = state.pattern.trim()
                    createProfile(
                        name = pattern,
                        filter = { profileId -> PackageFilter(type = FilterType.PACKAGE_NAME, filter = pattern, profileId = profileId) }
                    )
                }
                SetupSource.PROFILE -> state.profiles.first { it.profile.id == state.selectedProfileId }.profile
            }
            val widget = widgetRepository.findById(widgetId)?.copy(name = profile.name, profileId = profile.id)
                ?: Widget(id = widgetId, name = profile.name, color = Color.BLACK, profileId = profile.id)
            widgetRepository.save(widget)
            onDone(widget)
        }
    }

    private suspend fun createProfile(name: String, filter: (String) -> PackageFilter): WidgetProfile {
        val profile = WidgetProfile(name = name)
        widgetProfileRepository.create(profile)
        packageFilterRepository.save(filter(profile.id))
        trackingService.trackAction(Events.PROFILE_CREATED)
        return profile
    }

    private fun WidgetSetupState.withMatchCount(): WidgetSetupState {
        val filters = when (source) {
            SetupSource.MY_APPS -> apps.firstOrNull { it.packageName == selectedPackageName }?.let {
                listOf(PackageFilter(type = FilterType.SIGNATURE, filter = it.signatureHash, profileId = ""))
            }
            SetupSource.PATTERN -> pattern.trim().takeIf { it.isNotEmpty() }?.let {
                listOf(PackageFilter(type = FilterType.PACKAGE_NAME, filter = it, profileId = ""))
            }
            SetupSource.PROFILE -> selectedProfileId?.let { filtersByProfile[it] }
        }
        // A pattern being typed can be an invalid regex for a moment ("com.("): count no matches until it is valid.
        val count = runCatching { installedPackages.matching(filters.orEmpty()).size }.getOrDefault(0)
        return copy(matchCount = count)
    }
}
