package com.lemonlab.quizmaker.items

import android.graphics.Color
import android.text.format.DateUtils
import com.lemonlab.quizmaker.ChatMessage
import com.lemonlab.quizmaker.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_item.view.*
import java.util.*


class ChatItem(private val msg: ChatMessage, private val teach: String) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val sender = msg.username
        with(view) {
            usernameTV.text = sender
            messageTV.text = msg.text
            dateTV.text = ago(msg.time)

            if (sender == teach)
                usernameTV.setTextColor(Color.GREEN)
            else
                usernameTV.setTextColor(Color.LTGRAY)
        }


    }

    private fun ago(time: Long): String {

        return DateUtils.getRelativeTimeSpanString(
            time,
            Calendar.getInstance().timeInMillis,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()

    }

    override fun getLayout() = R.layout.chat_item
}