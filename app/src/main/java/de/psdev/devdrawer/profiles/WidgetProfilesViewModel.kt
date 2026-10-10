package de.psdev.devdrawer.profiles

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.R
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.widgets.IWidgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** A profile as the Profiles list shows it. */
@Immutable
data class ProfileSummary(
    val profile: WidgetProfile,
    val filterCount: Int,
    val appCount: Int,
    /** Names of the widgets that use this profile. */
    val usedBy: List<String>
)

sealed class DeleteResult {
    /** Deleted; holds what [WidgetProfilesViewModel.undoDelete] needs to bring it back. */
    data class Deleted(val profile: WidgetProfile, val filters: List<PackageFilter>) : DeleteResult()

    /** Not deleted, because these widgets still use it. */
    data class InUse(val profile: WidgetProfile, val widgets: List<Widget>) : DeleteResult()
}

@HiltViewModel
class WidgetProfilesViewModel @Inject constructor(
    private val application: Application,
    private val widgetProfileRepository: IWidgetProfileRepository,
    private val widgetRepository: IWidgetRepository,
    private val packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService,
    private val trackingService: TrackingService
) : ViewModel() {

    private val installedPackages = MutableStateFlow<List<PackageHashInfo>>(emptyList())

    init {
        viewModelScope.launch {
            installedPackages.value = appsService.installedPackages(includeSystemApps = true)
        }
    }

    val viewState = combine(
        widgetProfileRepository.widgetProfilesFlow(),
        packageFilterRepository.allFiltersFlow(),
        widgetRepository.widgetsFlow(),
        installedPackages
    ) { profiles, filters, widgets, packages ->
        val filtersByProfile = filters.groupBy { it.profileId }
        val widgetsByProfile = widgets.groupBy { it.profileId }
        UiState.Success(
            profiles.map { profile ->
                val profileFilters = filtersByProfile[profile.id].orEmpty()
                ProfileSummary(
                    profile = profile,
                    filterCount = profileFilters.size,
                    appCount = packages.matching(profileFilters).size,
                    usedBy = widgetsByProfile[profile.id].orEmpty().map { it.name }
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /** Deletes [widgetProfile] unless widgets use it; the result says which happened. */
    fun deleteProfile(widgetProfile: WidgetProfile, onResult: (DeleteResult) -> Unit) {
        viewModelScope.launch {
            val widgets = widgetRepository.findWidgetsForProfile(widgetProfile.id)
            if (widgets.isNotEmpty()) {
                onResult(DeleteResult.InUse(widgetProfile, widgets))
                return@launch
            }
            // Deleting the profile cascades to its filters; keep them so undo can restore both.
            val filters = packageFilterRepository.findAllByProfile(widgetProfile.id)
            widgetProfileRepository.delete(widgetProfile)
            trackingService.trackAction(Events.PROFILE_DELETED)
            onResult(DeleteResult.Deleted(widgetProfile, filters))
        }
    }

    fun undoDelete(deleted: DeleteResult.Deleted) {
        viewModelScope.launch {
            widgetProfileRepository.create(deleted.profile)
            deleted.filters.forEach { packageFilterRepository.save(it) }
        }
    }

    fun duplicateProfile(widgetProfile: WidgetProfile) {
        viewModelScope.launch {
            val copy = WidgetProfile(name = application.getString(R.string.profile_copy_name, widgetProfile.name))
            widgetProfileRepository.create(copy)
            packageFilterRepository.findAllByProfile(widgetProfile.id).forEach { filter ->
                packageFilterRepository.save(filter.copy(id = UUID.randomUUID().toString(), profileId = copy.id))
            }
            trackingService.trackAction(Events.PROFILE_CREATED)
        }
    }

    fun createNewProfile(onCreated: (WidgetProfile) -> Unit) {
        viewModelScope.launch {
            val size = widgetProfileRepository.findAll().size
            val widgetProfile = WidgetProfile(name = application.getString(R.string.new_profile_name, size + 1))
            widgetProfileRepository.create(widgetProfile)
            trackingService.trackAction(Events.PROFILE_CREATED)
            onCreated(widgetProfile)
        }
    }
}
