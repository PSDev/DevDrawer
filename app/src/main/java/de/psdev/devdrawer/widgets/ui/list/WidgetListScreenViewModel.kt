package de.psdev.devdrawer.widgets.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.psdev.devdrawer.database.DevDrawerDatabase
import javax.inject.Inject

@HiltViewModel
class WidgetListScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val database: DevDrawerDatabase
): ViewModel() {

    val widgets = database.widgetDao().findAllFlow()

}