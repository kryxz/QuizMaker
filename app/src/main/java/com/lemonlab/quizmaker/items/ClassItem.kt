package com.lemonlab.quizmaker.items

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.navigation.findNavController
import com.lemonlab.quizmaker.*
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.class_item.view.*


class ClassItem(
    private val theClass: TheClass,
    private val leave: (theClass: TheClass, group: ClassItem) -> Unit,
    private val userName: String
) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        with(view) {
            classTitleText.text = theClass.title
            classInstructorText.text = theClass.teach
            classCreationDateText.text = theClass.date.timeAsAString()
            leaveClass.visibility = View.VISIBLE

            if (theClass.open || theClass.teach == userName) {
                classCodeText.text = context.getString(R.string.joinCode, theClass.id)
                classCodeText.visibility = View.VISIBLE
                classCodeText.setOnClickListener {
                    copyID(context)
                }
            }

            enterClass.setOnClickListener {
                it.findNavController().navigate(ClassFragmentDirections.goToClass(theClass.id))
            }
            leaveClass.setOnClickListener {

                leave(theClass, this@ClassItem)
            }
        }

    }

    private fun copyID(context: Context) {
        with(context) {
            val clip = ClipData.newPlainText("code", theClass.id)
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }

    }

    override fun getLayout() = R.layout.class_item


}


class PublicClassItem(private val theClass: TheClass, private val join: (that: TheClass) -> Unit) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        view.classTitleText.text = theClass.title
        view.classInstructorText.text = theClass.teach
        view.classCreationDateText.text = theClass.date.timeAsAString()

        view.enterClass.setOnClickListener {
            join(theClass)
            it.findNavController().navigate(JoinFragmentDirections.joinThisClass(theClass.id))

        }
    }

    override fun getLayout() = R.layout.class_item


}