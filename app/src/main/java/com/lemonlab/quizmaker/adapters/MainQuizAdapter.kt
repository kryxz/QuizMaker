package com.lemonlab.quizmaker.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.*
import java.util.*
import kotlin.collections.HashMap


class QuizzesVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val startQuizButton = itemView.findViewById(R.id.startQuizButton) as AppCompatButton
    val quizAuthorText = itemView.findViewById(R.id.quizAuthorText) as AppCompatTextView
    val quizTitle = itemView.findViewById(R.id.quizTitleText) as AppCompatTextView
    val quizQuestionCount = itemView.findViewById(R.id.questionsCountText) as AppCompatTextView
    val quizTypeTextView = itemView.findViewById(R.id.quizTypeTextView) as AppCompatTextView
    val quizDateTextView = itemView.findViewById(R.id.quizDateTextView) as AppCompatTextView
    val reportQuiz = itemView.findViewById(R.id.reportQuiz) as AppCompatTextView

    val quizRatingBar = itemView.findViewById(R.id.quizRatingBar) as AppCompatRatingBar

}

class QuizAdapter(
    private val context: Context,
    private val userQuiz: List<Quiz>,
    private val viewType: ViewType
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
            holder.quizDateTextView,
            holder.reportQuiz,
            position
        )
    }

    private fun getDateFromMilliSeconds(milliSeconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH) + 1}  ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(
            Calendar.MINUTE
        )}"
    }

    private fun setUp(
        startQuizButton: AppCompatButton,
        quizAuthorText: AppCompatTextView,
        quizTitle: AppCompatTextView,
        quizQuestionCount: AppCompatTextView,
        quizRatingBar: AppCompatRatingBar,
        quizTypeTextView: AppCompatTextView,
        quizDateTextView: AppCompatTextView,
        reportQuiz: AppCompatTextView,
        position: Int
    ) {
        if (viewType == ViewType.ViewAnswers) {
            startQuizButton.text = context.getString(R.string.viewAnswers)
        }
        //decides if user already took this quiz
        else
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser!!.displayName.toString())
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


        //sets texts.
        val quizTypes = context.resources.getStringArray(R.array.quizQuestionsTypes)
        if (userQuiz[position].quizType == QuizType.MultipleChoice)
            quizTypeTextView.text = quizTypes[0]
        else
            quizTypeTextView.text = quizTypes[1]


        quizTitle.text = userQuiz[position].quizTitle
        quizAuthorText.text = context.getString(R.string.quizAuthorText, userQuiz[position].quizAuthor)
        quizQuestionCount.text = context.getString(R.string.questionsCountText, userQuiz[position].questionsCount)
        quizRatingBar.rating = userQuiz[position].rating
        quizDateTextView.text = getDateFromMilliSeconds(userQuiz[position].milliSeconds)
        if (viewType != ViewType.ViewAnswers)
            quizAuthorText.setOnClickListener {
                val action = MainFragmentDirections.viewProfile()
                action.isViewer = true
                action.username = userQuiz[position].quizAuthor
                Navigation.findNavController(it).navigate(action)
            }
        fun showReportDialog() {
            val dialogBuilder = android.app.AlertDialog.Builder(context).create()
            val dialogView = with(LayoutInflater.from(context)) {
                inflate(
                    R.layout.yes_no_dialog,
                    null
                )
            }
            dialogView.findViewById<AppCompatTextView>(R.id.dialogTitle).text = context.getString(R.string.reportQuiz)
            dialogView.findViewById<AppCompatTextView>(R.id.dialogMessageText).text =
                context.getString(R.string.reportQuizConfirm)

            dialogView.findViewById<AppCompatButton>(R.id.dialogCancelButton).setOnClickListener {
                dialogBuilder.dismiss()
            }

            dialogView.findViewById<AppCompatButton>(R.id.dialogConfirmButton).setOnClickListener {
                val reports = HashMap<String, Report>()
                val userID = FirebaseAuth.getInstance().currentUser!!.uid
                val quizRef =
                    FirebaseFirestore.getInstance().collection("userReports").document(userQuiz[position].quizUUID)

                quizRef.get().addOnSuccessListener {
                    reports["reports"] = if (it.get("reports", Report::class.java) != null)
                        it.get("reports", Report::class.java)!!
                    else
                        Report("", 0)
                    if (reports["reports"] != null) {
                        reports["reports"]!!.report(userID, userQuiz[position].quizUUID)
                    }
                    quizRef.set(reports)
                }

                dialogBuilder.dismiss()
            }

            with(dialogBuilder) {
                setView(dialogView)
                show()
            }

        }
        reportQuiz.setOnClickListener {
            showReportDialog()
        }

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
                    showToast(context, context.getString(R.string.wrongPassword))
                dialogBuilder.dismiss()
            }
            cancelButton.setOnClickListener { dialogBuilder.dismiss() }
            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

        }
        if (viewType == ViewType.ViewAnswers) {
            startQuizButton.setOnClickListener {
                val action = ProfileFragmentDirections.viewAnswers()
                action.quizID = userQuiz[position].quizUUID
                Navigation.findNavController(it).navigate(action)

            }
        } else
            startQuizButton.setOnClickListener {
                it.animate().scaleX(.3f).scaleY(.3f).setDuration(50)
                    .withEndAction {
                        if (userQuiz[position].passwordProtected) {
                            enterPasswordDialog(it)
                            it.animate().scaleX(1f).scaleY(1f).duration = 50
                        } else
                            Navigation.findNavController(it).navigate(
                                MainFragmentDirections.goToQuizNow(
                                    userQuiz[position].quizUUID
                                )
                            )
                    }


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
