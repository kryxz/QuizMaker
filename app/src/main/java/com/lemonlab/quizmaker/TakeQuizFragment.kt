package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_take_quiz.*


class TakeQuizFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        decideQuizType()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun decideQuizType() {
        val quizID = TakeQuizFragmentArgs.fromBundle(arguments!!).quizID
        FirebaseFirestore.getInstance().collection("Quizzes").document(quizID).get()
            .addOnSuccessListener { document ->
                val quiz = document.get("quiz", MultipleChoiceQuiz::class.java)!!.quiz
                if (quiz!!.quizType == QuizType.MultipleChoice)
                    setUpMultipleChoiceQuiz(quizID)
                else
                    setUpTrueFalse(quizID)
            }
    }


    private fun logQuiz(quizID: String) {
        var quizzesLog = QuizLog(mutableListOf())
        val log = HashMap<String, QuizLog>(1)
        val logRef =
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.displayName!!)
                .collection("userLog").document("takenQuizzes")
        logRef.get().addOnSuccessListener {
            if (it != null)
                quizzesLog = if (it.get("log", QuizLog::class.java) != null)
                    it.get("log", QuizLog::class.java)!!
                else
                    QuizLog(mutableListOf())

            quizzesLog.addQuiz(quizID)
            log["log"] = quizzesLog
            logRef.set(log)
        }

    }


    private fun showScoreDialog(score: Int, total: Int, quizID: String, quizType: QuizType) {
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
            TempData.currentQuizzes = null
            logQuiz(quizID)
            dialogBuilder.dismiss()
        }

        dialogView.findViewById<AppCompatButton>(R.id.messageQuizAuthorButton).setOnClickListener {
            //Show send message dialog
            dialogBuilder.dismiss()
        }
        val ratingBar: AppCompatRatingBar = dialogView.findViewById(R.id.quizFinishedRatingBar)

        fun sendNowMultipleChoice() {
            quizRef.get()
                .addOnSuccessListener { document ->
                    val userQuiz = document.get("quiz", MultipleChoiceQuiz::class.java)!!
                    val quiz: HashMap<String, MultipleChoiceQuiz> = HashMap()

                    with(userQuiz.quiz!!) {
                        setNewRating(ratingBar.rating)
                    }

                    quiz["quiz"] = userQuiz

                    quizRef.set(quiz).addOnSuccessListener {
                        Toast.makeText(context!!, getString(R.string.ratingSent), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        fun sendNowTrueFalse() {
            quizRef.get()
                .addOnSuccessListener { document ->
                    val userQuiz = document.get("quiz", TrueFalseQuiz::class.java)!!
                    val quiz: HashMap<String, TrueFalseQuiz> = HashMap()

                    with(userQuiz.quiz!!) {
                        setNewRating(ratingBar.rating)
                    }

                    quiz["quiz"] = userQuiz

                    quizRef.set(quiz).addOnSuccessListener {
                        Toast.makeText(context!!, getString(R.string.ratingSent), Toast.LENGTH_SHORT).show()
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
                        if (quizType == QuizType.MultipleChoice)
                            sendNowMultipleChoice()
                        else
                            sendNowTrueFalse()
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
                logQuiz(quizID)
                //Send Rating to database.
                sendRating()
            }
        }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

    private fun setUpTrueFalse(quizID: String) {
        val fireStoreDatabase = FirebaseFirestore.getInstance()
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
                showScoreDialog(score, userQuiz.quiz.questionsCount, quizID, userQuiz.quiz.quizType!!)
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
            }
            answersArray.put(position, answerCheckBox.isChecked)
            updateView()
        }
        fireStoreDatabase.collection("Quizzes").document(quizID).get()
            .addOnSuccessListener { document ->
                val userQuiz = document.get("quiz", TrueFalseQuiz::class.java)!!
                (activity as AppCompatActivity).supportActionBar!!.title = userQuiz.quiz?.quizTitle
                beginQuiz(userQuiz)
                takeQuizProgressBar.visibility = View.GONE
                questionNumberTextView.text =
                    getString(R.string.questionNumber, position, userQuiz.quiz!!.questionsCount)

            }
    }

    private fun setUpMultipleChoiceQuiz(quizID: String) {
        val fireStoreDatabase = FirebaseFirestore.getInstance()
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
            }

            fun nextQuestion() {
                if (position == userQuiz.quiz.questionsCount)
                    return
                answersArray[position] = quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                position = position.inc()
                (quizChoicesGroup.getChildAt(answersArray[position]) as RadioButton).isChecked = true
                updateTexts()

            }

            fun previousQuestion() {
                if (position == 1)
                    return
                answersArray[position] = quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                position = position.dec()
                (quizChoicesGroup.getChildAt(answersArray[position]) as RadioButton).isChecked = true
                updateTexts()
            }

            fun calculateScore() {
                var score = 0
                answersArray.forEach { key, value ->
                    if (userQuiz.questions!![key.toString()]!!.correctAnswer == value)
                        score = score.inc()
                }
                showScoreDialog(score, userQuiz.quiz.questionsCount, quizID, userQuiz.quiz.quizType!!)
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
                if (answersArray.size() == userQuiz.quiz.questionsCount)
                    calculateScore()
            }
            updateTexts()
        }
        fireStoreDatabase.collection("Quizzes").document(quizID).get()
            .addOnSuccessListener { document ->
                val userQuiz = document.get("quiz", MultipleChoiceQuiz::class.java)!!
                (activity as AppCompatActivity).supportActionBar!!.title = userQuiz.quiz?.quizTitle
                beginQuiz(userQuiz)
                takeQuizProgressBar.visibility = View.GONE
                questionNumberTextView.text = getString(R.string.questionNumber, position, userQuiz.quiz!!.questionsCount)

            }
    }


}
