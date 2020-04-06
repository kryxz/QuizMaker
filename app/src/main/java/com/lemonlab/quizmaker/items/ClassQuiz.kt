package com.lemonlab.quizmaker.items

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.lemonlab.quizmaker.Quiz
import com.lemonlab.quizmaker.QuizzesVM
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.TeachFragmentDirections
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.quiz_item.view.*

class ClassQuiz(
    private val quiz: Quiz,
    private val vm: QuizzesVM,
    private val lifecycleOwner: LifecycleOwner,
    private val code: String
) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val view = viewHolder.itemView
        val context = view.context


        QuizItem.initView(view, quiz, context)

        vm.canRate(quiz.quizUUID).observe(lifecycleOwner, Observer {
            QuizItem.setUpQuizButton(context, it, view.startQuizButton)

            canViewAnswers(!it, view.viewAnswersButton)

        })
        val isCreator = vm.getName() == quiz.quizAuthor

        with(view) {
            editQuizButton.visibility = if (isCreator)
                View.VISIBLE
            else View.GONE

            editQuizButton.setOnClickListener {
                val action = TeachFragmentDirections.editClassQuiz().setClassCode(code)
                    .setQuizID(quiz.quizUUID)
                it.findNavController().navigate(action)
            }

            startQuizButton.setOnClickListener {
                if (quiz.passwordProtected)
                    QuizItem.enterPasswordDialog(view, context, quiz, ::enterQuiz)
                else
                    enterQuiz(it)
            }

        }


    }

    private fun canViewAnswers(can: Boolean, button: AppCompatButton) {
        if (can) {
            with(button) {
                visibility = View.VISIBLE

                setOnClickListener {
                    val action = TeachFragmentDirections.viewQuizAnswers()
                        .setQuizID(quiz.quizUUID).setClassCode(code)
                    findNavController().navigate(action)
                }
            }

        } else
            button.visibility = View.GONE
    }

    private fun enterQuiz(view: View) {
        val action =
            TeachFragmentDirections.takeClassQuiz(quiz.quizUUID)
        action.classCode = code
        view.findNavController()
            .navigate(action)
    }

    override fun getLayout() = R.layout.quiz_item


}

