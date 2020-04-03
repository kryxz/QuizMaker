package com.lemonlab.quizmaker.items

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.TrueFalseQuestion
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.tf_review.view.*

class TFEdit(
    private val q: TrueFalseQuestion,
    private val key: String,
    private val questions: HashMap<String, TrueFalseQuestion>,
    private val update: () -> Unit

) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val context = view.context

        TFReview.initView(view, q)

        with(view.editQuestion) {
            visibility = View.VISIBLE
            setOnClickListener {
                edit(context)
            }

        }

    }

    override fun getLayout() = R.layout.tf_review

    private fun edit(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context).create()

        val dialogView = with(LayoutInflater.from(context)) {
            inflate(R.layout.tf_edit_dialog, null)
        }

        with(dialogView) {
            val qText = findViewById<TextInputEditText>(R.id.questionText)

            val answerBox = findViewById<AppCompatCheckBox>(R.id.answerCheckBox)

            answerBox.isChecked = q.answer
            qText.setText(q.question)


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
                questions[key] = question
                update()
                dialogBuilder.dismiss()
            }
            with(dialogBuilder) {
                setView(dialogView)
                show()
            }

        }

    }
}

