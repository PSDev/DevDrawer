package de.psdev.devdrawer.profiles

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.appwidget.isSystemApp
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.appwidget.toPackageHashInfo
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.databinding.AppSignatureChooserBottomSheetDialogFragmentBinding
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class AppSignatureChooserBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    private var _binding: AppSignatureChooserBottomSheetDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AppSignatureChooserBottomSheetDialogFragmentArgs by navArgs()

    private val onAppClickListener: AppInfoActionListener = { appInfo ->
        lifecycleScope.launch {
            val packageFilter = PackageFilter(
                filter = appInfo.signatureHashSha256,
                type = FilterType.SIGNATURE,
                description = appInfo.name,
                profileId = navArgs.widgetProfileId
            )
            devDrawerDatabase.packageFilterDao().insert(packageFilter)
            UpdateReceiver.send(requireContext())
            dismiss()
        }
    }
    private val appAdapter = AppListAdapter(onAppClickListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AppSignatureChooserBottomSheetDialogFragmentBinding.inflate(inflater).also {
        _binding = it
    }.root

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnClose.setOnClickListener { dismiss() }
            recyclerApps.adapter = appAdapter
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    val filters = devDrawerDatabase.packageFilterDao().findAllByProfile(navArgs.widgetProfileId)
                    val context = requireContext()
                    val packageManager = context.packageManager
                    val installedPackages = Firebase.performance.trace("widget_profile_packages") {
                        packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)
                            .asSequence()
                            .filterNot { it.isSystemApp } // TODO Option to allow system apps?
                            .map { it.toPackageHashInfo() }
                            .distinctBy { it.signatureHashSha256 }
                            .filter { hashInfo -> filters.none { it.type == FilterType.SIGNATURE && it.filter == hashInfo.signatureHashSha256 } }
                            .mapNotNull { it.toAppInfo(context) }
                            .sortedBy { it.name }
                            .toList()
                    }
                    logger.warn { "Installed packages: $installedPackages" }
                    withContext(Dispatchers.Main) {
                        appAdapter.awaitSubmit(installedPackages)
                        progress.hide()
                        recyclerApps.isVisible = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerApps.adapter = null
        super.onDestroyView()
    }
}