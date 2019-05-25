package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUI()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setUpUI() {
        val fireBaseAuth = FirebaseAuth.getInstance()
        fun loginNow() {
            Navigation.findNavController(view!!).navigate(
                LoginFragmentDirections.loginToMain(),
                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
            )
        }
        if (fireBaseAuth.currentUser != null) {
            loginNow()
        }
        newAcctButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.createAccount)
        }
        loginButton.setOnClickListener {
            val userEmail = loginEmailEditText.text.toString()
            val userPassword = loginPasswordEditText.text.toString()
            if (userEmail.contains('@') && userPassword.length >= 6) {
                fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
                    loginNow()

                }.addOnFailureListener { exception ->
                    Log.i("Login", exception.message)
                }
            }
        }
    }
}
