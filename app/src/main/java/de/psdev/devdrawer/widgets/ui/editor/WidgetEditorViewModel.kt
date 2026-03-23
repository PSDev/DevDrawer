package de.psdev.devdrawer.widgets.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
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
    private val widgetProfileRepository: IWidgetProfileRepository,
    private val trackingService: TrackingService
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(widgetId: Int): WidgetEditorViewModel
    }

    private val editableWidgetState: MutableStateFlow<Widget?> = MutableStateFlow(null)

    init {
        // Initialise the editable copy from the persisted widget on first load.
        // Done here rather than inside the combine transform to avoid side effects in a pure combiner.
        viewModelScope.launch {
            val initial = widgetRepository.widgetFlow(widgetId).filterNotNull().first()
            editableWidgetState.value = initial
        }
    }

    val state: StateFlow<WidgetEditorViewState> = combine(
        widgetRepository.widgetFlow(widgetId),
        widgetProfileRepository.widgetProfilesFlow(),
        editableWidgetState
    ) { persistedWidget, widgetProfiles, editableWidget ->
        WidgetEditorViewState(
            persistedWidget = persistedWidget,
            widgetProfiles = widgetProfiles,
            editableWidget = editableWidget ?: persistedWidget
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetEditorViewState.Empty)

    fun onNameChanged(newName: String) {
        editableWidgetState.update { it?.copy(name = newName) }
    }

    fun onWidgetColorChanged(color: Int) {
        editableWidgetState.update { it?.copy(color = color) }
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
}
