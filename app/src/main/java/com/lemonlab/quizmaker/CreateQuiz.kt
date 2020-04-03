package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_create_quiz.*

class CreateQuiz : Fragment() {

    private lateinit var viewModel: QuestionsVM


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).questionsVM
        init()
    }

    private fun textWatchers() {
        // Adds on textChanged listeners to all edit texts, and handles errors and assignments.
        // Adds an adapter to the spinner, too.

        quizQuestionsCount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.toString().toInt() > 30)
                    quizQuestionsCount.error = getString(R.string.cannotBeGreaterThan30)
                else
                    viewModel.setSize(
                        if (s.toString().isNotEmpty())
                            s.toString().toInt()
                        else
                            0
                    )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        quizPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank())
                    quizPasswordEditText.error = getString(R.string.cannotBeEmpty)
                else
                    viewModel.setPin(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        quizTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank())
                    quizTitleEditText.error = getString(R.string.cannotBeEmpty)
                else
                    viewModel.setTitle(s.toString())
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

        questionsTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.setQuizType(
                        if (questionsTypeSpinner.selectedItemPosition == 0)
                            QuizType.MultipleChoice
                        else
                            QuizType.TrueFalse
                    )

                }
            }
    }

    private fun getPreviousData() {
        isPasswordProtected.isChecked = viewModel.hasPin()
        quizTitleEditText.setText(viewModel.getTitle())
        quizPasswordEditText.setText(viewModel.getPin())
        quizQuestionsCount.setText(viewModel.getSize().toString())
        val type = viewModel.getQuizType()

        questionsTypeSpinner.setSelection(if (type == QuizType.TrueFalse) 1 else 0)
    }


    private fun init() {
        getPreviousData()
        textWatchers()

        // to Remove saved questions
        fun deleteCached() {
            viewModel.removeAll()
            Navigation.findNavController(view!!).navigate(R.id.viewEditQuestions)
        }

        fun navigateToEditingQuestions() {
            val code = CreateQuizArgs.fromBundle(arguments!!).classCode
            val direction = CreateQuizDirections.reviewQuiz()
            direction.classCode = code
            view!!.findNavController().navigate(direction)
        }

        isPasswordProtected.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                quizPasswordEditText.visibility = View.VISIBLE
            else
                quizPasswordEditText.visibility = View.GONE

            viewModel.setHasPin(isChecked)


        }

        writeQuestionsButton.setOnClickListener {

            val quizType = if (questionsTypeSpinner.selectedItemPosition == 0)
                QuizType.MultipleChoice
            else
                QuizType.TrueFalse

            viewModel.setQuizType(quizType)
            viewModel.setTitle(quizTitleEditText.text.toString())
            viewModel.setPin(quizPasswordEditText.text.toString())
            viewModel.setSize(quizQuestionsCount.text.toString().toIntOrNull() ?: 1)

            if (viewModel.getMultiChoice().isNotEmpty()
                && viewModel.getQuizType() == QuizType.MultipleChoice ||
                viewModel.getTrueFalse()
                    .isNotEmpty() && viewModel.getQuizType() == QuizType.TrueFalse

            ) {
                context!!.showYesNoDialog(
                    ::deleteCached,
                    ::navigateToEditingQuestions,
                    getString(R.string.existingQuestions),
                    getString(R.string.existingQuestionsMessage)
                )

            } else if (viewModel.getTitle() != "")
                navigateToEditingQuestions()


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.hideKeypad()
    }


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

    if (dialogTitle == getString(R.string.changeTheme))
        dialogBuilder.setOnDismissListener { functionIfCancel() }

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

fun String.removedWhitespace(): String {
    var isFirstSpace = false
    var result = ""
    for (char in this) {
        if (char != ' ' && char != '\n') {
            isFirstSpace = true
            result += char
        } else if (isFirstSpace) {
            result += " "
            isFirstSpace = false
        }
    }
    return result
}