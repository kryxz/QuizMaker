package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
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

    override fun onDestroyView() {
        activity!!.hideKeypad()
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
                    setUpMultipleChoiceQuiz(
                        document.get("quiz", MultipleChoiceQuiz::class.java)!!,
                        quizID
                    )
                else if (view != null)
                    setUpTrueFalse(document.get("quiz", TrueFalseQuiz::class.java)!!, quizID)
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
                answersArray[position] = answerCheckBox.isChecked
                answersArray.forEach { key, value ->
                    if (userQuiz.questions!![key.toString()]!!.answer == value)
                        score = score.inc()
                }
                Navigation.findNavController(view!!).navigate(
                    TakeQuizFragmentDirections.viewResult(
                        quizID,
                        userQuiz.quiz.questionsCount,
                        score,
                        userQuiz.quiz.quizAuthor,
                        userQuiz.quiz.quizTitle
                    ),
                    NavOptions.Builder().setPopUpTo(R.id.takeQuizFragment, true).build()
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
                when {
                    answersArray.size() == userQuiz.quiz.questionsCount ||
                            position == userQuiz.quiz.questionsCount -> calculateScore()
                    else -> showToast(context!!, getString(R.string.answerQuestions))
                }
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
                (quizChoicesGroup.getChildAt(answersArray[position]) as RadioButton).isChecked =
                    true
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
                Navigation.findNavController(view!!).navigate(
                    TakeQuizFragmentDirections.viewResult(
                        quizID,
                        userQuiz.quiz.questionsCount,
                        score,
                        userQuiz.quiz.quizAuthor,
                        userQuiz.quiz.quizTitle
                    ),
                    NavOptions.Builder().setPopUpTo(R.id.takeQuizFragment, true).build()
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
                    answersArray[position] =
                        group.indexOfChild(view!!.findViewById(group.checkedRadioButtonId))
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
