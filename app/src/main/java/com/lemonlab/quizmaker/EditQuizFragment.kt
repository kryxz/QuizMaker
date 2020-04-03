package com.lemonlab.quizmaker

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.items.MulEdit
import com.lemonlab.quizmaker.items.TFEdit
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_edit_quiz.*


class EditQuizFragment : Fragment() {

    private lateinit var vm: QuizzesVM

    private lateinit var theQuiz: Quizzer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.editQuiz)
            showEditDialog()

        return super.onOptionsItemSelected(item)
    }

    private fun showEditDialog() {
        val dialogBuilder = AlertDialog.Builder(context).create()

        val dialogView = with(LayoutInflater.from(context)) {
            inflate(R.layout.edit_quiz_dialog, null)
        }

        val args = EditQuizFragmentArgs.fromBundle(arguments!!)
        val id = args.quizID
        val code = args.classCode

        with(dialogView) {
            val titleText = findViewById<TextInputEditText>(R.id.quizTitle)
            titleText.setText(theQuiz.getTitle())

            val spinner = findViewById<AppCompatSpinner>(R.id.questionsSpinner)
            fun spinnerSetUp() {
                ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    IntArray(theQuiz.getSize()) { it + 1 }.toList()
                )
                    .also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
            }

            val questionText = findViewById<AppCompatTextView>(R.id.questionText)

            questionText.text = getString(
                R.string.questionText,
                theQuiz.getQuestion(1)
            )

            val deleteQuestion = findViewById<AppCompatButton>(R.id.deleteThisQuestion)
            val deleteQuiz = findViewById<AppCompatButton>(R.id.deleteQuiz)

            val confirm = findViewById<AppCompatButton>(R.id.dialogConfirmButton)
            val cancel = findViewById<AppCompatButton>(R.id.dialogCancelButton)

            fun deleteQuizNow() {
                context.showYesNoDialog(
                    {
                        vm.delQuiz(code, id)
                        dialogBuilder.dismiss()
                        view!!.findNavController().navigateUp()

                    }, {},
                    getString(R.string.deleteQuiz),
                    getString(R.string.deleteQuizConfirm)
                )
            }

            fun deleteQuestion() {
                if (theQuiz.getSize() == 1) {
                    deleteQuizNow()
                    return
                }
                theQuiz.deleteQuestion(spinner.selectedItemPosition + 1)
                spinnerSetUp()
                setUp(quiz = theQuiz, code = code)
            }
            listOf(cancel, confirm, deleteQuestion, deleteQuiz).forEach {
                it.setOnClickListener { button ->
                    when (button) {
                        cancel -> dialogBuilder.dismiss()
                        confirm -> {
                            if (titleText.text.isNullOrEmpty()) return@setOnClickListener
                            val title = titleText.text.toString()
                            theQuiz.setTitle(title)
                            dialogBuilder.dismiss()
                        }

                        deleteQuiz -> {
                            deleteQuizNow()
                        }

                        deleteQuestion -> {
                            context.showYesNoDialog(
                                {
                                    deleteQuestion()

                                }, {},
                                getString(R.string.DeleteCurrentQuestion),
                                getString(R.string.thisWillDeleteCurrentQuestion)
                            )
                        }

                    }
                }
            }



            spinnerSetUp()
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    questionText.text = getString(
                        R.string.questionText,
                        theQuiz.getQuestion(position + 1)
                    )
                }
            }


        }
        with(dialogBuilder) {
            setView(dialogView)
            show()
        }


    }

    private fun init() {
        val args = EditQuizFragmentArgs.fromBundle(arguments!!)
        val id = args.quizID
        val code = args.classCode

        editQuestionsProgressBar.visibility = View.VISIBLE
        vm.getQuiz(code, id).observe(viewLifecycleOwner, Observer {
            if (it != null)
                setUp(it, code)
        })

    }

    private fun setUp(quiz: Quizzer, code: String) {
        theQuiz = quiz
        editQuestionsProgressBar.visibility = View.GONE
        val adapter = GroupAdapter<ViewHolder>()


        if (quiz is MultipleChoiceQuiz) {
            val questions = quiz.questions!!
            fun update() {
                adapter.clear()
                for (item in questions)
                    adapter.add(MulEdit(item.value, item.key, questions, ::update))
            }

            listOf(addQuestionTop, addQuestion).forEach {
                it.setOnClickListener {
                    multiChoiceDialog(::update, quiz)
                }
            }

            updateQuiz.setOnClickListener {
                quiz.questions = questions
                vm.updateQuiz(code, quiz.getID(), quiz)

                Toast.makeText(
                    context!!, getString(R.string.updateQuizDone),
                    Toast.LENGTH_SHORT
                ).show()

                it.findNavController().navigateUp()
                update()
            }
            update()

        } else if (quiz is TrueFalseQuiz) {

            val questions = quiz.questions!!
            fun update() {
                adapter.clear()
                for (item in questions)
                    adapter.add(TFEdit(item.value, item.key, questions, ::update))
            }
            listOf(addQuestionTop, addQuestion).forEach {
                it.setOnClickListener {
                    trueFalseDialog(::update, quiz)
                }
            }
            updateQuiz.setOnClickListener {
                quiz.questions = questions
                vm.updateQuiz(code, quiz.getID(), quiz)
                Toast.makeText(
                    context!!, getString(R.string.updateQuizDone),
                    Toast.LENGTH_SHORT
                ).show()

                it.findNavController().navigateUp()
                update()
            }
            update()
        }

        editQuestionsRV.adapter = adapter


    }

    private fun trueFalseDialog(update: () -> Unit, quiz: TrueFalseQuiz) {
        val dialogBuilder = AlertDialog.Builder(context).create()

        val dialogView = with(LayoutInflater.from(context)) {
            inflate(R.layout.tf_edit_dialog, null)
        }

        with(dialogView) {
            val qText = findViewById<TextInputEditText>(R.id.questionText)

            val answerBox = findViewById<AppCompatCheckBox>(R.id.answerCheckBox)

            val confirm = findViewById<AppCompatButton>(R.id.confirmEditQuestion)
            val cancel = findViewById<AppCompatButton>(R.id.cancelEditQuestion)

            cancel.setOnClickListener {
                dialogBuilder.dismiss()
            }

            confirm.setOnClickListener {
                if (qText.text.isNullOrEmpty()) return@setOnClickListener
                val answer = answerBox.isChecked
                val text = qText.text.toString()
                val question = TrueFalseQuestion(text, answer)

                val key = (quiz.questions?.size?.inc() ?: 0).toString()
                quiz.questions!![key] = question
                quiz.quiz!!.questionsCount++
                dialogBuilder.dismiss()
                update()
            }
            with(dialogBuilder) {
                setView(dialogView)
                show()
            }

        }


    }

    private fun multiChoiceDialog(update: () -> Unit, quiz: MultipleChoiceQuiz) {
        val dialogBuilder = AlertDialog.Builder(context).create()

        val dialogView = with(LayoutInflater.from(context)) {
            inflate(R.layout.mul_edit_dialog, null)
        }

        with(dialogView) {
            val qText = findViewById<TextInputEditText>(R.id.questionText)
            val choiceA = findViewById<AppCompatEditText>(R.id.firstChoice)
            val choiceB = findViewById<AppCompatEditText>(R.id.secondChoice)
            val choiceC = findViewById<AppCompatEditText>(R.id.thirdChoice)
            val choiceD = findViewById<AppCompatEditText>(R.id.fourthChoice)

            val answerText = findViewById<AppCompatEditText>(R.id.answerText)

            val confirm = findViewById<AppCompatButton>(R.id.confirmEditQuestion)
            val cancel = findViewById<AppCompatButton>(R.id.cancelEditQuestion)


            cancel.setOnClickListener {
                dialogBuilder.dismiss()
            }

            confirm.setOnClickListener {
                if (qText.text.isNullOrEmpty()
                    || choiceA.text.isNullOrEmpty()
                    || choiceB.text.isNullOrEmpty()
                    || choiceC.text.isNullOrEmpty()
                    || choiceD.text.isNullOrEmpty()
                    || answerText.text.isNullOrEmpty()
                )

                    return@setOnClickListener

                val answersAlphabet = context.resources.getStringArray(R.array.chars)
                val enAnswers = context.resources.getStringArray(R.array.enChars)

                val answer = when (val t = answerText.text.toString().toLowerCase()) {
                    in answersAlphabet -> answersAlphabet.indexOf(t) // Alef, Baa...
                    in enAnswers -> enAnswers.indexOf(t) // a, b, c, d
                    else -> (t.toIntOrNull()?.dec()) ?: -1
                }


                if (answer > 3 || answer < 0) {
                    answerText.error = getString(R.string.wrongFormat)
                    return@setOnClickListener
                }

                val newQuestion = MultipleChoiceQuestion(
                    qText.text.toString(),
                    choiceA.text.toString(),
                    choiceB.text.toString(),
                    choiceC.text.toString(),
                    choiceD.text.toString(),
                    answer
                )
                val key = (quiz.questions?.size?.inc() ?: 0).toString()
                quiz.questions!![key] = newQuestion
                quiz.quiz!!.questionsCount++
                dialogBuilder.dismiss()
                update()
            }

        }
        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

}
