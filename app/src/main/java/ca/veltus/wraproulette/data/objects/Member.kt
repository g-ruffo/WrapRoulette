package ca.veltus.wraproulette.data.objects

import android.view.View
import androidx.annotation.Keep
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MemberBidListItemBinding
import ca.veltus.wraproulette.databinding.MemberListItemBinding
import ca.veltus.wraproulette.databinding.WinnerMemberItemBinding
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import com.xwray.groupie.databinding.BindableItem
import java.util.*

@Keep
data class Member(
    val uid: String?,
    var tempMemberUid: String?,
    val poolId: String,
    val displayName: String,
    val email: String?,
    val department: String?,
    val bidTime: Date?,
    val profilePicturePath: String?,
    var winnings: Int?,
    var activeMember: Boolean
) {
    constructor() : this("", null, "", "", "", null, null, null, null, true)
}

class MemberItem(
    val member: Member, private val userUid: String
) : BindableItem<MemberListItemBinding>() {

    override fun bind(viewBinding: MemberListItemBinding, position: Int) {
        viewBinding.member = member

        // Check if current user is the pool admin. If not hide the items edit button.
        if (userUid != member.uid || member.tempMemberUid == null) {
            viewBinding.editMemberIcon.visibility = View.GONE
            viewBinding.cardView.isClickable = false
            viewBinding.cardView.isFocusable = false
        }
        // If member has left the pool reduce the alpha of item by 50%
        if (!member.activeMember) {
            viewBinding.mainLayout.alpha = 0.5f
        }

        // If the user has uploaded a profile image, retrieve it from Firebase Storage and replace the placeholder.
        if (member.profilePicturePath != null) {
            Glide.with(viewBinding.root)
                .load(FirebaseStorageUtil.pathToReference(member.profilePicturePath))
                .placeholder(R.drawable.no_profile_image_member)
                .into(viewBinding.memberProfileImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.member_list_item
    }
}

class MemberBidItem(val member: Member) : BindableItem<MemberBidListItemBinding>() {

    override fun bind(viewBinding: MemberBidListItemBinding, position: Int) {
        viewBinding.member = member

        // If member has left the pool reduce the alpha of item by 50%
        if (!member.activeMember) {
            viewBinding.mainLayout.alpha = 0.5f
        }

        // If the user has uploaded a profile image, retrieve it from Firebase Storage and replace the placeholder.
        if (member.profilePicturePath != null) {
            Glide.with(viewBinding.root)
                .load(FirebaseStorageUtil.pathToReference(member.profilePicturePath))
                .placeholder(R.drawable.no_profile_image_member)
                .into(viewBinding.memberProfileImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.member_bid_list_item
    }
}

class WinnerMemberItem(val member: Member) : BindableItem<WinnerMemberItemBinding>() {

    override fun bind(viewBinding: WinnerMemberItemBinding, position: Int) {
        viewBinding.member = member

        // If the user has uploaded a profile image, retrieve it from Firebase Storage and replace the placeholder.
        if (member.profilePicturePath != null) {
            Glide.with(viewBinding.root)
                .load(FirebaseStorageUtil.pathToReference(member.profilePicturePath))
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(viewBinding.profilePictureImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.winner_member_item
    }
}