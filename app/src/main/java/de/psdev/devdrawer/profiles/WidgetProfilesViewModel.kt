package de.psdev.devdrawer.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.widgets.IWidgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetProfilesViewModel @Inject constructor(
    private val widgetProfileRepository: IWidgetProfileRepository,
    private val widgetRepository: IWidgetRepository,
    private val trackingService: TrackingService
) : ViewModel() {

    val viewState = widgetProfileRepository.widgetProfilesFlow()
        .map { UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun prepareProfileDeletion(widgetProfile: WidgetProfile, onResult: (DeleteDialogState) -> Unit) {
        viewModelScope.launch {
            val widgets = widgetRepository.findWidgetsForProfile(widgetProfile.id)
            onResult(
                if (widgets.isNotEmpty()) {
                    DeleteDialogState.InUseError(widgetProfile = widgetProfile, widgets = widgets)
                } else {
                    DeleteDialogState.Showing(widgetProfile)
                }
            )
        }
    }

    fun deleteProfile(widgetProfile: WidgetProfile) {
        viewModelScope.launch {
            widgetProfileRepository.delete(widgetProfile)
            trackingService.trackAction(Events.PROFILE_DELETED)
        }
    }

    fun createNewProfile(onCreated: (WidgetProfile) -> Unit) {
        viewModelScope.launch {
            val size = widgetProfileRepository.findAll().size
            val widgetProfile = WidgetProfile(name = "Profile ${size + 1}")
            widgetProfileRepository.create(widgetProfile)
            trackingService.trackAction(Events.PROFILE_CREATED)
            onCreated(widgetProfile)
        }
    }

}
