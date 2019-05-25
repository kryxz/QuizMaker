package com.lemonlab.quizmaker


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
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
        super.onViewCreated(view, savedInstanceState)
    }
    private fun loginIfNoUser(){
        //send user to Login fragment if need be.
        if (FirebaseAuth.getInstance().currentUser == null)
            Navigation.findNavController(view!!).navigate(
                MainFragmentDirections.MainToLogin(),
                NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
            )
    }
    private fun setUp(){
        loginIfNoUser()
        createQuizButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.createQuiz)
        }
    }

}
