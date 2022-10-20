package ca.veltus.wraproulette.data.objects

import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MessageListItemFromUserBinding
import ca.veltus.wraproulette.databinding.MessageListItemToUserBinding
import com.xwray.groupie.databinding.BindableItem
import java.util.*

data class Message(
    val text: String,
    val time: Date,
    val senderUid: String,
    val profilePicture: String?,
    val messageUid: String
) {
    constructor() : this("", Date(0), "", null, "")
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
    }

    override fun getLayout(): Int {
        return R.layout.message_list_item_to_user
    }
}