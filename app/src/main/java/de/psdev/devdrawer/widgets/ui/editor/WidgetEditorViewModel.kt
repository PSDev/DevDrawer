package de.psdev.devdrawer.widgets.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.WidgetProfileRepository
import de.psdev.devdrawer.widgets.WidgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val widgetRepository: WidgetRepository,
    private val widgetProfileRepository: WidgetProfileRepository
): ViewModel() {
    private val widgetId: Int = savedStateHandle["widgetId"]!!

    private val editableWidgetState: MutableStateFlow<Widget?> = MutableStateFlow(null)

    val state = combine(
        widgetRepository.widgetFlow(widgetId),
        widgetProfileRepository.widgetProfilesFlow(),
        editableWidgetState

    ) { persistedWidget, widgetProfiles, editableWidget ->
        if (editableWidgetState.value == null) {
            editableWidgetState.value = persistedWidget
        }
        WidgetEditorViewState(
            persistedWidget = persistedWidget,
            widgetProfiles = widgetProfiles,
            editableWidget = editableWidget
        )
    }

    fun onNameChanged(newName: String) {
        editableWidgetState.value = editableWidgetState.value?.copy(
            name = newName
        )
    }

    fun onWidgetColorChanged(color: Int) {
        editableWidgetState.value = editableWidgetState.value?.copy(
            color = color
        )
    }

    fun onWidgetProfileSelected(widgetProfile: WidgetProfile) {
        editableWidgetState.value = editableWidgetState.value?.copy(
            profileId = widgetProfile.id
        )
    }

    fun saveChanges() {
        editableWidgetState.value?.let {
            viewModelScope.launch {
                widgetRepository.update(it)
            }
        }
    }
}