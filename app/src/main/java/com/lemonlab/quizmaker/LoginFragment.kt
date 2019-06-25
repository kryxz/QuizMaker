package com.lemonlab.quizmaker


import android.os.Bundle
import android.util.Patterns
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

    private fun fieldsOK(userEmail: CharSequence, userPassword: String): Boolean {
        //returns true if userEmail is an Email, and password is 6+ chars.

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
            loginEmailEditText.error = getString(R.string.invalidEmail)
        if (userPassword.length < 6)
            loginPasswordEditText.error = getString(R.string.passwordTooShort)

        return (Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() && userPassword.length >= 6)
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
            if (fieldsOK(userEmail, userPassword)) {
                loggingInBar.visibility = View.VISIBLE
                fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
                    loginNow()
                }.addOnFailureListener {
                    loggingInBar.visibility = View.GONE
                    if (it.localizedMessage!!.contains("no user"))
                        showToast(context!!, getString(R.string.noUser))
                    else
                        showToast(context!!, getString(R.string.loginFailed))
                }
            }
        }
    }
}
