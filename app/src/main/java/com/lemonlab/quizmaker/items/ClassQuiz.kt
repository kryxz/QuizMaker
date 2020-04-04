package com.lemonlab.quizmaker.items

import android.view.View
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

        })
        val isCreator = vm.getName() == quiz.quizAuthor

        view.editQuizButton.visibility = if (isCreator)
            View.VISIBLE
        else View.GONE

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

    private fun enterQuiz(view: View) {
        val action =
            TeachFragmentDirections.takeClassQuiz(quiz.quizUUID)
        action.classCode = code
        view.findNavController()
            .navigate(action)
    }

    override fun getLayout() = R.layout.quiz_item


}


/*
    private fun downloadQuiz(){
    val printAttrs =
        PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(PrintAttributes.MediaSize.NA_LETTER).setResolution(
                Resolution(
                    "zooey",
                    PRINT_SERVICE,
                    300,
                    300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

    val document: PdfDocument = PrintedPdfDocument(context, printAttrs)
    val pageInfo = PageInfo.Builder(300, 300, 1).create()
    val page = document.startPage(pageInfo)
    val content: View = holder.itemView.findViewById(R.id.questionsCountText)
    content.draw(page.canvas)
    // do final processing of the page
    document.finishPage(page)

    try {
        val f = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
                .toString() + "/sample.pdf"
        )
        Log.i("SavingPDF", f.absolutePath)
        Log.i("SavingPDF", f.path)
        val fos = FileOutputStream(f)
        document.writeTo(fos)
        document.close()
        fos.close()
    } catch (e: IOException) {
        throw RuntimeException("Error generating file", e)
    }
}

 */