package ca.veltus.wraproulette.data.objects

import android.view.View
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.PoolListItemBinding
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.google.firebase.Timestamp
import com.xwray.groupie.databinding.BindableItem

data class Pool(
    var docId: String,
    var adminUid: String,
    var production: String,
    var password: String,
    var date: String,
    var betAmount: String?,
    var margin: Timestamp?,
    var lockTime: Timestamp?,
    var startTime: Timestamp,
    var endTime: Timestamp?,
    val users: MutableMap<String, Any>? = null
) {
    constructor(): this("", "", "", "", "", null, null, null, Timestamp.now(), null, mutableMapOf())

}

class PoolItem(val pool: Pool
) : BindableItem<PoolListItemBinding>() {

    override fun bind(viewBinding: PoolListItemBinding, position: Int) {
        viewBinding.pool = pool
        FirestoreUtil.getCurrentUser { user ->
            if (pool.docId == user.activePool) {
                viewBinding.selectedPoolIcon.visibility = View.VISIBLE

            } else {
                viewBinding.selectedPoolIcon.visibility = View.GONE
            }
            user.activePool
        }
        viewBinding.cardView.setOnClickListener {
            FirestoreUtil.setActivePool(pool.docId) {
                viewBinding.selectedPoolIcon.visibility = View.VISIBLE
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
