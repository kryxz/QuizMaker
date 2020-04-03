package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
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
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val args = TakeQuizFragmentArgs.fromBundle(arguments!!)
        val quizID = args.quizID

        val classCode = args.classCode

        val vm = (activity as MainActivity).vm

        vm.getQuiz(classCode, quizID).observe(viewLifecycleOwner, Observer {
            if (it != null)
                setUp(it)
        })


    }

    private fun setUp(quiz: Quizzer) {
        var position = 1
        questionNumberTextView.text = position.toString()

        if (quiz is MultipleChoiceQuiz) {
            quizChoicesGroup.visibility = View.VISIBLE
            answerCheckBox.visibility = View.GONE
        } else {
            quizChoicesGroup.visibility = View.GONE
            answerCheckBox.visibility = View.VISIBLE
        }
        val answers = SparseArray<Any>()
        fun updateTextMulti() {
            quizQuestionText.text = (quiz as MultipleChoiceQuiz).getQuestion(position)
            firstAnswer.text = quiz.getChoiceOne(position)
            secondAnswer.text = quiz.getChoiceTwo(position)
            thirdAnswer.text = quiz.getChoiceThree(position)
            fourthAnswer.text = quiz.getChoiceFour(position)

            if (answers[position] != null) {
                val choice = quizChoicesGroup.getChildAt(
                    answers[position].toString().toInt()
                ) as RadioButton
                choice.isChecked = true
            }

        }


        fun updateTextTF() {
            quizQuestionText.text = quiz.getQuestion(position)
            if (answers[position] != null)
                answerCheckBox.isChecked = answers[position] as Boolean
        }

        fun next() {
            if (position == quiz.getSize()) return

            if (quiz is MultipleChoiceQuiz) {
                answers[position] =
                    quizChoicesGroup.indexOfChild(
                        view!!.findViewById(quizChoicesGroup.checkedRadioButtonId)
                    )
                position++
                updateTextMulti()
            } else {
                answers[position] = answerCheckBox.isChecked
                position++
                updateTextTF()
            }
            questionNumberTextView.text =
                getString(R.string.questionNumber, position, quiz.getSize())

        }

        fun prev() {
            if (position == 1) return
            if (quiz is MultipleChoiceQuiz) {
                answers[position] =
                    quizChoicesGroup.indexOfChild(view!!.findViewById(quizChoicesGroup.checkedRadioButtonId))
                position--
                updateTextMulti()

            } else {
                answers[position] = answerCheckBox.isChecked
                position--
                updateTextTF()
            }
            questionNumberTextView.text =
                getString(R.string.questionNumber, position, quiz.getSize())
        }

        fun finish() {

            if (quiz is MultipleChoiceQuiz) {
                answers[position] =
                    quizChoicesGroup.indexOfChild(
                        view!!.findViewById(quizChoicesGroup.checkedRadioButtonId)
                    )
            } else
                answers[position] = answerCheckBox.isChecked


            val action = TakeQuizFragmentDirections.viewResult(
                quiz.getID(),
                quiz.getSize(),
                quiz.score(answers),
                quiz.getAuthor(),
                quiz.getTitle()
            )
            action.classCode = TakeQuizFragmentArgs.fromBundle(arguments!!).classCode
            val navOptions = NavOptions.Builder().setPopUpTo(
                R.id.takeQuizFragment,
                true
            ).build()
            view!!.findNavController().navigate(action, navOptions)
        }

        if (quiz is MultipleChoiceQuiz) {

            quizChoicesGroup.setOnCheckedChangeListener { group, _ ->
                if (group.indexOfChild(view!!.findViewById(group.checkedRadioButtonId)) != -1)
                    answers[position] =
                        group.indexOfChild(view!!.findViewById(group.checkedRadioButtonId))
            }
            updateTextMulti()
        } else {
            answerCheckBox.setOnCheckedChangeListener { _, isChecked ->
                answers[position] = isChecked
            }

            answers.put(position, answerCheckBox.isChecked)
            updateTextTF()
        }


        fun submit() {
            when {
                answers.size() == quiz.getSize() ||
                        position == quiz.getSize() -> finish()
                else -> showToast(context!!, getString(R.string.answerQuestions))
            }
        }

        listOf(quizNextQuestionButton, quizPreviousQuestionButton, submitAnswers)
            .forEach { button ->
                button.setOnClickListener {
                    when (it) {
                        quizNextQuestionButton -> next()
                        quizPreviousQuestionButton -> prev()
                        submitAnswers -> submit()
                    }
                }
            }

        (activity as AppCompatActivity).supportActionBar!!.title = quiz.getTitle()
        takeQuizProgressBar.visibility = View.GONE

        questionNumberTextView.text =
            getString(R.string.questionNumber, position, quiz.getSize())


    }

}
