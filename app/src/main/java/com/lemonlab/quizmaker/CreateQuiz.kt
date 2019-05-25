package com.lemonlab.quizmaker


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_quiz.*

enum class QuizCategory {
    Academic, Fun
}

class TempData {
    var quizCategory = QuizCategory.Academic
    var isPasswordProtected = false
    var isOneTimeQuiz = false
    var questionsCount = 0
    var quizPin = 0
}

class CreateQuiz : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUp()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setUp() {
        isPasswordProtected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                quizPasswordEditText.visibility = View.VISIBLE
            else
                quizPasswordEditText.visibility = View.GONE
        }
        ArrayAdapter.createFromResource(
            context!!,
            R.array.quizQuestionsTypes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            questionsTypeSpinner.adapter = adapter
        }
        quizQuestionsCount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.toString().toInt() > 30)
                    quizQuestionsCount.error = getString(R.string.cannotBeGreaterThan20)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        writeQuestionsButton.setOnClickListener {

            if (!quizQuestionsCount.text.isNullOrEmpty() && quizQuestionsCount.text.toString().toInt() >= 1)
                TempData().questionsCount = quizQuestionsCount.text.toString().toInt()
            else
                quizQuestionsCount.error = getString(R.string.cannotBeEmpty)

            if (!quizPasswordEditText.text.isNullOrEmpty() && TempData().isPasswordProtected)
                TempData().quizPin = quizPasswordEditText.text.toString().toInt()
            else
                quizPasswordEditText.error = getString(R.string.cannotBeEmpty)

            TempData().quizCategory =
                if (categoryGroup.checkedRadioButtonId == funRadioButton.id)
                    QuizCategory.Fun
                else
                    QuizCategory.Academic

            TempData().isPasswordProtected = isPasswordProtected.isChecked
            TempData().isOneTimeQuiz = isOneTime.isChecked

        }

    }

    override fun onDestroyView() {
        hideKeypad()
        super.onDestroyView()
    }

    private fun hideKeypad() =
        (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager)
            .hideSoftInputFromWindow(view!!.windowToken, 0)

}
