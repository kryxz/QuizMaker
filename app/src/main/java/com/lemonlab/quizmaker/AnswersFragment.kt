package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.lemonlab.quizmaker.items.MulReview
import com.lemonlab.quizmaker.items.TFReview
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_answers.*


class AnswersFragment : Fragment() {

    private lateinit var vm: QuizzesVM

    companion object {
        var seeAnswers = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        init()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_download, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.downloadNow)
            dialog()

        return super.onOptionsItemSelected(item)
    }

    private fun dialog() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.yes_no_dialog,
                null
            )
        }

        dialogView.findViewById<AppCompatTextView>(R.id.dialogTitle).text =
            getString(R.string.saveQuiz)
        dialogView.findViewById<AppCompatTextView>(R.id.dialogMessageText).text =
            getString(R.string.quizWillBeSavedImage)


        with(dialogView.findViewById<AppCompatButton>(R.id.dialogCancelButton)) {
            text = getString(R.string.withoutAnswers)
            setOnClickListener {
                seeAnswers = false
                dialogBuilder.dismiss()
                viewQuizAnswers()
                Handler().postDelayed({
                    questionsRecyclerView.downloadAsBitMap()
                }, 1000)
            }
        }

        with(dialogView.findViewById<AppCompatButton>(R.id.dialogConfirmButton)) {
            text = getString(R.string.withAnswers)
            setOnClickListener {
                seeAnswers = true
                dialogBuilder.dismiss()
                viewQuizAnswers()
                Handler().postDelayed({
                    questionsRecyclerView.downloadAsBitMap()
                }, 1000)
            }
        }


        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }


    private fun init() {
        viewQuizAnswers()
    }


    private fun viewQuizAnswers() {
        val quizID = AnswersFragmentArgs.fromBundle(arguments!!).quizID
        val code = AnswersFragmentArgs.fromBundle(arguments!!).classCode

        (activity as AppCompatActivity).supportActionBar!!.title =
            getString(R.string.viewAnswers)

        val adapter = GroupAdapter<ViewHolder>().apply {
            clear()
        }

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


        vm.getQuiz(code, quizID).observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { quiz ->

                if (quiz is MultipleChoiceQuiz) mulChoice(quiz)
                else if (quiz is TrueFalseQuiz) tf(quiz)

                (activity as AppCompatActivity).supportActionBar.also {
                    it!!.setHomeAsUpIndicator(R.drawable.ic_cancel)
                    it.title = quiz?.getTitle()
                }
            })

    }

}


