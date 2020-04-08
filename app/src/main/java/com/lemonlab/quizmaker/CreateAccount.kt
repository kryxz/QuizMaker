package com.lemonlab.quizmaker


import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_create_account.*
import kotlinx.coroutines.launch
import kotlin.random.Random


class CreateAccount : Fragment() {

    private val vm: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }


    private fun init() {
        showHidePassword()

        fun failed() {
            signUpButton.isEnabled = true
            creatingAccountBar.visibility = View.GONE
        }

        fun success() {
            Navigation.findNavController(view!!).navigate(
                CreateAccountDirections.createToMain(),
                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true)
                    .build()
            )

        }
        signUpButton.setOnClickListener {
            val username = signUpDisplayName.text.toString().removeSpecialChars()
            val userEmail = signUpEmailEditText.text.toString().removedWhitespace()
            val userPassword = signUpPasswordEditText.text.toString().removedWhitespace()

            if (fieldsOK(userEmail, userPassword)) {
                signUpButton.isEnabled = false
                creatingAccountBar.visibility = View.VISIBLE
                vm.viewModelScope.launch {
                    val isNameUsed = vm.isSignedUp(username)
                    if (isNameUsed) {
                        userNameExists()
                        return@launch
                    } else {
                        vm.createUser(
                            context!!,
                            username,
                            userEmail,
                            userPassword,
                            ::success,
                            ::failed
                        )

                    }


                }
            }

        }
    }


    private fun showHidePassword() {
        signUpPasswordEditText.transformationMethod = PasswordTransformationMethod()
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                signUpPasswordEditText.transformationMethod = null
            else
                signUpPasswordEditText.transformationMethod = PasswordTransformationMethod()

        }
    }

    private fun userNameExists() {
        creatingAccountBar.visibility = View.GONE
        signUpButton.isEnabled = true
        signUpDisplayName.error = getString(R.string.userNameExists)
        showSuggestions()
    }

    private fun showSuggestions() {
        val userName = signUpEmailEditText.text.toString().substringBefore('@')
        val suggestions = listOf(
            "$userName${Random.nextInt(100, 1000)}",
            "$userName${Random.nextInt(100, 1000)}",
            "$userName${Random.nextInt(100, 1000)}"
        )
            .toString().replace("[", "").replace("]", "")
        with(suggestedNamesTextView) {
            text = getString(R.string.suggestions, suggestions)
            visibility = View.VISIBLE
        }
    }

    private fun fieldsOK(userEmail: CharSequence, userPassword: String): Boolean {
        // returns true if input is an email, and password is 6+ chars.

        val emailOK = Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()

        val passwordOK = userPassword.length >= 6
        if (!emailOK)
            signUpEmailEditText.error = getString(R.string.invalidEmail)
        if (!passwordOK)
            signUpPasswordEditText.error = getString(R.string.passwordTooShort)


        return emailOK && passwordOK

    }

    override fun onDestroyView() {
        activity!!.hideKeypad()
        super.onDestroyView()
    }

}
