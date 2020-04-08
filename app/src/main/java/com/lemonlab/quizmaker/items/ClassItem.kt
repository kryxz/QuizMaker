package com.lemonlab.quizmaker.items

import android.content.Context
import android.view.View
import androidx.navigation.findNavController
import com.lemonlab.quizmaker.*
import com.lemonlab.quizmaker.data.TheClass
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
                createLink.visibility = View.VISIBLE
                createLink.setOnClickListener {
                    copyLink(context)
                }
            } else {
                classCodeText.visibility = View.GONE
                createLink.visibility = View.GONE

            }

            enterClass.setOnClickListener {
                it.findNavController().navigate(ClassFragmentDirections.goToClass(theClass.id))
            }
            leaveClass.setOnClickListener {

                leave(theClass, this@ClassItem)
            }
        }

    }

    private fun copyLink(context: Context) {
        val url = "www.lemonLabQuizMaker.com/${theClass.id}"
        context.copyText(url)
    }


    private fun copyID(context: Context) {
        context.copyText(theClass.id)
    }

    override fun getLayout() = R.layout.class_item


}


class PublicClassItem(private val theClass: TheClass, private val join: (that: TheClass) -> Unit) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        with(view) {
            classTitleText.text = theClass.title
            classInstructorText.text = theClass.teach
            classCreationDateText.text = theClass.date.timeAsAString()

            with(view.enterClass) {
                textSize = 19.0f
                setOnClickListener {
                    join(theClass)
                    it.findNavController()
                        .navigate(JoinFragmentDirections.joinThisClass(theClass.id))

                }
            }
        }

    }

    override fun getLayout() = R.layout.class_item


}