package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.questions_fill_form.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


class ViewEditQuestions : Fragment() {
    private var position = 1
    private lateinit var quizQuestions: LinkedHashMap<String, MultipleChoiceQuestion>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.questions_fill_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        quizQuestions = if (TempData.cachedQuestions != null)
            TempData.cachedQuestions!!
        else
            LinkedHashMap()

        decideQuizType()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.view_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setTextsMultipleChoice() {
        questionsTextEditText.setText(quizQuestions[position.toString()]!!.question)
        firstChoice.setText(quizQuestions[position.toString()]!!.first)
        secondChoice.setText(quizQuestions[position.toString()]!!.second)
        thirdChoice.setText(quizQuestions[position.toString()]!!.third)
        fourthChoice.setText(quizQuestions[position.toString()]!!.fourth)
        correctAnswerSpinner.setSelection(quizQuestions[position.toString()]!!.correctAnswer)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.editQuiz && questionsRecyclerView.layoutManager == null)
            showEditDialog()
        else if (item.itemId == R.id.editQuiz)
            Toast.makeText(context!!, "لا يمكنك التعديل من هنا", Toast.LENGTH_SHORT).show()
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
            quizQuestions[position.toString()] = MultipleChoiceQuestion()
            TempData.questionsCount = TempData.questionsCount.dec()
            position = TempData.questionsCount
            setTextsMultipleChoice()
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
            if (quizQuestions[position.toString()] != null)
                setTextsMultipleChoice()
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
                showYesNoDialog(
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
                showYesNoDialog(
                    ::resizeQuiz, fun() {},
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

    private fun reviewQuiz() {

        editQuestionsLayout.visibility = View.GONE

        bottomLayoutViewEdit.visibility = View.VISIBLE

        with(questionsRecyclerView) {
            layoutManager = null
            adapter = null
            onFlingListener = null

            visibility = View.VISIBLE
            layoutManager = ViewPagerLayoutManager(context!!, 0)
            adapter = QuestionsAdapter(context!!, quizQuestions)
        }

        publishQuizButton.setOnClickListener {
            publishQuiz()
        }
        backToEditingButton.setOnClickListener {
            fragmentManager!!.beginTransaction().detach(this)
                .attach(this)
                .commit()
        }

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.reviewQuestions)
    }

    private fun publishQuiz() {
        fun publishNow() {
            val userName: String = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            val userUID =
                FirebaseAuth.getInstance().currentUser!!.displayName.toString() + UUID.randomUUID().toString().substring(
                    0,
                    10
                )
            val quiz: HashMap<String, MultipleChoiceQuiz> = HashMap()
            quiz["quiz"] = MultipleChoiceQuiz(
                Quiz(
                    TempData.quizTitle, TempData.isPasswordProtected,

                    TempData.isOneTimeQuiz, TempData.questionsCount,

                    TempData.quizPin, TempData.quizType, userName,

                    userUID
                ),
                quizQuestions
            )

            FirebaseFirestore.getInstance().collection("Quizzes").document(userUID).set(quiz).addOnSuccessListener {
                //resets all temp data to their default values.
                TempData.resetData()

                Navigation.findNavController(view!!).navigate(R.id.mainFragment)
            }

        }
        showYesNoDialog(
            ::publishNow, fun() {},
            getString(R.string.publishQuizText),
            getString(R.string.publishQuizWithTitle, TempData.quizTitle)
        )

    }

    private fun multipleChoice() {

        fun addQuestion(key: String) {
            quizQuestions[key] = MultipleChoiceQuestion(
                questionsTextEditText.text.toString(),
                firstChoice.text.toString(), secondChoice.text.toString(),
                thirdChoice.text.toString(), fourthChoice.text.toString(),
                correctAnswerSpinner.selectedItemPosition
            )
        }

        isStatementTrueCheckBox.visibility = View.INVISIBLE

        fun allFieldsOK(): Boolean {
            return (questionsTextEditText.text!!.isNotEmpty() && firstChoice.text!!.isNotEmpty() && secondChoice.text!!.isNotEmpty()
                    && thirdChoice.text!!.isNotEmpty() && fourthChoice.text!!.isNotEmpty())
        }

        if (quizQuestions.isNotEmpty())
            setTextsMultipleChoice()

        fun canReviewQuiz() {
            with(reviewQuizButton) {
                this.setOnClickListener {
                    if (position == TempData.questionsCount && allFieldsOK()) {
                        addQuestion(position.toString())
                    }
                    if (quizQuestions.size >= TempData.questionsCount)
                        reviewQuiz()
                    else
                        Toast.makeText(context!!, getString(R.string.completeAllQuestions), Toast.LENGTH_SHORT).show()
                    TempData.cachedQuestions = quizQuestions
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
            if (quizQuestions.isNotEmpty() && quizQuestions[position.toString()] != null)
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
            if (quizQuestions.isNotEmpty() && quizQuestions[position.toString()] != null) {
                setTextsMultipleChoice()
            }

            updateTitle()
        }


        canReviewQuiz()
        buttonsClickHandle(::update, ::previous)

    }

    private fun trueFalse() {
        multipleChoiceLayout.visibility = View.GONE
        val sparseBooleanArrayTrueFalse = SparseBooleanArray(TempData.questionsCount)
        fun update() {
            sparseBooleanArrayTrueFalse.append(position, true)
            updateTitle()
        }

        fun previous() {
            updateTitle()
        }

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
