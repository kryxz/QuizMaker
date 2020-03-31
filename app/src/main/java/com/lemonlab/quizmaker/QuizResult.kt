package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_quiz_result.*
import java.util.*
import kotlin.collections.HashMap


class QuizResult : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadData()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadData() {
        val args = QuizResultArgs.fromBundle(arguments!!)
        val avg = (args.score / args.totalQuestions * 100).toString() + "%"
        quizScoreText.text =
            getString(R.string.quizScore, args.quizTitle, args.totalQuestions, args.score, avg)
        shouldRate(args.quizID)
        if (canMessageAuthor())
            messageQuizAuthorButton.setOnClickListener {
                sendMessageToAuthorDialog(args.quizAuthor)
            }
        else
            messageQuizAuthorButton.visibility = View.GONE

    }

    private fun canMessageAuthor(): Boolean {
        return QuizResultArgs.fromBundle(arguments!!).quizAuthor != FirebaseAuth.getInstance().currentUser!!.displayName

    }

    private fun sendMessageToAuthorDialog(quizAuthor: String) {
        val dialogBuilder = AlertDialog.Builder(context!!).create()
        val dialogView = with(layoutInflater) {
            inflate(
                R.layout.send_message_dialog,
                null
            )
        }
        dialogView.findViewById<AppCompatButton>(R.id.sendNowButton).setOnClickListener {
            val msg = HashMap<String, Message>(1)
            msg["message"] = Message(
                FirebaseAuth.getInstance().currentUser?.displayName!!,
                dialogView.findViewById<TextInputEditText>(R.id.messageText).text.toString()
                    .removedWhitespace(),
                Calendar.getInstance().timeInMillis,
                ""
            )
            FirebaseFirestore.getInstance().collection("users")
                .document(quizAuthor)
                .collection("messages").add(msg).addOnSuccessListener {
                    it.update("message.id", it.id)
                    NotificationSender().sendNotification(
                        context!!,
                        quizAuthor,
                        NotificationType.MESSAGE
                    )
                    showToast(context!!, getString(R.string.messageSent))
                }
            dialogBuilder.dismiss()

        }
        dialogView.findViewById<AppCompatButton>(R.id.cancelSendButton).setOnClickListener {
            dialogBuilder.dismiss()
        }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }

    private fun logQuiz() {
        val args = QuizResultArgs.fromBundle(arguments!!)
        val fireStore = FirebaseFirestore.getInstance()
        var quizzesLog = QuizLog(mutableListOf())
        val userName = FirebaseAuth.getInstance().currentUser?.displayName!!
        val log = HashMap<String, QuizLog>(1)
        val logRef =
            fireStore.collection("users")
                .document(userName)
                .collection("userData").document("taken")
        logRef.get().addOnSuccessListener {
            if (it != null)
                quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                    it.get("log", QuizLog::class.java)!!
                else
                    QuizLog(mutableListOf())
            //Updates points for author and user, and sends a notification to the quiz author.
            quizzesLog.addQuiz(
                args.quizID,
                userName,
                args.score,
                args.quizAuthor,
                args.totalQuestions,
                context!!
            )
            log["log"] = quizzesLog
            logRef.set(log)
        }

    }

    private fun sendRatingNow(quizID: String) {
        val quizRef = FirebaseFirestore.getInstance().collection("Quizzes").document(quizID)
        TempData.currentQuizzes = null
        quizRef.get()
            .addOnSuccessListener { document ->
                val theQuiz = document.get("quiz.quiz", Quiz::class.java)!!
                theQuiz.setNewRating(quizFinishedRatingBar.rating)
                quizRef.update("quiz.quiz", theQuiz).addOnSuccessListener {
                    showToast(context!!, getString(R.string.ratingSent))
                    showHideRating(View.GONE)
                }
            }
    }

    private fun showHideRating(visibility: Int) {
        sendRatingButton.visibility = visibility
        quizFinishedRatingBar.visibility = visibility
    }

    private fun shouldRate(quizID: String) {
        showHideRating(View.GONE)
        //This decides if a user's rating should be sent to database (users can rate a quiz once).
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.displayName!!)
            .collection("userData").document("taken")
            .get().addOnSuccessListener {
                val quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                    it.get("log", QuizLog::class.java)!!
                else
                    QuizLog(mutableListOf())

                if (!quizzesLog.userLog.contains(quizID)) {
                    showHideRating(View.VISIBLE)
                    sendRatingButton.setOnClickListener {
                        sendRatingNow(quizID)
                    }
                } else {
                    showHideRating(View.VISIBLE)
                    quizFinishedRatingBar.isEnabled = false
                    sendRatingButton.isEnabled = false
                    sendRatingButton.text = getString(R.string.alreadyRated)
                }
                logQuiz()
            }
    }
}
