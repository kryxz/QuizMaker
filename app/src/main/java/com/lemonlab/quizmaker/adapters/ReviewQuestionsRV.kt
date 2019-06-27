package com.lemonlab.quizmaker.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.lemonlab.quizmaker.*

class ReviewQuestionsRV(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val questionText = itemView.findViewById(R.id.reviewQuestionText) as AppCompatTextView
    val firstChoice = itemView.findViewById(R.id.reviewFirstChoice) as AppCompatTextView
    val secondChoice = itemView.findViewById(R.id.reviewSecondChoice) as AppCompatTextView
    val thirdChoice = itemView.findViewById(R.id.reviewThirdChoice) as AppCompatTextView
    val fourthChoice = itemView.findViewById(R.id.reviewFourthChoice) as AppCompatTextView
    val reviewMultipleChoiceLayout = itemView.findViewById(R.id.reviewMultipleChoiceLayout) as LinearLayout
    val reviewIsTrueCheckBox = itemView.findViewById(R.id.reviewIsTrueCheckBox) as AppCompatCheckBox


}

class QuestionsAdapter(
    private val context: Context,
    private val multipleChoiceQuestions: LinkedHashMap<String, MultipleChoiceQuestion>?,
    private val trueFalseQuestions: LinkedHashMap<String, TrueFalseQuestion>?
) : RecyclerView.Adapter<ReviewQuestionsRV>() {
    override fun getItemCount() = TempData.questionsCount

    override fun onBindViewHolder(holder: ReviewQuestionsRV, position: Int) {
        if (TempData.quizType == QuizType.MultipleChoice)
            setUpMultipleChoice(
                holder.questionText,
                holder.firstChoice,
                holder.secondChoice,
                holder.thirdChoice,
                holder.fourthChoice,
                position,
                holder.reviewIsTrueCheckBox
            )
        else
            setUpTrueFalse(
                holder.questionText,
                holder.reviewMultipleChoiceLayout,
                holder.reviewIsTrueCheckBox,
                position
            )
    }

    private fun setUpTrueFalse(
        questionsText: AppCompatTextView,
        reviewMultipleChoiceLayout: LinearLayout,
        reviewIsTrueCheckBox: AppCompatCheckBox,
        position: Int
    ) {
        reviewIsTrueCheckBox.visibility = View.VISIBLE
        reviewMultipleChoiceLayout.visibility = View.GONE
        reviewIsTrueCheckBox.buttonDrawable = null
        questionsText.text = context.getString(
            R.string.questionsTextLabel,
            trueFalseQuestions!![(position + 1).toString()]!!.question
        )
        if (trueFalseQuestions[(position + 1).toString()]!!.answer)
            reviewIsTrueCheckBox.text = context.getString(R.string.trueStatement)
        else
            reviewIsTrueCheckBox.text = context.getString(R.string.falseStatement)

    }

    private fun setUpMultipleChoice(
        questionsText: AppCompatTextView, firstChoice: AppCompatTextView,
        secondChoice: AppCompatTextView, thirdChoice: AppCompatTextView, fourthChoice: AppCompatTextView,
        position: Int, reviewIsTrueCheckBox: AppCompatCheckBox
    ) {
        reviewIsTrueCheckBox.visibility = View.GONE
        questionsText.text =
            context.getString(
                R.string.questionsTextLabel,
                multipleChoiceQuestions!![(position + 1).toString()]!!.question
            )
        val listOfTextAnswers = mutableListOf(firstChoice, secondChoice, thirdChoice, fourthChoice)
        firstChoice.text = multipleChoiceQuestions[(position + 1).toString()]!!.first
        secondChoice.text = multipleChoiceQuestions[(position + 1).toString()]!!.second
        thirdChoice.text = multipleChoiceQuestions[(position + 1).toString()]!!.third
        fourthChoice.text = multipleChoiceQuestions[(position + 1).toString()]!!.fourth
        listOfTextAnswers[multipleChoiceQuestions[(position + 1).toString()]!!.correctAnswer].setTextColor(Color.GREEN)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewQuestionsRV {
        return ReviewQuestionsRV(
            LayoutInflater.from(context).inflate(
                R.layout.review_question_item,
                parent,
                false
            )
        )
    }
}


