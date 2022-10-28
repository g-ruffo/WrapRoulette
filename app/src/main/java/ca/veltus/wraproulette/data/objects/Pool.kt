package ca.veltus.wraproulette.data.objects

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.PoolListItemBinding
import ca.veltus.wraproulette.ui.pools.PoolsFragmentDirections
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.xwray.groupie.databinding.BindableItem
import java.util.*

data class Pool(
    var docId: String,
    var adminUid: String,
    var adminName: String,
    var production: String,
    var password: String,
    var date: String,
    var betAmount: String?,
    var margin: String?,
    var lockTime: Date?,
    var startTime: Date?,
    var endTime: Date?,
    var winner: Member?,
    var users: MutableMap<String, Any>? = null
) {
    constructor() : this("", "", "", "", "", "", null, null, null, null, null, null, mutableMapOf())
}

class PoolItem(
    val pool: Pool,
    val user: User
) : BindableItem<PoolListItemBinding>() {

    override fun bind(viewBinding: PoolListItemBinding, position: Int) {
        viewBinding.pool = pool
        if (pool.docId == user.activePool) {
            viewBinding.cardView.setBackgroundColor(
                ContextCompat.getColor(
                    viewBinding.root.context,
                    R.color.selectedPoolCardView
                )
            )
        } else {
            viewBinding.cardView.setBackgroundColor(Color.WHITE)
        }
        user.activePool

        viewBinding.cardView.setOnClickListener {
            FirestoreUtil.setActivePool(pool.docId) {
                viewBinding.cardView.setBackgroundColor(
                    ContextCompat.getColor(
                        viewBinding.root.context,
                        R.color.selectedPoolCardView
                    )
                )
                Navigation.findNavController(viewBinding.root)
                    .navigate(PoolsFragmentDirections.actionNavPoolsToNavHome())
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.pool_list_item
    }

    companion object {
        private const val TAG = "Pool"
    }

}
