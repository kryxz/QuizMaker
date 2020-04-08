package com.lemonlab.quizmaker.items

import androidx.navigation.findNavController
import com.lemonlab.quizmaker.ProfileFragmentDirections
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.data.Quiz
import com.lemonlab.quizmaker.timeAsAString
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.quiz_hist.view.*


// quiz log as shown in a user profile
class QuizLog(private val quiz: Quiz) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val context = view.context


        with(view) {
            histTitle.text = quiz.quizTitle
            histQuizTotal.text =
                context.getString(R.string.questionsCountText, quiz.questionsCount)

            histQuizAuthor.text =
                context.getString(R.string.quizAuthorText, quiz.quizAuthor)

            histQuizDate.text = quiz.milliSeconds.timeAsAString()

            histQuizType.text = context.getString(quiz.quizType!!.id)
        }


        view.histReviewAnswers.setOnClickListener {
            val action = ProfileFragmentDirections.viewAnswers()
            action.quizID = quiz.quizUUID
            it.findNavController().navigate(action)
        }

    }


    override fun getLayout() = R.layout.quiz_hist

}

