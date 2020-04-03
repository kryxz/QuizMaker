package com.lemonlab.quizmaker.items

import android.graphics.Color
import android.view.View
import com.lemonlab.quizmaker.MultipleChoiceQuestion
import com.lemonlab.quizmaker.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mul_review.view.*


class MulReview(private val q: MultipleChoiceQuestion) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView

        initView(view, q)


    }

    companion object {
        fun initView(view: View, q: MultipleChoiceQuestion) {
            with(view) {
                reviewQuestionText.text = context.getString(R.string.questionsTextLabel, q.question)

                val listOfTextAnswers = mutableListOf(
                    reviewFirstChoice,
                    reviewSecondChoice,
                    reviewThirdChoice,
                    reviewFourthChoice
                )
                listOfTextAnswers[0].text = q.first
                listOfTextAnswers[1].text = q.second
                listOfTextAnswers[2].text = q.third
                listOfTextAnswers[3].text = q.fourth

                for (item in listOfTextAnswers) {
                    if (listOfTextAnswers.indexOf(item) == q.correctAnswer)
                        item.setTextColor(Color.GREEN)
                    else
                        item.setTextColor(Color.WHITE)
                }

            }
        }
    }

    override fun getLayout() = R.layout.mul_review
}
