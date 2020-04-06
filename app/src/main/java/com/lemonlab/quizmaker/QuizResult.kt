package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_quiz_result.*


class QuizResult : Fragment() {

    private lateinit var vm: QuizzesVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        loadData()
    }

    private fun loadData() {

        val args = QuizResultArgs.fromBundle(arguments!!)

        val avg = (args.score.toDouble() / args.totalQuestions.toDouble() * 100.0).toString()

        quizScoreText.text =
            getString(R.string.quizScore, args.quizTitle, args.totalQuestions, args.score, avg)

        shouldRate(args.quizID)

        if (canMessageAuthor())
            messageQuizAuthorButton.setOnClickListener {
                sendMessageToAuthorDialog(args.quizAuthor)
            }
        else
            messageQuizAuthorButton.visibility = View.GONE

        resultViewAnswersButton.setOnClickListener {
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.mainFragment, false).build()

            it.findNavController().navigate(
                QuizResultDirections.viewQuizAnswers().setClassCode(args.classCode)
                    .setQuizID(args.quizID), navOptions
            )

        }
    }


    private fun canMessageAuthor() =
        QuizResultArgs.fromBundle(arguments!!).quizAuthor !=
                vm.getName()


    private fun sendMessageToAuthorDialog(quizAuthor: String) {
        val dialogBuilder = AlertDialog.Builder(context!!).create()
        val dialogView = with(layoutInflater) {
            inflate(
                R.layout.send_message_dialog,
                null
            )
        }

        dialogView.findViewById<AppCompatButton>(R.id.sendNowButton).setOnClickListener {

            vm.sendMessage(
                context!!,
                quizAuthor,
                dialogView.findViewById<TextInputEditText>(R.id.messageText).text.toString()
                    .removedWhitespace()
            )

            context!!.showToast(getString(R.string.messageSent))

            dialogBuilder.dismiss()

        }
        dialogView.findViewById<AppCompatButton>(R.id.cancelSendButton).setOnClickListener {
            dialogBuilder.dismiss()
        }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }

    private fun logQuiz() {
        val args = QuizResultArgs.fromBundle(arguments!!)
        vm.logQuiz(
            id = args.quizID,
            score = args.score,
            author = args.quizAuthor,
            total = args.totalQuestions,
            context = context!!
        )

    }

    private fun sendRatingNow() {
        val args = QuizResultArgs.fromBundle(arguments!!)

        vm.rateQuiz(
            args.quizID,
            quizFinishedRatingBar.rating,
            classCode = args.classCode
        )
        shouldRate(args.quizID)

    }

    private fun showHideRating(visibility: Int) {
        sendRatingButton.visibility = visibility
        quizFinishedRatingBar.visibility = visibility
        rateQuizNow.visibility = visibility
    }

    private fun shouldRate(quizID: String) {
        showHideRating(View.GONE)
        // This decides if a user's rating should be sent to database (users can rate a quiz once).


        vm.canRate(quizID).observe(viewLifecycleOwner, Observer { canRate ->
            if (canRate) {
                showHideRating(View.VISIBLE)
                sendRatingButton.setOnClickListener {
                    sendRatingNow()
                }
            } else {
                showHideRating(View.GONE)
                quizFinishedRatingBar.isEnabled = false
                sendRatingButton.isEnabled = false
            }
        })
        logQuiz()

    }
}
