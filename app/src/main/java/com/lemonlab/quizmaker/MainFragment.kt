package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.adapters.QuizAdapter
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUp()
        (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_drawer)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loginIfNoUser() {
        //send user to Login fragment if need be.
        if (FirebaseAuth.getInstance().currentUser == null)
            Navigation.findNavController(view!!).navigate(
                MainFragmentDirections.MainToLogin(),
                NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
            )
    }

    private fun setUp() {
        loginIfNoUser()
        getData()
        createQuizButton.setOnClickListener {
            it.animate().scaleX(.3f).scaleY(.3f).setDuration(50)
                .withEndAction {
                    Navigation.findNavController(it).navigate(R.id.createQuiz)
                }
        }

    }

    override fun onDestroyView() {
        with(QuizzesRecyclerView) {
            layoutManager = null
            adapter = null
            onFlingListener = null
        }
        super.onDestroyView()
    }

    private fun getData() {
        if (TempData.currentQuizzes.isNullOrEmpty())
            setUpAdapter()
        else
            with(QuizzesRecyclerView) {
                layoutManager = LinearLayoutManager(context!!)
                adapter = QuizAdapter(context!!, TempData.currentQuizzes!!, ViewType.TakeQuiz)
                MainFragmentProgressBar.visibility = View.GONE
            }
    }

    private fun setUpAdapter() {
        val listOfQuizzes = mutableListOf<Quiz>()
        //This is how we retrieve data from the database
        FirebaseFirestore.getInstance().collection("Quizzes").get().addOnSuccessListener { documents ->


            for (item in documents) {
                listOfQuizzes.add(item.get("quiz.quiz", Quiz::class.java)!!)
                val source = if (item.metadata.isFromCache)
                    "local cache"
                else
                    "server"
                Log.d("Data", "Data fetched from $source")

            }
            //to get questions, use item.get("quiz", MultipleChoiceQuiz::class.java)!!.questions
            with(listOfQuizzes) {
                sortWith(compareBy { it.milliSeconds })
                reverse()
            }
            TempData.currentQuizzes = listOfQuizzes

            if (view != null)
                with(QuizzesRecyclerView) {
                    layoutManager = LinearLayoutManager(context!!)
                    adapter = QuizAdapter(context!!, listOfQuizzes, ViewType.TakeQuiz)
                    MainFragmentProgressBar.visibility = View.GONE
                }
        }

    }

}
