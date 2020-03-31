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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.lemonlab.quizmaker.adapters.QuestionsAdapter
import kotlinx.android.synthetic.main.questions_fill_form.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


class ViewEditQuestions : Fragment() {
    private var position = 1
    private lateinit var multipleChoiceQuestions: LinkedHashMap<String, MultipleChoiceQuestion>
    private lateinit var trueFalseQuestions: LinkedHashMap<String, TrueFalseQuestion>
    private var quizID = "empty"
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
        decideWhatToDo()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        activity!!.hideKeypad()
        super.onDestroyView()
    }

    private fun decideWhatToDo() {
        if (quizID == "empty")
            decideQuizType()
        else
            viewQuizAnswers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setTextsMultipleChoice() {
        if (TempData.quizType != QuizType.MultipleChoice)
            return
        if (multipleChoiceQuestions[position.toString()] != null) {
            questionsTextEditText.setText(multipleChoiceQuestions[position.toString()]!!.question)
            firstChoice.setText(multipleChoiceQuestions[position.toString()]!!.first)
            secondChoice.setText(multipleChoiceQuestions[position.toString()]!!.second)
            thirdChoice.setText(multipleChoiceQuestions[position.toString()]!!.third)
            fourthChoice.setText(multipleChoiceQuestions[position.toString()]!!.fourth)
            correctAnswerSpinner.setSelection(multipleChoiceQuestions[position.toString()]!!.correctAnswer)
        }

    }

    private fun setTextTrueFalse() {
        if (TempData.quizType != QuizType.TrueFalse)
            return
        if (trueFalseQuestions[position.toString()] != null) {
            questionsTextEditText.setText(trueFalseQuestions[position.toString()]!!.question)
            isStatementTrueCheckBox.isChecked = trueFalseQuestions[position.toString()]!!.answer
        }

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
            if (TempData.quizType == QuizType.MultipleChoice) {
                multipleChoiceQuestions[position.toString()] = MultipleChoiceQuestion()
                if (multipleChoiceQuestions[position.toString()] != null)
                    setTextsMultipleChoice()
            } else if (TempData.quizType == QuizType.TrueFalse) {
                trueFalseQuestions[position.toString()] = TrueFalseQuestion()
                if (trueFalseQuestions[position.toString()] != null)
                    setTextTrueFalse()
            }
            TempData.questionsCount = TempData.questionsCount.dec()
            position = TempData.questionsCount
            updateTitle()
            dialogBuilder.dismiss()
        }

        val questionsCount = dialogView.findViewById<TextInputEditText>(R.id.dialogPropertiesQuizQuestionsCount)
        val quizTitle = dialogView.findViewById<TextInputEditText>(R.id.dialogPropertiesQuizTitleEditText)

        fun resizeQuiz() {
            val quizQuestionsCount = questionsCount.text.toString().toIntOrNull() ?: TempData.questionsCount
            TempData.questionsCount = quizQuestionsCount
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

        dialogView.findViewById<AppCompatButton>(R.id.dialogDeleteCurrentQuestionButton).setOnClickListener {

            if (TempData.questionsCount > 1)
                context!!.showYesNoDialog(
                    ::deleteCurrentQuestion,
                    fun() {},
                    getString(R.string.DeleteCurrentQuestion),
                    getString(R.string.thisWillDeleteCurrentQuestion)
                )
        }

        dialogView.findViewById<AppCompatButton>(R.id.dialogPropertiesConfirmButton).setOnClickListener {

            //Updates title
            TempData.quizTitle =
                if (quizTitle.text.toString().length > 1)
                    quizTitle.text.toString()
                else
                    TempData.quizTitle

            //Updates questions count
            val quizQuestionsCount = questionsCount.text.toString().toIntOrNull() ?: TempData.questionsCount
            if (quizQuestionsCount != TempData.questionsCount &&
                quizQuestionsCount <= 30
            )
                context!!.showYesNoDialog(
                    ::resizeQuiz, {},
                    getString(R.string.resizeQuiz),
                    getString(R.string.thisWillDeleteRemainingQuestions)
                )

            dialogBuilder.dismiss()
        }
        dialogView.findViewById<AppCompatButton>(R.id.dialogPropertiesCancelButton).setOnClickListener {
            dialogBuilder.dismiss()
        }

        questionsCount.hint = TempData.questionsCount.toString()
        quizTitle.hint = TempData.quizTitle


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }

    private fun viewQuizAnswers() {
        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.viewAnswers)
        editQuestionsLayout.visibility = View.GONE
        val quizRef = FirebaseFirestore.getInstance().collection("Quizzes").document(quizID)
        quizRef.get().addOnSuccessListener {
            val quiz = it.get("quiz.quiz", Quiz::class.java)!!
            if (quiz.quizType == QuizType.MultipleChoice && view != null)
                with(questionsRecyclerView) {
                    visibility = View.VISIBLE
                    val quizQuestions = LinkedHashMap(it.get("quiz", MultipleChoiceQuiz::class.java)!!.questions!!)
                    TempData.quizType = quiz.quizType
                    TempData.questionsCount = quizQuestions.size
                    layoutManager = null
                    adapter = null
                    onFlingListener = null
                    (activity as AppCompatActivity).supportActionBar!!.title = quiz.quizTitle

                    layoutManager = LinearLayoutManager(context!!)
                    adapter = QuestionsAdapter(context!!, quizQuestions, null)
                    (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_cancel)

                }
            else if (view != null)
                with(questionsRecyclerView) {
                    visibility = View.VISIBLE
                    TempData.quizType = quiz.quizType
                    val quizQuestions = LinkedHashMap(it.get("quiz", TrueFalseQuiz::class.java)!!.questions)
                    TempData.questionsCount = quizQuestions.size
                    layoutManager = null
                    adapter = null
                    onFlingListener = null
                    (activity as AppCompatActivity).supportActionBar!!.title = quiz.quizTitle
                    layoutManager = LinearLayoutManager(context!!)
                    adapter = QuestionsAdapter(context!!, null, quizQuestions)
                    (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_cancel)

                }
        }
    }

    private fun reviewQuiz() {

        editQuestionsLayout.visibility = View.GONE

        bottomLayoutViewEdit.visibility = View.VISIBLE
        if (TempData.quizType == QuizType.MultipleChoice)
            with(questionsRecyclerView) {
                TempData.multiChoiceCachedQuestions = multipleChoiceQuestions
                layoutManager = null
                adapter = null
                onFlingListener = null

                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context!!)
                adapter = QuestionsAdapter(context!!, multipleChoiceQuestions, null)
                (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_cancel)
            }
        else
            with(questionsRecyclerView) {
                TempData.trueFalseCachedQuestions = trueFalseQuestions
                layoutManager = null
                adapter = null
                onFlingListener = null

                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context!!)
                adapter = QuestionsAdapter(context!!, null, trueFalseQuestions)
                (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_cancel)
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

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.reviewQuestions)
    }

    private fun publishQuiz() {
        fun publishNow() {
            val userName: String = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            val quizID =
                FirebaseAuth.getInstance().currentUser!!.displayName.toString() +
                        UUID.randomUUID().toString().substring(0, 10)
            val quizData = Quiz(
                TempData.quizTitle, TempData.isPasswordProtected,

                TempData.questionsCount,

                TempData.quizPin, TempData.quizType, userName,

                quizID, 0f, 0, Calendar.getInstance().timeInMillis
            )
            when (TempData.quizType) {
                QuizType.MultipleChoice -> {
                    val quiz: HashMap<String, MultipleChoiceQuiz> = HashMap()
                    quiz["quiz"] = MultipleChoiceQuiz(quizData, multipleChoiceQuestions)

                    FirebaseFirestore.getInstance().collection("Quizzes").document(quizID).set(quiz)
                        .addOnSuccessListener {
                            //resets all temp data to their default values.
                            TempData.resetData()
                            FirebaseMessaging.getInstance()
                                .subscribeToTopic(FirebaseAuth.getInstance().currentUser!!.displayName!!)
                            Navigation.findNavController(view!!).navigate(R.id.mainFragment)
                        }
                }
                QuizType.TrueFalse -> {
                    val quiz: HashMap<String, TrueFalseQuiz> = HashMap()
                    quiz["quiz"] = TrueFalseQuiz(quizData, trueFalseQuestions)

                    FirebaseFirestore.getInstance().collection("Quizzes").document(quizID).set(quiz)
                        .addOnSuccessListener {
                            //resets all temp data to their default values.
                            TempData.resetData()

                            Navigation.findNavController(view!!).navigate(R.id.mainFragment)
                        }
                }
            }

        }
        context!!.showYesNoDialog(
            ::publishNow, fun() {},
            getString(R.string.publishQuizText),
            getString(R.string.publishQuizWithTitle, TempData.quizTitle)
        )

    }

    private fun multipleChoice() {
        multipleChoiceQuestions = if (TempData.multiChoiceCachedQuestions != null)
            TempData.multiChoiceCachedQuestions!!
        else
            LinkedHashMap()

        fun addQuestion(key: String) {
            multipleChoiceQuestions[key] = MultipleChoiceQuestion(
                questionsTextEditText.text.toString().removedWhitespace(),
                firstChoice.text.toString().removedWhitespace(), secondChoice.text.toString().removedWhitespace(),
                thirdChoice.text.toString().removedWhitespace(), fourthChoice.text.toString().removedWhitespace(),
                correctAnswerSpinner.selectedItemPosition
            )
        }

        isStatementTrueCheckBox.visibility = View.INVISIBLE

        fun allFieldsOK(): Boolean {
            return (questionsTextEditText.text!!.isNotEmpty() && firstChoice.text!!.isNotEmpty() && secondChoice.text!!.isNotEmpty()
                    && thirdChoice.text!!.isNotEmpty() && fourthChoice.text!!.isNotEmpty())
        }

        if (multipleChoiceQuestions.isNotEmpty())
            setTextsMultipleChoice()

        fun canReviewQuiz() {
            with(reviewQuizButton) {
                this.setOnClickListener {
                    if (position == TempData.questionsCount && allFieldsOK()) {
                        addQuestion(position.toString())
                    }
                    if (multipleChoiceQuestions.size >= TempData.questionsCount && multipleChoiceQuestions.allQuestionsOK())
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
            addQuestion(position.toString())
            listOfEditTexts.forEach {
                it.text!!.clear()
            }
            correctAnswerSpinner.setSelection(0)
            position = position.dec()
            if (multipleChoiceQuestions.isNotEmpty() && multipleChoiceQuestions[position.toString()] != null)
                setTextsMultipleChoice()

            updateTitle()

        }

        fun update() {
            if (position == TempData.questionsCount && allFieldsOK())
                addQuestion(position.toString())
            else if (position < TempData.questionsCount) {
                position = position.inc()
                if (allFieldsOK())
                    addQuestion((position - 1).toString())
            }
            listOfEditTexts.forEach {
                it.text!!.clear()
            }
            correctAnswerSpinner.setSelection(0)
            if (multipleChoiceQuestions.isNotEmpty() && multipleChoiceQuestions[position.toString()] != null) {
                setTextsMultipleChoice()
            }

            updateTitle()
        }


        canReviewQuiz()
        buttonsClickHandle(::update, ::previous)

    }

    private fun trueFalse() {
        trueFalseQuestions = if (TempData.trueFalseCachedQuestions != null)
            TempData.trueFalseCachedQuestions!!
        else
            LinkedHashMap()


        multipleChoiceLayout.visibility = View.GONE
        fun addQuestion(key: String) {
            if (questionsTextEditText.text!!.isNotBlank()) {
                trueFalseQuestions[key] = TrueFalseQuestion(
                    questionsTextEditText.text.toString().removedWhitespace(),
                    isStatementTrueCheckBox.isChecked
                )
                TempData.trueFalseCachedQuestions = trueFalseQuestions
            }
        }

        fun clearFields() {
            questionsTextEditText.text!!.clear()
            isStatementTrueCheckBox.isChecked = false
        }

        fun update() {
            if (position == TempData.questionsCount)
                return
            addQuestion(position.toString())
            position = position.inc()
            updateTitle()
            clearFields()
            setTextTrueFalse()
        }

        fun previous() {
            if (position == 1)
                return
            addQuestion(position.toString())
            position = position.dec()
            updateTitle()
            clearFields()
            setTextTrueFalse()
        }

        fun canReviewQuiz() {
            with(reviewQuizButton) {
                this.setOnClickListener {
                    if (position == TempData.questionsCount && questionsTextEditText.text!!.isNotBlank()) {
                        addQuestion(position.toString())
                    }
                    if (trueFalseQuestions.size >= TempData.questionsCount && trueFalseQuestions.areQuestionsOK())
                        reviewQuiz()
                    else
                        showToast(context!!, getString(R.string.completeAllQuestions))
                }
            }
        }

        isStatementTrueCheckBox.setOnCheckedChangeListener { _, _ ->
            addQuestion(position.toString())
        }
        canReviewQuiz()
        buttonsClickHandle(::update, ::previous)

    }

    private fun updateTitle() {
        (activity as AppCompatActivity).supportActionBar!!.title =
            "$position/" + (TempData.questionsCount).toString()
    }

    private fun buttonsClickHandle(updateNext: () -> Unit, updatePrevious: () -> Unit) {
        mutableListOf<AppCompatButton>(previousQuestionButton, nextQuestionButton).forEach {
            it.setOnClickListener { button ->
                when (button) {
                    nextQuestionButton
                    -> {
                        updateNext()
                    }
                    previousQuestionButton
                    -> {
                        if (position > 1)
                            updatePrevious()
                    }
                }
            }
        }
    }


    private fun decideQuizType() {
        if (TempData.quizType == QuizType.TrueFalse)
            trueFalse()
        else if (TempData.quizType == QuizType.MultipleChoice)
            multipleChoice()
        updateTitle()
    }

}
