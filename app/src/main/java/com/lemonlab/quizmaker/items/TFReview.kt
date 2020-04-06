package com.lemonlab.quizmaker.items

import android.graphics.Color
import android.view.View
import com.lemonlab.quizmaker.AnswersFragment
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.TrueFalseQuestion
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.tf_review.view.*

class TFReview(private val q: TrueFalseQuestion) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        initView(view, q)
        if (!AnswersFragment.seeAnswers)
            view.reviewIsTrueCheckBox.visibility = View.GONE


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

                with(reviewIsTrueCheckBox) {
                    if (q.answer) {
                        text = context.getString(R.string.trueStatement)
                        setTextColor(Color.GREEN)
                    } else {
                        text = context.getString(R.string.falseStatement)
                        setTextColor(Color.RED)

                    }
                }

            }
        }
    }

    override fun getLayout() = R.layout.tf_review
}

