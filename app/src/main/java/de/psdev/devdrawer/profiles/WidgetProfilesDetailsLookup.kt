package de.psdev.devdrawer.profiles

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import de.psdev.devdrawer.profiles.ui.list.WidgetProfilesListAdapter

class WidgetProfilesDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is WidgetProfilesListAdapter.WidgetProfileViewHolder) {
                val currentItem = viewHolder.currentItem
                if (currentItem != null) {
                    return object : ItemDetails<String>() {
                        override fun getSelectionKey(): String? = currentItem.id
                        override fun getPosition(): Int = viewHolder.absoluteAdapterPosition
                    }
                }
            }
        }
        return null
    }
}