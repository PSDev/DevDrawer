package de.psdev.devdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding

abstract class ViewBindingBaseFragment<T : ViewBinding> : BaseFragment() {

    private var _binding: T? = null

    // This property is only valid between onCreateView and onDestroyView.
    protected val binding get() = _binding!!

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createViewBinding(inflater, container, savedInstanceState).also { viewBinding ->
        _binding = viewBinding
    }.root

    protected abstract fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): T

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
