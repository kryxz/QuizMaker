package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.util.set
import androidx.core.view.forEachIndexed
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
        takeQuizAdBanner.loadAd()
        takeQuizProgressBar.visibility = View.VISIBLE
        answersLayout.visibility = View.GONE
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
        takeQuizProgressBar.visibility = View.GONE
        answersLayout.visibility = View.VISIBLE

        var position = 1
        questionNumberTextView.text = position.toString()

        if (quiz is MultipleChoiceQuiz) {
            quizChoicesGroup.visibility = View.VISIBLE
            answerCheckBox.visibility = View.GONE
        } else {
            quizChoicesGroup.visibility = View.GONE
            answerCheckBox.visibility = View.VISIBLE
        }
        val answers = SparseArray<Any>().also {
            it.clear()
        }

        fun fadeViewInOut(v: View, end: () -> Unit) {
            val duration = if (position == 1)
                0L
            else
                250L
            v.apply {
                animate().alpha(0f).setDuration(duration).withEndAction {
                    alpha = 1f
                    end()
                }.start()
            }
        }

        fun updateTextMulti() {
            fadeViewInOut(quizChoicesGroup) { }

            fadeViewInOut(quizQuestionText) {
                quizQuestionText.text = (quiz as MultipleChoiceQuiz).getQuestion(position)
                firstAnswer.text = quiz.getChoiceOne(position)
                secondAnswer.text = quiz.getChoiceTwo(position)
                thirdAnswer.text = quiz.getChoiceThree(position)
                fourthAnswer.text = quiz.getChoiceFour(position)
            }
            quizChoicesGroup.clearCheck()
            if (answers[position] != -1 && answers[position] != null) {
                val pos = answers[position] as Int
                quizChoicesGroup.forEachIndexed { index, view ->
                    val item = view as AppCompatRadioButton
                    if (index == pos) {
                        item.isChecked = true
                    }
                }

            }

        }


        fun getAnswerFromGroup(): Int {
            var answer: Int = -1
            quizChoicesGroup.forEachIndexed { index, view ->
                val item = view as AppCompatRadioButton
                if (item.isChecked)
                    answer = index
            }
            return if (answers[position] == -1 || answers[position] == null)
                answer
            else
                answers[position] as Int

        }

        fun getAnswerCheckBox(): Boolean {

            return if (answers[position] == null)
                answerCheckBox.isChecked
            else
                answers[position] as Boolean
        }

        fun updateTextTF() {
            fadeViewInOut(quizQuestionText) {
                quizQuestionText.text = quiz.getQuestion(position)

            }

            with(answerCheckBox) {
                fadeViewInOut(this) {
                    answerCheckBox.isChecked = getAnswerCheckBox()
                }
            }

        }

        fun next() {
            if (position == quiz.getSize()) return
            if (quiz is MultipleChoiceQuiz) {
                answers[position] = getAnswerFromGroup()
                position++
                updateTextMulti()
            } else {

                answers[position] = getAnswerCheckBox()
                position++
                updateTextTF()
            }

            with(questionNumberTextView) {
                fadeViewInOut(this) {
                    text = getString(R.string.questionNumber, position, quiz.getSize())
                }
            }

        }

        fun prev() {
            if (position == 1) return
            if (quiz is MultipleChoiceQuiz) {
                answers[position] = getAnswerFromGroup()
                position--
                updateTextMulti()

            } else {
                answers[position] = answerCheckBox.isChecked
                position--
                updateTextTF()
            }
            with(questionNumberTextView) {
                fadeViewInOut(this) {
                    text = getString(R.string.questionNumber, position, quiz.getSize())
                }
            }
        }

        fun finish() {

            if (quiz is MultipleChoiceQuiz)
                answers[position] = getAnswerFromGroup()
            else
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
            quizChoicesGroup.setOnCheckedChangeListener { _, _ ->
                answers[position] = getAnswerFromGroup()

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
                else -> context!!.showToast(getString(R.string.answerQuestions))
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
