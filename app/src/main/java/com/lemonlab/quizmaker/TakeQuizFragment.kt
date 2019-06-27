package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_take_quiz.*
import java.util.*
import kotlin.collections.HashMap


class TakeQuizFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_quiz, container, false)
    }

    override fun onDestroyView() {
        //Hides keypad
        (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view!!.windowToken,
            0
        )
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        decideQuizType()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun decideQuizType() {
        val quizID = TakeQuizFragmentArgs.fromBundle(arguments!!).quizID
        FirebaseFirestore.getInstance().collection("Quizzes").document(quizID).get()
            .addOnSuccessListener { document ->
                val quiz = document.get("quiz.quiz", Quiz::class.java)!!
                if (quiz.quizType == QuizType.MultipleChoice && view != null)
                    setUpMultipleChoiceQuiz(document.get("quiz", MultipleChoiceQuiz::class.java)!!, quizID)
                else if (view != null)
                    setUpTrueFalse(document.get("quiz", TrueFalseQuiz::class.java)!!, quizID)
            }
    }


    private fun logQuiz(quizID: String, pointsToGet: Int, quizAuthor: String) {
        val fireStore = FirebaseFirestore.getInstance()
        var quizzesLog = QuizLog(mutableListOf())
        val userName = FirebaseAuth.getInstance().currentUser?.displayName!!
        val log = HashMap<String, QuizLog>(1)
        val logRef =
            fireStore.collection("users")
                .document(userName)
                .collection("userLog").document("takenQuizzes")
        logRef.get().addOnSuccessListener {
            if (it != null)
                quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                    it.get("log", QuizLog::class.java)!!
                else
                    QuizLog(mutableListOf())
            //Updates points for author and user, and sends a notification to the quiz author.
            quizzesLog.addQuiz(quizID, userName, pointsToGet, quizAuthor, context!!)
            log["log"] = quizzesLog
            logRef.set(log)
        }

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
                dialogView.findViewById<TextInputEditText>(R.id.messageText).text.toString(),
                Calendar.getInstance().timeInMillis
            )
            FirebaseFirestore.getInstance().collection("users")
                .document(quizAuthor)
                .collection("messages").add(msg).addOnSuccessListener {
                    NotificationSender().sendNotification(context!!, quizAuthor, NotificationType.MESSAGE)
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

    private fun disableUI() {
        for (item in quizChoicesGroup.children) {
            (item as AppCompatRadioButton).isEnabled = false
        }
        answerCheckBox.isEnabled = false
    }

    private fun showScoreDialog(score: Int, total: Int, quizID: String, quizAuthor: String) {
        disableUI()
        val quizRef = FirebaseFirestore.getInstance().collection("Quizzes").document(quizID)

        val dialogBuilder = AlertDialog.Builder(context!!).create()
        val dialogView = with(layoutInflater) {
            inflate(
                R.layout.quiz_completed_dialog,
                null
            )
        }

        dialogView.findViewById<AppCompatTextView>(R.id.quizScoreText).text = getString(R.string.score, score, total)

        dialogView.findViewById<AppCompatButton>(R.id.quizFinishedOKButton).setOnClickListener {
            Navigation.findNavController(view!!).navigate(R.id.mainFragment)
            logQuiz(quizID, score, quizAuthor)
            dialogBuilder.dismiss()
        }

        dialogView.findViewById<AppCompatButton>(R.id.messageQuizAuthorButton).setOnClickListener {
            sendMessageToAuthorDialog(quizAuthor)
            dialogBuilder.dismiss()
        }
        val ratingBar: AppCompatRatingBar = dialogView.findViewById(R.id.quizFinishedRatingBar)

        fun sendRatingNow() {
            TempData.currentQuizzes = null
            quizRef.get()
                .addOnSuccessListener { document ->
                    val theQuiz = document.get("quiz.quiz", Quiz::class.java)!!
                    theQuiz.setNewRating(ratingBar.rating)
                    quizRef.update("quiz.quiz", theQuiz).addOnSuccessListener {
                        showToast(context!!, getString(R.string.ratingSent))
                    }
                }
        }

        fun shouldRate() {
            //This decides if a user's rating should be sent to database (users can rate a quiz once).
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.displayName!!)
                .collection("userLog").document("takenQuizzes")
                .get().addOnSuccessListener {
                    val quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                        it.get("log", QuizLog::class.java)!!
                    else
                        QuizLog(mutableListOf())

                    if (!quizzesLog.userLog.contains(quizID)) {
                        sendRatingNow()
                    }
                }

        }
        with(dialogView.findViewById<AppCompatButton>(R.id.sendRatingButton)) {
            fun sendRating() {
                shouldRate()
                this.visibility = View.GONE
                ratingBar.visibility = View.GONE
            }
            setOnClickListener {
                //Send quiz to log.
                logQuiz(quizID, score, quizAuthor)
                //Send Rating to database.
                sendRating()
            }
        }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

    private fun setUpTrueFalse(userQuiz: TrueFalseQuiz, quizID: String) {
        var position = 1
        questionNumberTextView.text = position.toString()
        quizChoicesGroup.visibility = View.GONE
        fun beginQuiz(userQuiz: TrueFalseQuiz) {
            val answersArray = SparseBooleanArray(userQuiz.quiz!!.questionsCount)
            fun updateView() {
                quizQuestionText.text = userQuiz.questions!![(position).toString()]!!.question
                questionNumberTextView.text =
                    getString(R.string.questionNumber, position, userQuiz.quiz.questionsCount)
                answerCheckBox.isChecked = answersArray[position]
            }

            fun nextQuestion() {
                if (position == userQuiz.quiz.questionsCount)
                    return
                answersArray[position] = answerCheckBox.isChecked
                position = position.inc()
                updateView()
            }

            fun previousQuestion() {
                if (position == 1)
                    return
                answersArray[position] = answerCheckBox.isChecked
                position = position.dec()
                updateView()

            }

            fun calculateScore() {
                var score = 0
                answersArray.forEach { key, value ->
                    if (userQuiz.questions!![key.toString()]!!.answer == value)
                        score = score.inc()
                }
                showScoreDialog(
                    score,
                    userQuiz.quiz.questionsCount,
                    quizID,
                    userQuiz.quiz.quizAuthor
                )
            }
            listOf(quizNextQuestionButton, quizPreviousQuestionButton).forEach { button ->
                button.setOnClickListener {
                    when (it) {
                        quizNextQuestionButton -> nextQuestion()
                        quizPreviousQuestionButton -> previousQuestion()
                    }
                }
            }
            answerCheckBox.setOnCheckedChangeListener { _, isChecked ->
                answersArray[position] = isChecked

            }
            submitAnswers.setOnClickListener {
                if (answersArray.size() == userQuiz.quiz.questionsCount)
                    calculateScore()
                else
                    showToast(context!!, getString(R.string.answerQuestions))
            }
            answersArray.put(position, answerCheckBox.isChecked)
            updateView()
        }
        (activity as AppCompatActivity).supportActionBar!!.title = userQuiz.quiz?.quizTitle
        beginQuiz(userQuiz)
        takeQuizProgressBar.visibility = View.GONE
        questionNumberTextView.text =
            getString(R.string.questionNumber, position, userQuiz.quiz!!.questionsCount)

    }

    private fun setUpMultipleChoiceQuiz(userQuiz: MultipleChoiceQuiz, quizID: String) {
        var position = 1
        questionNumberTextView.text = position.toString()
        answerCheckBox.visibility = View.GONE
        fun beginQuiz(userQuiz: MultipleChoiceQuiz) {
            val answersArray = SparseIntArray(userQuiz.quiz!!.questionsCount)
            fun updateTexts() {
                quizQuestionText.text = userQuiz.questions!![(position).toString()]!!.question
                firstAnswer.text = userQuiz.questions[(position).toString()]!!.first
                secondAnswer.text = userQuiz.questions[(position).toString()]!!.second
                thirdAnswer.text = userQuiz.questions[(position).toString()]!!.third
                fourthAnswer.text = userQuiz.questions[(position).toString()]!!.fourth
                questionNumberTextView.text =
                    getString(R.string.questionNumber, position, userQuiz.quiz.questionsCount)
                (quizChoicesGroup.getChildAt(answersArray[position]) as RadioButton).isChecked = true
            }

            fun nextQuestion() {
                if (position == userQuiz.quiz.questionsCount)
                    return
                answersArray[position] =
                    quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                position = position.inc()
                updateTexts()

            }

            fun previousQuestion() {
                if (position == 1)
                    return
                answersArray[position] =
                    quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                position = position.dec()
                updateTexts()
            }

            fun calculateScore() {
                var score = 0
                answersArray[position] =
                    quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                answersArray.forEach { key, value ->
                    if (userQuiz.questions!![key.toString()]!!.correctAnswer == value)
                        score = score.inc()
                }
                showScoreDialog(
                    score,
                    userQuiz.quiz.questionsCount,
                    quizID,
                    userQuiz.quiz.quizAuthor
                )
            }
            listOf(quizNextQuestionButton, quizPreviousQuestionButton).forEach { button ->
                button.setOnClickListener {
                    when (it) {
                        quizNextQuestionButton -> nextQuestion()
                        quizPreviousQuestionButton -> previousQuestion()
                    }
                }
            }
            quizChoicesGroup.setOnCheckedChangeListener { group, _ ->
                if (group.indexOfChild(view!!.findViewById(group.checkedRadioButtonId)) != -1)
                    answersArray[position] = group.indexOfChild(view!!.findViewById(group.checkedRadioButtonId))
            }
            submitAnswers.setOnClickListener {
                when {
                    answersArray.size() == userQuiz.quiz.questionsCount || position == userQuiz.quiz.questionsCount ->
                        calculateScore()
                    else -> showToast(context!!, getString(R.string.answerQuestions))
                }
            }
            updateTexts()
        }
        (activity as AppCompatActivity).supportActionBar!!.title = userQuiz.quiz?.quizTitle
        beginQuiz(userQuiz)
        takeQuizProgressBar.visibility = View.GONE
        questionNumberTextView.text =
            getString(R.string.questionNumber, position, userQuiz.quiz!!.questionsCount)

    }


}
