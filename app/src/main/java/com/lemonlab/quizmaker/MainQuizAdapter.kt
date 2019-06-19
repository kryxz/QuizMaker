package com.lemonlab.quizmaker

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.get
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText


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

        fun enterPasswordDialog(view: View) {
            val dialogBuilder = AlertDialog.Builder(context).create()
            val dialogView = with(LayoutInflater.from(context)) {
                inflate(
                    R.layout.enter_password_dialog,
                    null
                )
            }
            val passwordField = dialogView.findViewById<TextInputEditText>(R.id.enterQuizPasswordEditText)
            val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmQuizPassword)
            val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelPasswordDialog)

            confirmButton.setOnClickListener {
                if (passwordField.text.toString() == userQuiz[position].quizPin)
                    Navigation.findNavController(view).navigate(MainFragmentDirections.goToQuizNow(userQuiz[position].userUID))
                else if (passwordField.text!!.isNotBlank())
                    Toast.makeText(context, context.getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show()
                dialogBuilder.dismiss()
            }
            cancelButton.setOnClickListener { dialogBuilder.dismiss() }
            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

        }

        startQuizButton.setOnClickListener {
            if (userQuiz[position].passwordProtected)
                enterPasswordDialog(it)
            else
                Navigation.findNavController(it).navigate(MainFragmentDirections.goToQuizNow(userQuiz[position].userUID))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizzesVH {
        return QuizzesVH(LayoutInflater.from(context).inflate(R.layout.quiz_item, parent, false))
    }

}

class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val quizQuestionText = itemView.findViewById(R.id.quizQuestionText) as AppCompatTextView
    val quizChoicesGroup = itemView.findViewById(R.id.quizChoicesGroup) as RadioGroup
    val submitAnswers = itemView.findViewById(R.id.submitAnswers) as AppCompatButton

}

class TakeQuizAdapter(
    private val context: Context,
    private val multipleChoiceQuiz: MultipleChoiceQuiz?,
    private val trueFalseQuiz: MultipleChoiceQuiz?
) : RecyclerView.Adapter<QuizViewHolder>() {

    override fun getItemCount(): Int {
        return if (multipleChoiceQuiz != null)
            multipleChoiceQuiz.quiz!!.questionsCount
        else
            trueFalseQuiz!!.quiz!!.questionsCount
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        if (multipleChoiceQuiz != null)
            setUpMultipleChoice(
                holder.quizQuestionText, holder.quizChoicesGroup,
                holder.submitAnswers, position
            )
        else
            setUpTrueFalse(
                holder.quizQuestionText,
                holder.submitAnswers, position
            )
    }

    private fun setUpMultipleChoice(
        quizQuestionText: AppCompatTextView, quizChoicesGroup: RadioGroup,
        submitAnswers: AppCompatButton, position: Int
    ) {
        quizQuestionText.text = multipleChoiceQuiz!!.questions!![(position + 1).toString()]!!.question

        (quizChoicesGroup[0] as AppCompatRadioButton).text =
            multipleChoiceQuiz.questions!![(position + 1).toString()]!!.first
        (quizChoicesGroup[1] as AppCompatRadioButton).text =
            multipleChoiceQuiz.questions[(position + 1).toString()]!!.second
        (quizChoicesGroup[2] as AppCompatRadioButton).text =
            multipleChoiceQuiz.questions[(position + 1).toString()]!!.third
        (quizChoicesGroup[3] as AppCompatRadioButton).text =
            multipleChoiceQuiz.questions[(position + 1).toString()]!!.fourth
        submitAnswers.setOnClickListener { }


    }


    private fun setUpTrueFalse(
        quizQuestionText: AppCompatTextView,
        submitAnswers: AppCompatButton, position: Int
    ) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        return QuizViewHolder(LayoutInflater.from(context).inflate(R.layout.questions_item, parent, false))

    }


}