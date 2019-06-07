package com.lemonlab.quizmaker

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

class ReviewQuestionsRV(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val questionText = itemView.findViewById(R.id.reviewQuestionText) as AppCompatTextView
    val firstChoice = itemView.findViewById(R.id.reviewFirstChoice) as AppCompatTextView
    val secondChoice = itemView.findViewById(R.id.reviewSecondChoice) as AppCompatTextView
    val thirdChoice = itemView.findViewById(R.id.reviewThirdChoice) as AppCompatTextView
    val fourthChoice = itemView.findViewById(R.id.reviewFourthChoice) as AppCompatTextView


}

class QuestionsAdapter(
    private val context: Context,
    private val quizQuestions: LinkedHashMap<String, MultipleChoiceQuestion>
) : RecyclerView.Adapter<ReviewQuestionsRV>() {
    override fun getItemCount() = TempData.questionsCount

    override fun onBindViewHolder(holder: ReviewQuestionsRV, position: Int) {
        setUp(
            holder.questionText,
            holder.firstChoice,
            holder.secondChoice,
            holder.thirdChoice,
            holder.fourthChoice,
            position
        )
    }

    private fun setUp(
        questionsText: AppCompatTextView, firstChoice: AppCompatTextView,
        secondChoice: AppCompatTextView, thirdChoice: AppCompatTextView, fourthChoice: AppCompatTextView, position: Int
    ) {

        questionsText.text =
            context.getString(R.string.questionsTextLabel, quizQuestions[(position + 1).toString()]!!.question)
        val listOfTextAnswers = mutableListOf(firstChoice, secondChoice, thirdChoice, fourthChoice)
        firstChoice.text = quizQuestions[(position + 1).toString()]!!.first
        secondChoice.text = quizQuestions[(position + 1).toString()]!!.second
        thirdChoice.text = quizQuestions[(position + 1).toString()]!!.third
        fourthChoice.text = quizQuestions[(position + 1).toString()]!!.fourth
        listOfTextAnswers[quizQuestions[(position + 1).toString()]!!.correctAnswer].setTextColor(Color.GREEN)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewQuestionsRV {
        return ReviewQuestionsRV(LayoutInflater.from(context).inflate(R.layout.review_question_item, parent, false))
    }
}


class QuizzesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val startQuizButton = itemView.findViewById(R.id.startQuizButton) as AppCompatButton
    val quizAuthorText = itemView.findViewById(R.id.quizAuthorText) as AppCompatTextView
    val quizTitle = itemView.findViewById(R.id.quizTitleText) as AppCompatTextView
    val quizQuestionCount = itemView.findViewById(R.id.questionsCountText) as AppCompatTextView

}

class QuizAdapter(
    private val context: Context,
    private val userQuiz: List<Quiz>
) : RecyclerView.Adapter<QuizzesVH>() {

    override fun getItemCount() = userQuiz.size

    override fun onBindViewHolder(holder: QuizzesVH, position: Int) {
        setUp(
            holder.startQuizButton,
            holder.quizAuthorText,
            holder.quizTitle,
            holder.quizQuestionCount,
            position
        )
    }

    private fun setUp(
        startQuizButton: AppCompatButton,
        quizAuthorText: AppCompatTextView,
        quizTitle: AppCompatTextView,
        quizQuestionCount: AppCompatTextView,
        position: Int
    ) {

        quizTitle.text = userQuiz[position].quizTitle
        quizAuthorText.text = context.getString(R.string.quizAuthorText, userQuiz[position].quizAuthor)
        quizQuestionCount.text = context.getString(R.string.questionsCountText, userQuiz[position].questionsCount)

        Log.i("Data", userQuiz[position].passwordProtected.toString() + " " + position)
        startQuizButton.setOnClickListener {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizzesVH {
        return QuizzesVH(LayoutInflater.from(context).inflate(R.layout.quiz_item, parent, false))
    }

}