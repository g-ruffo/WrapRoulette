package ca.veltus.wraproulette.data.objects

import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.databinding.MessageListItemBinding
import com.xwray.groupie.databinding.BindableItem
import java.sql.Date

data class Message(
    val text: String,
    val time: Date,
    val senderUid: String
) {
    constructor() : this("", Date(0), "")
}

class MessageItem(val message: Message) : BindableItem<MessageListItemBinding>() {

    override fun bind(viewBinding: MessageListItemBinding, position: Int) {
        viewBinding.message = message
    }
    override fun getLayout(): Int {
        return R.layout.message_list_item
    }



}