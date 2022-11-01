package ca.veltus.wraproulette.data.objects

import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MemberListItemBinding
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import com.xwray.groupie.databinding.BindableItem
import java.util.*

data class Member(
    val uid: String?,
    val poolId: String,
    val displayName: String,
    val email: String?,
    val department: String?,
    val bidTime: Date?,
    val profilePicturePath: String?
) {
    constructor() : this("", "", "", "", null, null, null)
}

class MemberItem(
    val member: Member
) : BindableItem<MemberListItemBinding>() {

    override fun bind(viewBinding: MemberListItemBinding, position: Int) {
        viewBinding.member = member
        if (member.profilePicturePath != null) {
            Glide.with(viewBinding.root)
                .load(FirebaseStorageUtil.pathToReference(member.profilePicturePath))
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(viewBinding.memberProfileImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.member_list_item
    }

    companion object {
        private const val TAG = "Member"
    }
}