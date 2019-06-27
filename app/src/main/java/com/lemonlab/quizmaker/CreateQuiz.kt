package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_create_quiz.*
import kotlin.collections.LinkedHashMap

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

    private fun textWatchers() {
        //Adds on textChanged listeners to all edit texts, and handles errors and assignments.
        //Adds an adapter to the spinner, too.

        quizQuestionsCount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.toString().toInt() > 30)
                    quizQuestionsCount.error = getString(R.string.cannotBeGreaterThan30)
                else
                    TempData.questionsCount = if (s.toString().isNotEmpty())
                        s.toString().toInt()
                    else
                        0
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        quizPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank())
                    quizPasswordEditText.error = getString(R.string.cannotBeEmpty)
                else
                    TempData.quizPin = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        quizTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank())
                    quizTitleEditText.error = getString(R.string.cannotBeEmpty)
                else
                    TempData.quizTitle = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        ArrayAdapter.createFromResource(
            context!!,
            R.array.quizQuestionsTypes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            questionsTypeSpinner.adapter = adapter
        }
    }

    private fun getPreviousData() {
        isPasswordProtected.isChecked = TempData.isPasswordProtected
        if (TempData.quizTitle.isNotEmpty())
            quizTitleEditText.setText(TempData.quizTitle)
        if (TempData.quizPin != "notRequired")
            quizPasswordEditText.setText(TempData.quizPin)
        if (TempData.questionsCount != 0)
            quizQuestionsCount.setText(TempData.questionsCount.toString())
    }

    private fun setUp() {
        getPreviousData()

        //toRemove saved questions
        fun deleteCached() {
            TempData.deleteCached()
            Navigation.findNavController(view!!).navigate(R.id.viewEditQuestions)
        }

        fun navigateToEditingQuestions() {
            Navigation.findNavController(view!!).navigate(R.id.viewEditQuestions)
        }
        textWatchers()

        isPasswordProtected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                quizPasswordEditText.visibility = View.VISIBLE
            else
                quizPasswordEditText.visibility = View.GONE

            TempData.isPasswordProtected = isChecked
            TempData.quizPin = if (isChecked)
                ""
            else
                "notRequired"
        }

        writeQuestionsButton.setOnClickListener {
            TempData.quizType = if (questionsTypeSpinner.selectedItemPosition == 0)
                QuizType.MultipleChoice
            else
                QuizType.TrueFalse

            if (TempData.multiChoiceCachedQuestions != null && TempData.quizType == QuizType.MultipleChoice ||
                TempData.trueFalseCachedQuestions != null && TempData.quizType == QuizType.TrueFalse
            ) {
                context!!.showYesNoDialog(
                    ::deleteCached,
                    ::navigateToEditingQuestions,
                    getString(R.string.existingQuestions),
                    getString(R.string.existingQuestionsMessage)
                )
            } else if (TempData.quizPin != "" && TempData.quizTitle != "" && TempData.questionsCount >= 1)
                navigateToEditingQuestions()


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

fun Context.showYesNoDialog(
    functionToPerform: () -> Unit,
    functionIfCancel: () -> Unit,
    dialogTitle: String,
    dialogMessage: String
) {
    val dialogBuilder = AlertDialog.Builder(this).create()
    val dialogView = with(LayoutInflater.from(this)) {
        inflate(
            R.layout.yes_no_dialog,
            null
        )
    }
    dialogView.findViewById<AppCompatTextView>(R.id.dialogTitle).text = dialogTitle
    dialogView.findViewById<AppCompatTextView>(R.id.dialogMessageText).text = dialogMessage

    dialogView.findViewById<AppCompatButton>(R.id.dialogCancelButton).setOnClickListener {
        functionIfCancel()
        dialogBuilder.dismiss()
    }

    dialogView.findViewById<AppCompatButton>(R.id.dialogConfirmButton).setOnClickListener {
        functionToPerform()
        dialogBuilder.dismiss()
    }

    with(dialogBuilder) {
        setView(dialogView)
        show()
    }

}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun LinkedHashMap<String, MultipleChoiceQuestion>.allQuestionsOK(): Boolean {
    var isOK = false
    this.forEach {
        isOK = it.value.first.isNotEmpty() && it.value.second.isNotEmpty() &&
                it.value.third.isNotEmpty() && it.value.fourth.isNotEmpty() &&
                it.value.question.isNotEmpty()
    }
    return isOK
}

fun LinkedHashMap<String, TrueFalseQuestion>.areQuestionsOK(): Boolean {
    var isOK = false
    this.forEach {
        isOK = it.value.question.isNotEmpty()
    }
    return isOK
}