package de.psdev.devdrawer.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.config.RemoteConfigService
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: BaseFragment() {

    @Inject
    lateinit var remoteConfigService: RemoteConfigService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            DevDrawerTheme {
                SettingsScreen()
            }
        }
    }

}