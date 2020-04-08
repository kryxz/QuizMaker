package com.lemonlab.quizmaker.items

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.*
import com.lemonlab.quizmaker.data.Quiz
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.quiz_item.view.*

class QuizItem(
    private val quiz: Quiz,
    private val vm: QuizzesVM,
    private val lifecycleOwner: LifecycleOwner
) : Item<ViewHolder>() {


    override fun getLayout() = R.layout.quiz_item


    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val context = view.context


        vm.canRate(quiz.quizUUID).observe(lifecycleOwner, Observer {
            setUpQuizButton(context, it, view.startQuizButton)

        })



        initView(view, quiz, context)

        with(view) {
            quizAuthorText.setOnClickListener {
                val action = MainFragmentDirections.viewProfile()
                action.isViewer = true
                action.username = quiz.quizAuthor
                it.findNavController().navigate(action)
            }
            val isAuthor = quiz.quizAuthor == vm.getName()

            with(editMyQuiz) {
                visibility = if (isAuthor) View.VISIBLE
                else View.GONE
                setOnClickListener {
                    findNavController()
                        .navigate(MainFragmentDirections.editQuizNow().setQuizID(quiz.quizUUID))
                }
            }

            startQuizButton.setOnClickListener {
                if (quiz.passwordProtected)
                    enterPasswordDialog(view, context, quiz, ::enterQuiz)
                else
                    enterQuiz(it)
            }

        }


    }

    companion object {

        fun setUpQuizButton(context: Context, bool: Boolean, button: AppCompatButton) {

            val title = if (!bool)
                context.getString(R.string.quizAlreadyTaken)
            else
                context.getString(R.string.startQuiz)

            val icon = if (!bool)
                ContextCompat.getDrawable(context, R.drawable.ic_replay)
            else
                ContextCompat.getDrawable(context, R.drawable.ic_quiz)
            with(button) {
                text = title
                setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
            }

        }

        fun initView(view: View, quiz: Quiz, context: Context) {
            with(view) {
                quizTypeTextView.text = context.getString(quiz.quizType!!.id)
                quizTitleText.text = quiz.quizTitle
                quizAuthorText.text =
                    context.getString(R.string.quizAuthorText, quiz.quizAuthor)
                questionsCountText.text =
                    context.getString(R.string.questionsCountText, quiz.questionsCount)
                quizRatingBar.rating = quiz.rating
                quizDateTextView.text = quiz.milliSeconds.timeAsAString()
            }
        }

        fun enterPasswordDialog(
            view: View,
            context: Context, quiz: Quiz,
            enter: (view: View) -> Unit
        ) {

            val dialogBuilder = AlertDialog.Builder(context).create()
            val dialogView = with(LayoutInflater.from(context)) {
                inflate(
                    R.layout.enter_password_dialog,
                    null
                )
            }
            val passwordField =
                dialogView.findViewById<TextInputEditText>(R.id.enterQuizPasswordEditText)
            val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmQuizPassword)
            val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelPasswordDialog)

            confirmButton.setOnClickListener {

                if (passwordField.text.toString() == quiz.quizPin)
                    enter(view)
                else
                    context.showToast(context.getString(R.string.wrongPassword))

                dialogBuilder.dismiss()
            }

            cancelButton.setOnClickListener { dialogBuilder.dismiss() }
            dialogBuilder.setView(dialogView)
            dialogBuilder.show()

        }

    }

    private fun enterQuiz(view: View) =
        view.findNavController().navigate(MainFragmentDirections.goToQuizNow(quiz.quizUUID))


}


