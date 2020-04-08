package com.lemonlab.quizmaker.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.NotificationSender
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.data.Message
import com.lemonlab.quizmaker.data.NotificationType
import com.lemonlab.quizmaker.showToast
import java.util.*
import kotlin.collections.HashMap


class MessagesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val senderTextView = itemView.findViewById(R.id.senderTextView) as AppCompatTextView
    val sendDateTextView = itemView.findViewById(R.id.sendDateTextView) as AppCompatTextView
    val messagePreviewTextView =
        itemView.findViewById(R.id.messagePreviewTextView) as AppCompatTextView
    val replyMessageButton = itemView.findViewById(R.id.replyMessageButton) as AppCompatImageView

}

class MessagesAdapter(
    private val context: Context,
    private val messages: MutableList<Message>
) : RecyclerView.Adapter<MessagesVH>() {

    override fun onBindViewHolder(holder: MessagesVH, position: Int) {
        loadMessages(
            holder.senderTextView,
            holder.sendDateTextView,
            holder.messagePreviewTextView,
            holder.replyMessageButton,
            position
        )
    }

    private fun showReplyDialog(messageAuthor: String, message: String) {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.send_reply_dialog,
                null
            )
        }
        dialogView.findViewById<AppCompatTextView>(R.id.messageContentReplyDialog).text = message
        dialogView.findViewById<AppCompatTextView>(R.id.sendToReplyDialog).text =
            context.getString(R.string.sendTo, messageAuthor)

        dialogView.findViewById<AppCompatButton>(R.id.sendNowButtonReplyDialog).setOnClickListener {
            val msg = HashMap<String, Message>(1)
            msg["message"] = Message(
                FirebaseAuth.getInstance().currentUser?.displayName!!,
                dialogView.findViewById<TextInputEditText>(R.id.messageTextReplyDialog).text.toString(),
                Calendar.getInstance().timeInMillis,
                ""
            )
            FirebaseFirestore.getInstance().collection("users")
                .document(messageAuthor)
                .collection("messages").add(msg).addOnSuccessListener {
                    it.update("message.id", it.id)
                    NotificationSender().sendNotification(
                        context,
                        messageAuthor,
                        NotificationType.MESSAGE
                    )
                    context.showToast(context.getString(R.string.messageSent))
                }
            dialogBuilder.dismiss()

        }
        dialogView.findViewById<AppCompatButton>(R.id.cancelSendButtonReplyDialog)
            .setOnClickListener {
                dialogBuilder.dismiss()
            }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

    private fun getDateFromMilliSeconds(milliSeconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH) + 1}  ${calendar.get(
            Calendar.HOUR_OF_DAY
        )}:${calendar.get(
            Calendar.MINUTE
        )}"
    }

    private fun loadMessages(
        senderTextView: AppCompatTextView,
        sendDateTextView: AppCompatTextView,
        messagePreviewTextView: AppCompatTextView,
        replyMessageButton: AppCompatImageView,
        position: Int
    ) {


        senderTextView.text = messages[position].sender
        messagePreviewTextView.text = messages[position].message

        sendDateTextView.text = getDateFromMilliSeconds(messages[position].milliSeconds)
        replyMessageButton.setOnClickListener {
            showReplyDialog(messages[position].sender, messages[position].message)
        }

    }

    fun deleteMessage(i: Int) {
        val messagesRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.displayName!!)
            .collection("messages")

        messagesRef.get().addOnSuccessListener {
            messagesRef.document(messages[i].id).delete().addOnSuccessListener {
                messages.removeAt(i)
                notifyItemRemoved(i)
            }
        }

    }

    override fun getItemCount() = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesVH {
        return MessagesVH(
            LayoutInflater.from(context).inflate(
                R.layout.message_item,
                parent,
                false
            )
        )
    }
}

abstract class SwipeToDeleteCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val intrinsicWidth = deleteIcon!!.intrinsicWidth
    private val intrinsicHeight = deleteIcon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.RED
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder.adapterPosition == 10) return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}