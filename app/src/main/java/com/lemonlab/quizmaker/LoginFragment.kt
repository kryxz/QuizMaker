package com.lemonlab.quizmaker


import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onDestroyView() {
        //Hides keypad
        (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view!!.windowToken,
            0
        )
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUI()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun fieldsOK(userEmail: CharSequence, userPassword: String): Boolean {
        //returns true if userEmail is an Email, and password is 6+ chars.

        val emailOK = if (userEmail.contains('@'))
            Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()
        else
            true
        val passwordOK = userPassword.length >= 6
        if (!emailOK)
            loginEmailEditText.error = getString(R.string.invalidEmail)
        if (!passwordOK)
            loginPasswordEditText.error = getString(R.string.passwordTooShort)

        return (emailOK && passwordOK)
    }

    private fun showHidePassword() {
        showLoginPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                loginPasswordEditText.transformationMethod = null
            else
                loginPasswordEditText.transformationMethod = PasswordTransformationMethod()

        }
    }

    private fun setUpUI() {
        val fireBaseAuth = FirebaseAuth.getInstance()
        loginPasswordEditText.transformationMethod = PasswordTransformationMethod()
        showHidePassword()
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

        fun emailLogin(userEmail: String, userPassword: String) {
            fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
                loginNow()
            }.addOnFailureListener {
                loginButton.isEnabled = true
                loggingInBar.visibility = View.GONE
                if (it.localizedMessage!!.contains("no user"))
                    showToast(context!!, getString(R.string.noUser))
                else
                    showToast(context!!, getString(R.string.loginFailed))
            }
        }

        fun userNameLogin(userLogin: String, userPassword: String) {
            FirebaseFirestore.getInstance().collection("users").document(userLogin).get().addOnSuccessListener {
                if (it.get("user.email", String::class.java) != null) {
                    val email = it.get("user.email", String::class.java)!!
                    emailLogin(email, userPassword)
                } else {
                    loginButton.isEnabled = true
                    loggingInBar.visibility = View.GONE
                    showToast(context!!, getString(R.string.noUser))
                }
            }
        }
        loginButton.setOnClickListener {
            val userLogin = loginEmailEditText.text.toString()
            val userPassword = loginPasswordEditText.text.toString()
            if (fieldsOK(userLogin, userPassword)) {
                loginButton.isEnabled = false
                loggingInBar.visibility = View.VISIBLE
                if (userLogin.contains('@'))
                    emailLogin(userLogin, userPassword)
                else
                    userNameLogin(userLogin, userPassword)

            }
        }
    }
}
