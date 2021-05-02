package de.psdev.devdrawer.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.commit
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.util.LibsListenerImpl
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ViewBindingBaseFragment
import de.psdev.devdrawer.databinding.FragmentAboutBinding
import de.psdev.devdrawer.utils.consume

@AndroidEntryPoint
class AboutFragment : ViewBindingBaseFragment<FragmentAboutBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAboutBinding =
        FragmentAboutBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(R.id.container_fragment) == null) {
            childFragmentManager.commit {
                val fragment = LibsBuilder()
                    .withFields(R.string::class.java.fields)
                    .withListener(object : LibsListenerImpl() {
                        override fun onExtraClicked(v: View, specialButton: Libs.SpecialButton): Boolean =
                            when (specialButton) {
                                Libs.SpecialButton.SPECIAL1 -> consume {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            "https://play.google.com/store/apps/details?id=de.psdev.devdrawer".toUri()
                                        setPackage("com.android.vending")
                                    }
                                    startActivity(intent)
                                }
                                Libs.SpecialButton.SPECIAL2 -> consume {
                                    val customTabsIntent = CustomTabsIntent.Builder().build()
                                    customTabsIntent.intent.data = "https://github.com/PSDev/DevDrawer".toUri()
                                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ContextCompat.startActivity(
                                        view.context.applicationContext,
                                        customTabsIntent.intent,
                                        customTabsIntent.startAnimationBundle
                                    )
                                }
                                else -> super.onExtraClicked(v, specialButton)
                            }
                    })
                    .supportFragment()

                add(R.id.container_fragment, fragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.app_info)
    }
}