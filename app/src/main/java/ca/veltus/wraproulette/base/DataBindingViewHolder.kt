package ca.veltus.wraproulette.base

import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView

// View Holder for the Recycler View to bind the data item to the UI
class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: T) {
        // TODO: Change _all to item
        binding.setVariable(BR._all, item)
        binding.executePendingBindings()
    }
}