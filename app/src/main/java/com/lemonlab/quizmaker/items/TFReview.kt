package com.lemonlab.quizmaker.items

import android.view.View
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.TrueFalseQuestion
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.tf_review.view.*

class TFReview(private val q: TrueFalseQuestion) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        initView(view, q)

    }

    companion object {
        fun initView(view: View, q: TrueFalseQuestion) {
            with(view) {
                reviewIsTrueCheckBox.visibility = View.VISIBLE
                reviewIsTrueCheckBox.buttonDrawable = null
                reviewQuestionText.text = context.getString(
                    R.string.questionsTextLabel,
                    q.question
                )
                if (q.answer)
                    reviewIsTrueCheckBox.text = context.getString(R.string.trueStatement)
                else
                    reviewIsTrueCheckBox.text = context.getString(R.string.falseStatement)
            }
        }
    }

    override fun getLayout() = R.layout.tf_review
}

