package com.lemonlab.quizmaker.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.*


class QuizzesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val startQuizButton = itemView.findViewById(R.id.startQuizButton) as AppCompatButton
    val quizAuthorText = itemView.findViewById(R.id.quizAuthorText) as AppCompatTextView
    val quizTitle = itemView.findViewById(R.id.quizTitleText) as AppCompatTextView
    val quizQuestionCount = itemView.findViewById(R.id.questionsCountText) as AppCompatTextView
    val quizTypeTextView = itemView.findViewById(R.id.quizTypeTextView) as AppCompatTextView
    val quizRatingBar = itemView.findViewById(R.id.quizRatingBar) as AppCompatRatingBar

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
            holder.quizRatingBar,
            holder.quizTypeTextView,
            position
        )
    }

    private fun setUp(
        startQuizButton: AppCompatButton,
        quizAuthorText: AppCompatTextView,
        quizTitle: AppCompatTextView,
        quizQuestionCount: AppCompatTextView,
        quizRatingBar: AppCompatRatingBar,
        quizTypeTextView: AppCompatTextView,
        position: Int
    ) {

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.displayName!!)
            .collection("userLog").document("takenQuizzes")
            .get().addOnSuccessListener {
                if (it != null) {
                    val quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                        it.get("log", QuizLog::class.java)!!
                    else
                        QuizLog(mutableListOf())
                    if (quizzesLog.userLog.contains(userQuiz[position].quizUUID))
                        startQuizButton.text = context.getString(R.string.quizAlreadyTaken)
                }
            }

        val quizTypes = context.resources.getStringArray(R.array.quizQuestionsTypes)
        if (userQuiz[position].quizType == QuizType.MultipleChoice)
            quizTypeTextView.text = quizTypes[0]
        else
            quizTypeTextView.text = quizTypes[1]

        quizTitle.text = userQuiz[position].quizTitle
        quizAuthorText.text = context.getString(R.string.quizAuthorText, userQuiz[position].quizAuthor)
        quizQuestionCount.text = context.getString(R.string.questionsCountText, userQuiz[position].questionsCount)
        quizRatingBar.rating = userQuiz[position].rating

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
                    Navigation.findNavController(view).navigate(
                        MainFragmentDirections.goToQuizNow(
                            userQuiz[position].quizUUID
                        )
                    )
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
                Navigation.findNavController(it).navigate(
                    MainFragmentDirections.goToQuizNow(
                        userQuiz[position].quizUUID
                    )
                )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizzesVH {
        return QuizzesVH(
            LayoutInflater.from(context).inflate(
                R.layout.quiz_item,
                parent,
                false
            )
        )
    }

}
