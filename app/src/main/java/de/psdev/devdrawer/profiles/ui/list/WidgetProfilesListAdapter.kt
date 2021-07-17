package de.psdev.devdrawer.profiles.ui.list

import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.databinding.ListItemWidgetProfileBinding
import de.psdev.devdrawer.profiles.WidgetActionListener
import de.psdev.devdrawer.profiles.ui.list.WidgetProfilesListAdapter.WidgetProfileViewHolder
import de.psdev.devdrawer.utils.consume
import de.psdev.devdrawer.utils.layoutInflater

class WidgetProfilesListAdapter : ListAdapter<WidgetProfile, WidgetProfileViewHolder>(WidgetProfile.DIFF_CALLBACK) {

    var selectionTracker: SelectionTracker<String>? = null
    var itemLongClickListener: WidgetActionListener? = null

    // ==========================================================================================================================
    // ListAdapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetProfileViewHolder {
        val onClickListener: WidgetActionListener = { selectedItem: WidgetProfile ->
            selectionTracker?.select(selectedItem.id)
        }
        val onEditClickListener: WidgetActionListener = { selectedItem: WidgetProfile ->
            itemLongClickListener?.invoke(selectedItem)
        }
        return WidgetProfileViewHolder(
            binding = ListItemWidgetProfileBinding.inflate(parent.layoutInflater, parent, false),
            onClickListener = onClickListener,
            onEditClickListener = onEditClickListener
        )
    }

    override fun onBindViewHolder(holder: WidgetProfileViewHolder, position: Int) {
        val widgetProfile = getItem(position)
        val isSelected = selectionTracker?.isSelected(widgetProfile.id) ?: false
        holder.bindTo(widgetProfile, isSelected)
    }

    public override fun getItem(position: Int): WidgetProfile = super.getItem(position)

    class WidgetProfileViewHolder(
        private val binding: ListItemWidgetProfileBinding,
        private val onClickListener: WidgetActionListener,
        private val onEditClickListener: WidgetActionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        var currentItem: WidgetProfile? = null
            private set

        fun bindTo(item: WidgetProfile, isActivated: Boolean = false) {
            currentItem = item
            with(binding) {
                with(txtName) {
                    text = item.name
                }
                with(root) {
                    setOnClickListener {
                        onClickListener(item)
                    }
                    setOnLongClickListener {
                        consume {
                            onEditClickListener(item)
                        }
                    }
                }
                root.isActivated = isActivated
            }
        }
    }
}