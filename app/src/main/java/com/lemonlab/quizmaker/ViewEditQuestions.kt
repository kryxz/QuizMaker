package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.items.MulReview
import com.lemonlab.quizmaker.items.TFReview
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.questions_fill_form.*
import java.util.*
import kotlin.collections.HashMap


class ViewEditQuestions : Fragment() {
    private var position = 1
    private var quizID = "empty"

    private lateinit var viewModel: QuestionsVM
    private lateinit var vm: QuizzesVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        quizID = ViewEditQuestionsArgs.fromBundle(arguments!!).quizID
        setHasOptionsMenu(quizID == "empty")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.questions_fill_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).questionsVM
        vm = (activity as MainActivity).vm
        init()

    }

    private fun init() {
        if (quizID != "empty") {
            viewQuizAnswers()
            return
        }
        if (viewModel.getQuizType() == QuizType.TrueFalse)
            trueFalse()
        else if (viewModel.getQuizType() == QuizType.MultipleChoice)
            multipleChoice()

        updateTitle()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.hideKeypad()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setTextsMultipleChoice() {
        val q = viewModel.getMultiChoiceQuestion(position)

        questionsTextEditText.setText(q.question)
        firstChoice.setText(q.first)
        secondChoice.setText(q.second)
        thirdChoice.setText(q.third)
        fourthChoice.setText(q.fourth)
        correctAnswerSpinner.setSelection(q.correctAnswer)

    }

    private fun setTextTrueFalse() {
        val q = viewModel.getTFQuestion(position)

        questionsTextEditText.setText(q.question)
        isStatementTrueCheckBox.isChecked = q.answer


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.editQuiz && questionsRecyclerView.layoutManager == null)
            showEditDialog()
        else if (item.itemId == R.id.editQuiz)
            showToast(context!!, getString(R.string.cannotEdit))
        return super.onOptionsItemSelected(item)
    }


    private fun showEditDialog() {
        val dialogBuilder = AlertDialog.Builder(context!!).create()
        val dialogView = with(layoutInflater) {
            inflate(
                R.layout.edit_quiz_properties_dialog,
                null
            )
        }

        fun deleteCurrentQuestion() {
            val quizType = viewModel.getQuizType()
            if (quizType == QuizType.MultipleChoice)
                viewModel.setMultiChoiceQuestion(position, MultipleChoiceQuestion())
            else
                viewModel.setTFQuestion(position, TrueFalseQuestion())

            viewModel.setSize(viewModel.getSize().dec())
            position = viewModel.getSize()
            updateTitle()
            dialogBuilder.dismiss()
        }


        val questionsCount =
            dialogView.findViewById<TextInputEditText>(R.id.dialogPropertiesQuizQuestionsCount)
        val quizTitle =
            dialogView.findViewById<TextInputEditText>(R.id.dialogPropertiesQuizTitleEditText)

        fun resizeQuiz() {
            val quizQuestionsCount =
                questionsCount.text.toString().toIntOrNull() ?: viewModel.getSize()

            viewModel.setSize(quizQuestionsCount)
            position = 1
            updateTitle()
            dialogBuilder.dismiss()
            setTextsMultipleChoice()
            setTextTrueFalse()
        }


        questionsCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.toString().toInt() > 30)
                    questionsCount.error = getString(R.string.cannotBeGreaterThan30)
            }
        })

        dialogView.findViewById<AppCompatButton>(R.id.dialogDeleteCurrentQuestionButton)
            .setOnClickListener {

                if (viewModel.getSize() > 1)
                    context!!.showYesNoDialog(
                        ::deleteCurrentQuestion,
                        fun() {},
                        getString(R.string.DeleteCurrentQuestion),
                        getString(R.string.thisWillDeleteCurrentQuestion)
                    )
            }

        dialogView.findViewById<AppCompatButton>(R.id.dialogPropertiesConfirmButton)
            .setOnClickListener {

                //Updates title
                val title = quizTitle.text.toString()
                if (title.length > 1)
                    viewModel.setTitle(title)


                //Updates questions count
                val quizQuestionsCount =
                    questionsCount.text.toString().toIntOrNull() ?: viewModel.getSize()
                if (quizQuestionsCount != viewModel.getSize() &&
                    quizQuestionsCount <= 30
                )
                    context!!.showYesNoDialog(
                        ::resizeQuiz, {},
                        getString(R.string.resizeQuiz),
                        getString(R.string.thisWillDeleteRemainingQuestions)
                    )

                dialogBuilder.dismiss()
            }
        dialogView.findViewById<AppCompatButton>(R.id.dialogPropertiesCancelButton)
            .setOnClickListener {
                dialogBuilder.dismiss()
            }

        questionsCount.hint = viewModel.getSize().toString()
        quizTitle.hint = viewModel.getTitle()


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }

    private fun viewQuizAnswers() {
        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.viewAnswers)
        editQuestionsLayout.visibility = View.GONE


        questionsRecyclerView.layoutManager = LinearLayoutManager(context!!)
        questionsRecyclerView.visibility = View.VISIBLE

        val adapter = GroupAdapter<ViewHolder>()

        fun mulChoice(quiz: MultipleChoiceQuiz) {
            adapter.clear()
            val items = quiz.questions
            for (item in items!!.values)
                adapter.add(MulReview(item))

            questionsRecyclerView.adapter = adapter

        }

        fun tf(quiz: TrueFalseQuiz) {
            adapter.clear()
            val items = quiz.questions
            for (item in items!!.values)
                adapter.add(TFReview(item))
            questionsRecyclerView.adapter = adapter


        }


        vm.getQuiz("empty", quizID).observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { quiz ->

                if (quiz is MultipleChoiceQuiz) mulChoice(quiz)
                else if (quiz is TrueFalseQuiz) tf(quiz)

                (activity as AppCompatActivity).supportActionBar.also {
                    it!!.setHomeAsUpIndicator(R.drawable.ic_cancel)
                    it.title = quiz?.getTitle()
                }
            })


    }

    private fun reviewQuiz() {

        editQuestionsLayout.visibility = View.GONE

        val adapter = GroupAdapter<ViewHolder>()
        bottomLayoutViewEdit.visibility = View.VISIBLE
        with(questionsRecyclerView) {
            visibility = View.VISIBLE
            adapter.clear()
            val type = viewModel.getQuizType()
            layoutManager = LinearLayoutManager(context!!)

            if (type == QuizType.TrueFalse) {
                val items = viewModel.getTrueFalse()
                for (item in items)
                    adapter.add(TFReview(item.value))
            } else {
                val items = viewModel.getMultiChoice()
                for (item in items)
                    adapter.add(MulReview(item.value))
            }

            this.adapter = adapter

            (activity as AppCompatActivity).supportActionBar!!
                .setHomeAsUpIndicator(R.drawable.ic_cancel)

        }

        publishQuizButton.setOnClickListener {
            publishQuiz()
        }

        backToEditingButton.setOnClickListener {
            parentFragmentManager.beginTransaction().detach(this)
                .attach(this)
                .commit()
            (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow)
        }

        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.reviewQuestions)
    }

    private fun publishQuiz() {

        fun publishNow() {
            val code = ViewEditQuestionsArgs.fromBundle(arguments!!).classCode
            val isClass = code != "empty"
            val userName = vm.getName()

            val id = StringBuilder()
            if (isClass)
                id.append("class")

            id.append(UUID.randomUUID().toString().substring(0, 10))
            id.append(userName)

            val quizID = id.toString()

            val quizData = Quiz(
                viewModel.getTitle(), viewModel.hasPin(),

                viewModel.getSize(),

                viewModel.getPin(), viewModel.getQuizType(), userName,

                quizID, 0f, 0, Calendar.getInstance().timeInMillis
            )

            val quiz = if (quizData.quizType == QuizType.MultipleChoice)
                MultipleChoiceQuiz(quizData, viewModel.getMultiChoice())
            else
                TrueFalseQuiz(quizData, viewModel.getTrueFalse())

            HashMap<String, Quizzer>().apply {
                this["quiz"] = quiz
                vm.sendQuiz(quizID, this, code)
                viewModel.removeAll()
                activity!!.hideKeypad()
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.classFragment, false).build()
                if (isClass)
                    view!!.findNavController().navigate(
                        ViewEditQuestionsDirections.goToClass(code), navOptions
                    )
                else
                    view!!.findNavController().navigate(R.id.mainFragment)
            }

        }
        context!!.showYesNoDialog(
            ::publishNow, {},
            getString(R.string.publishQuizText),
            getString(R.string.publishQuizWithTitle, viewModel.getTitle())
        )

    }

    private fun multipleChoice() {

        fun addQuestion(key: Int) {
            val question = MultipleChoiceQuestion(
                questionsTextEditText.text.toString().removedWhitespace(),
                firstChoice.text.toString().removedWhitespace(),
                secondChoice.text.toString().removedWhitespace(),
                thirdChoice.text.toString().removedWhitespace(),
                fourthChoice.text.toString().removedWhitespace(),
                correctAnswerSpinner.selectedItemPosition
            )
            viewModel.setMultiChoiceQuestion(key, question)

        }

        isStatementTrueCheckBox.visibility = View.INVISIBLE

        fun allFieldsOK(): Boolean {
            return (questionsTextEditText.text!!.isNotEmpty() && firstChoice.text!!.isNotEmpty() && secondChoice.text!!.isNotEmpty()
                    && thirdChoice.text!!.isNotEmpty() && fourthChoice.text!!.isNotEmpty())
        }

        if (viewModel.getMultiChoice().isNotEmpty())
            setTextsMultipleChoice()

        fun canReviewQuiz() {
            with(reviewQuizButton) {
                this.setOnClickListener {
                    if (position == viewModel.getSize() && allFieldsOK()) {
                        addQuestion(position)
                    }
                    if (viewModel.getMultiChoice().size == viewModel.getSize()
                        && viewModel.getMultiChoice().allQuestionsOK()
                    )
                        reviewQuiz()
                    else
                        showToast(context!!, getString(R.string.completeAllQuestions))
                }
            }
        }

        val listOfEditTexts = listOf<AppCompatEditText>(
            questionsTextEditText,
            firstChoice, secondChoice, thirdChoice, fourthChoice
        )
        ArrayAdapter.createFromResource(
            context!!,
            R.array.multipleChoices,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            correctAnswerSpinner.adapter = adapter
        }

        fun previous() {
            addQuestion(position)
            listOfEditTexts.forEach {
                it.text!!.clear()
            }
            correctAnswerSpinner.setSelection(0)
            position = position.dec()

            if (viewModel.getMultiChoice().isNotEmpty())
                setTextsMultipleChoice()

            updateTitle()

        }

        fun update() {
            if (position == viewModel.getSize() && allFieldsOK())
                addQuestion(position)
            else if (position < viewModel.getSize()) {
                position = position.inc()
                if (allFieldsOK())
                    addQuestion((position - 1))
            }
            listOfEditTexts.forEach {
                it.text!!.clear()
            }
            correctAnswerSpinner.setSelection(0)

            setTextsMultipleChoice()

            updateTitle()
        }


        canReviewQuiz()
        buttonsClickHandle(::update, ::previous)
        setTextsMultipleChoice()
    }

    private fun trueFalse() {

        multipleChoiceLayout.visibility = View.GONE

        fun addQuestion(key: Int) {
            val text = questionsTextEditText.text.toString().removedWhitespace()
            val answer = isStatementTrueCheckBox.isChecked

            val question = TrueFalseQuestion(
                text,
                answer
            )
            if (text.isNotEmpty())
                viewModel.setTFQuestion(key, question)

        }

        fun clearFields() {
            questionsTextEditText.text!!.clear()
            isStatementTrueCheckBox.isChecked = false
        }

        fun update() {
            if (position == viewModel.getSize())
                return

            addQuestion(position)
            position = position.inc()
            updateTitle()
            clearFields()
            setTextTrueFalse()
        }

        fun previous() {
            if (position == 1)
                return
            addQuestion(position)
            position = position.dec()
            updateTitle()
            clearFields()
            setTextTrueFalse()
        }

        fun canReviewQuiz() {
            with(reviewQuizButton) {
                setOnClickListener {
                    if (position == viewModel.getSize()
                        && questionsTextEditText.text!!.isNotBlank()
                    )
                        addQuestion(position)
                    if (viewModel.getTrueFalse().size >= viewModel.getSize()
                        && viewModel.getTrueFalse().areQuestionsOK()
                    )
                        reviewQuiz()
                    else
                        showToast(context!!, getString(R.string.completeAllQuestions))
                }
            }
        }

        isStatementTrueCheckBox.setOnCheckedChangeListener { _, _ ->
            addQuestion(position)
        }
        canReviewQuiz()
        buttonsClickHandle(::update, ::previous)
        setTextTrueFalse()
    }

    private fun updateTitle() {
        (activity as AppCompatActivity).supportActionBar!!.title =
            "$position/" + (viewModel.getSize()).toString()
    }

    private fun buttonsClickHandle(updateNext: () -> Unit, updatePrevious: () -> Unit) {

        mutableListOf<AppCompatButton>(previousQuestionButton, nextQuestionButton).forEach {
            it.setOnClickListener { button ->
                when (button) {
                    nextQuestionButton -> updateNext()
                    previousQuestionButton -> if (position > 1) updatePrevious()
                }
            }
        }
    }

}


