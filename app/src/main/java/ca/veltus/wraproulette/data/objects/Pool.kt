package ca.veltus.wraproulette.data.objects

import android.view.View
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.PoolListItemBinding
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
    var winners: List<Member> = listOf(),
    var users: MutableMap<String, Any> = mutableMapOf(),
    var pIRRulesEnabled: Boolean = false
) {
    constructor() : this(
        "", "", "", "", "", "", null, null, null, null, null, listOf(), mutableMapOf(), false
    )
}

class PoolItem(
    val pool: Pool, val user: User
) : BindableItem<PoolListItemBinding>() {

    override fun bind(viewBinding: PoolListItemBinding, position: Int) {
        viewBinding.pool = pool
        if (pool.docId == user.activePool) {
            viewBinding.activePoolIcon.visibility = View.VISIBLE
        } else {
            viewBinding.activePoolIcon.visibility = View.GONE
        }
    }

    override fun getLayout(): Int {
        return R.layout.pool_list_item
    }

    companion object {
        private const val TAG = "Pool"
    }

}
