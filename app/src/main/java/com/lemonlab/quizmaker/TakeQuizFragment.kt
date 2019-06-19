package com.lemonlab.quizmaker


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_take_quiz.*


class TakeQuizFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpQuiz()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setUpQuiz() {
        val quizID = TakeQuizFragmentArgs.fromBundle(arguments!!).quizID
        FirebaseFirestore.getInstance().collection("Quizzes").document(quizID).get()
            .addOnSuccessListener { document ->
                val userQuiz = document.get("quiz", MultipleChoiceQuiz::class.java)!!
                (activity as AppCompatActivity).supportActionBar!!.title = userQuiz.quiz?.quizTitle
                Toast.makeText(context!!, getString(R.string.swipeLeftRight), Toast.LENGTH_SHORT).show()
                with(quizQuestionsRV) {
                    layoutManager = ViewPagerLayoutManager(context, 0)
                    adapter = TakeQuizAdapter(context, userQuiz, null)
                }
            }

    }

    override fun onDestroyView() {
        with(quizQuestionsRV) {
            layoutManager = null
            adapter = null
            onFlingListener = null
        }
        super.onDestroyView()
    }

}
