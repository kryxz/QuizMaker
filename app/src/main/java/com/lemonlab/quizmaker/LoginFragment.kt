package com.lemonlab.quizmaker


import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private val vm: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onDestroyView() {
        activity!!.hideKeypad()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }


    private fun init() {
        showHidePassword()

        newAcctButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.createAccount)
        }

        fun failed() {
            loginButton.isEnabled = true
            loggingInBar.visibility = View.GONE
        }

        fun success() {
            Navigation.findNavController(view!!).navigate(
                LoginFragmentDirections.loginToMain(),
                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
            )

        }
        loginButton.setOnClickListener {
            val userLogin = loginEmailEditText.text.toString()
            val userPassword = loginPasswordEditText.text.toString()
            if (fieldsOK(userLogin, userPassword)) {
                loginButton.isEnabled = false
                loggingInBar.visibility = View.VISIBLE

                if (userLogin.contains('@'))
                    vm.loginEmail(context!!, userLogin, userPassword, ::success, ::failed)
                else
                    vm.loginUsername(context!!, userLogin, userPassword, ::success, ::failed)

            }
        }


    }


    private fun fieldsOK(userEmail: CharSequence, userPassword: String): Boolean {
        // returns true if userEmail is an Email, and password is 6+ chars.

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
        loginPasswordEditText.transformationMethod = PasswordTransformationMethod()
        showLoginPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                loginPasswordEditText.transformationMethod = null
            else
                loginPasswordEditText.transformationMethod = PasswordTransformationMethod()

        }
    }
}
