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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_create_account.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


class CreateAccount : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpUI()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        //Hides keypad
        (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view!!.windowToken,
            0
        )
        super.onDestroyView()
    }

    private fun fieldsOK(userEmail: CharSequence, userPassword: String): Boolean {
        //returns true if userEmail is an Email, and password is 6+ chars.
        return (Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() && userPassword.length >= 6)
    }

    private fun createAccountAndSignIn(username: String, userEmail: String, userPassword: String) {
        creatingAccountBar.visibility = View.VISIBLE
        val fireStoreDatabase = FirebaseFirestore.getInstance()
        val fireBaseAuth = FirebaseAuth.getInstance()

        val newUser = HashMap<String, User>()

        fireBaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
            creatingAccountBar.alpha = 0.5f

            fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
                creatingAccountBar.alpha = 0.7f
                fireBaseAuth.currentUser!!.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build()
                )
                val randomBios = resources.getStringArray(R.array.randomBios)
                newUser["user"] =
                    User(
                        username,
                        userEmail,
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        randomBios[Random.nextInt(0, randomBios.size)],
                        0,
                        Calendar.getInstance().timeInMillis
                    )

                fireStoreDatabase.collection("users").document(username).set(newUser).addOnSuccessListener {
                    Navigation.findNavController(view!!).navigate(
                        CreateAccountDirections.createToMain(),
                        NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                    )
                }
            }


        }
    }

    private fun removeSpecialChars(input: String): String {
        return removeWhitespace(input).replace(Regex("[^a-zA-Z0-9_ ]"), "").replace(" ", "")
    }

    private fun removeWhitespace(string: String): String {
        var isFirstSpace = false
        var result = ""
        for (char in string) {
            if (char != ' ' && char != '\n') {
                isFirstSpace = true
                result += char
            } else if (isFirstSpace) {
                result += " "
                isFirstSpace = false
            }
        }
        return result
    }

    private fun showHidePassword() {
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                signUpPasswordEditText.transformationMethod = null
            else
                signUpPasswordEditText.transformationMethod = PasswordTransformationMethod()

        }
    }

    private fun setUpUI() {
        showHidePassword()
        signUpPasswordEditText.transformationMethod = PasswordTransformationMethod()
        val fireStoreDatabaseUsers = FirebaseFirestore.getInstance().collection("users")
        signUpButton.setOnClickListener {
            val username = removeSpecialChars(signUpDisplayName.text.toString())
            val userEmail = signUpEmailEditText.text.toString()
            val userPassword = signUpPasswordEditText.text.toString()
            if (fieldsOK(userEmail, userPassword)) {
                var isUserNameOK = false
                var isEmailOK = false
                fireStoreDatabaseUsers.get().addOnSuccessListener {

                    for (doc in it) {
                        isEmailOK = if (it != null)
                            !doc.get("user.email", String::class.java).toString().equals(userEmail, ignoreCase = true)
                        else
                            true
                        isUserNameOK = if (it != null)
                            !doc.get("user.username", String::class.java).toString().equals(
                                username,
                                ignoreCase = true
                            )
                        else
                            true
                    }
                    when {
                        isUserNameOK && isEmailOK -> createAccountAndSignIn(username, userEmail, userPassword)
                        !isEmailOK && !isUserNameOK -> invalidUserNameAndEmail()
                        !isEmailOK -> signUpEmailEditText.error = getString(R.string.accountExists)
                        else -> userNameExists()
                    }
                }
            } else
                showErrors()
        }
    }

    private fun userNameExists() {
        signUpDisplayName.error = getString(R.string.userNameExists)
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

    private fun invalidUserNameAndEmail() {
        signUpEmailEditText.error = getString(R.string.accountExists)

    }

    private fun showErrors() {
        showToast(context!!, getString(R.string.checkFields))
        if (!Patterns.EMAIL_ADDRESS.matcher(signUpEmailEditText.text.toString()).matches())
            signUpEmailEditText.error = getString(R.string.invalidEmail)
        if (signUpPasswordEditText.text!!.length < 6)
            signUpPasswordEditText.error = getString(R.string.passwordTooShort)
    }
}
