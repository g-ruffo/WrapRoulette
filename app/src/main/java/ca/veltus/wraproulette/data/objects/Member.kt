package ca.veltus.wraproulette.data.objects

import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MemberListItemBinding
import com.xwray.groupie.databinding.BindableItem
import java.util.*

data class Member(
    val uid: String,
    val poolId: String,
    val displayName: String,
    val email: String,
    val department: String?,
    val bidTime: Date?,
    val profilePicturePath: String?
) {
    constructor() : this("", "", "", "", null, null, null)
}

class MemberItem(val member: Member
) : BindableItem<MemberListItemBinding>() {

    override fun bind(viewBinding: MemberListItemBinding, position: Int) {
        viewBinding.member = member
    }

    override fun getLayout(): Int {
        return R.layout.member_list_item
    }

    companion object {
        private const val TAG = "Member"
    }
}