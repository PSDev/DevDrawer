package de.psdev.devdrawer.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.WidgetProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetProfilesViewModel @Inject constructor(
    private val widgetProfileRepository: WidgetProfileRepository,
    val devDrawerDatabase: DevDrawerDatabase
): ViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Loading)

    init {
        viewModelScope.launch {
            widgetProfileRepository.widgetProfilesFlow().collect {
                delay(100)
                viewState.value = ViewState.Loaded(it)
            }
        }
    }

    suspend fun deleteProfile(widgetProfile: WidgetProfile) {
        widgetProfileRepository.delete(widgetProfile)
    }

    suspend fun createNewProfile(): WidgetProfile {
        val size = widgetProfileRepository.findAll().size
        val widgetProfile = WidgetProfile(name = "Profile ${size + 1}")
        widgetProfileRepository.create(widgetProfile)
        return widgetProfile
    }

    sealed class ViewState {
        object Loading: ViewState()
        data class Loaded(
            val data: List<WidgetProfile>
        ): ViewState()
    }

}