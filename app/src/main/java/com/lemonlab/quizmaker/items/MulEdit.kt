package com.lemonlab.quizmaker.items

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.data.MultipleChoiceQuestion
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.mul_review.view.*


class MulEdit(
    private val q: MultipleChoiceQuestion,
    private val key: String,
    private val questions: HashMap<String, MultipleChoiceQuestion>,
    private val update: () -> Unit
) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val context = view.context

        MulReview.initView(view, q)

        with(view.editQuestion) {
            visibility = View.VISIBLE
            setOnClickListener {
                edit(context)
            }
        }

    }


    override fun getLayout() = R.layout.mul_review

    private fun edit(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context).create()

        val dialogView = with(LayoutInflater.from(context)) {
            inflate(R.layout.mul_edit_dialog, null)
        }

        with(dialogView) {
            val qText = findViewById<TextInputEditText>(R.id.questionText)
            qText.setText(q.question)

            val choiceA = findViewById<AppCompatEditText>(R.id.firstChoice)
            choiceA.setText(q.first)
            val choiceB = findViewById<AppCompatEditText>(R.id.secondChoice)
            choiceB.setText(q.second)
            val choiceC = findViewById<AppCompatEditText>(R.id.thirdChoice)
            choiceC.setText(q.third)
            val choiceD = findViewById<AppCompatEditText>(R.id.fourthChoice)
            choiceD.setText(q.fourth)

            val answerText = findViewById<AppCompatEditText>(R.id.answerText)
            answerText.setText((q.correctAnswer + 1).toString())

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
                ) {
                    return@setOnClickListener
                }


                val answersAlphabet = context.resources.getStringArray(R.array.chars)
                val enAnswers = context.resources.getStringArray(R.array.enChars)

                val answer = when (val t = answerText.text.toString().toLowerCase()) {
                    in answersAlphabet -> answersAlphabet.indexOf(t) // Alef, Baa...
                    in enAnswers -> enAnswers.indexOf(t) // a, b, c, d
                    else -> (t.toIntOrNull()?.dec()) ?: -1
                }

                if (answer > 3 || answer < 0) {
                    answerText.error = context.getString(R.string.wrongFormat)
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
                questions[key] = newQuestion
                dialogBuilder.dismiss()
                update()
            }
            with(dialogBuilder) {
                setView(dialogView)
                show()
            }


        }


    }
}
