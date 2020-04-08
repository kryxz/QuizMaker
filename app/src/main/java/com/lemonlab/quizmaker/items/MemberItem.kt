package com.lemonlab.quizmaker.items

import android.view.View
import com.lemonlab.quizmaker.QuizzesVM
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.data.TheClass
import com.lemonlab.quizmaker.showYesNoDialog
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.member_item.view.*

class MemberItem(
    private val name: String,
    private val theClass: TheClass,
    private val vm: QuizzesVM,
    private val update: () -> Unit
) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val view = viewHolder.itemView
        val context = view.context

        with(view) {
            memberName.text = name
            val isAuthor = vm.getName() == theClass.teach
            memberDelete.visibility = if (isAuthor)
                View.VISIBLE
            else
                View.GONE
            if (isAuthor)
                memberDelete.setOnClickListener {
                    context.showYesNoDialog({
                        vm.deleteMember(name, theClass)
                        update()
                    }, {},
                        context.getString(R.string.deleteMember),
                        context.getString(R.string.deleteMemberConfirm)
                    )
                }
        }
    }

    override fun getLayout() = R.layout.member_item

}