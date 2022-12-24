package ca.veltus.wraproulette.data.objects

import androidx.annotation.Keep
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MessageListItemFromUserBinding
import ca.veltus.wraproulette.databinding.MessageListItemToUserBinding
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import com.xwray.groupie.databinding.BindableItem
import java.util.*

@Keep
data class Message(
    val text: String,
    val time: Date,
    val senderUid: String,
    val senderName: String,
    val profilePicture: String?,
    var messageUid: String
) {
    constructor() : this("", Date(0), "", "", null, "")
}

class MessageItemFrom(val message: Message) : BindableItem<MessageListItemFromUserBinding>() {

    override fun bind(viewBinding: MessageListItemFromUserBinding, position: Int) {
        viewBinding.message = message
    }

    override fun getLayout(): Int {
        return R.layout.message_list_item_from_user
    }
}

class MessageItemTo(val message: Message) : BindableItem<MessageListItemToUserBinding>() {

    override fun bind(viewBinding: MessageListItemToUserBinding, position: Int) {
        viewBinding.message = message
        if (message.profilePicture != null) {
            Glide.with(viewBinding.root)
                .load(FirebaseStorageUtil.pathToReference(message.profilePicture))
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(viewBinding.profilePictureImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.message_list_item_to_user
    }
}