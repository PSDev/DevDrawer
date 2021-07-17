package de.psdev.devdrawer.widgets.ui.editor

import android.app.Activity
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.findNavController
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import javax.inject.Inject

@AndroidEntryPoint
class WidgetEditFragment: BaseFragment() {

    // Dependencies
    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    @Inject
    lateinit var viewModelViewModelFactory: EditWidgetFragmentViewModel.ViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            DevDrawerTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    WidgetEditor(
                        onEditWidgetProfile = { widgetProfile ->
                            findNavController().navigate(WidgetEditFragmentDirections.createProfileAction(widgetProfile.id))
                        },
                        onChangesSaved = {
                            val widgetId = requireActivity().intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
                            val resultValue = Intent().apply {
                                putExtra(EXTRA_APPWIDGET_ID, widgetId)
                            }
                            requireActivity().setResult(Activity.RESULT_OK, resultValue)
                            // Will either close the fragment or finish the activity when it's the last activity
                            if (!findNavController().popBackStack()) {
                                requireActivity().finish()
                            }
                            UpdateReceiver.send(requireContext())
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.edit_widget)
    }

}
