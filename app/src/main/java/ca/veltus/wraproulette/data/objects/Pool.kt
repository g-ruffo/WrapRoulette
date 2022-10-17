package ca.veltus.wraproulette.data.objects

import android.os.Parcelable
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.PoolListItemBinding
import com.google.firebase.Timestamp
import com.xwray.groupie.databinding.BindableItem
import kotlinx.parcelize.Parcelize

data class Pool(
    var docId: String,
    var adminUid: String,
    var production: String,
    var password: String,
    var date: Timestamp,
    var betAmount: String?,
    var margin: Timestamp?,
    var lockTime: Timestamp?,
    var startTime: Timestamp,
    var endTime: Timestamp?,
    val users: MutableMap<String, Any>? = null
) {
    constructor(): this("", "", "", "", Timestamp.now(), null, null, null, Timestamp.now(), null, mutableMapOf())

}

class PoolItem(val pool: Pool
) : BindableItem<PoolListItemBinding>() {

    override fun bind(viewBinding: PoolListItemBinding, position: Int) {
        viewBinding.pool = pool
    }

    override fun getLayout(): Int {
        return R.layout.pool_list_item
    }

    companion object {
        private const val TAG = "Pool"
    }

}
