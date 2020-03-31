package com.lemonlab.quizmaker


import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        activity!!.hideKeypad()
        super.onDestroyView()
    }

    private fun fieldsOK(userEmail: CharSequence, userPassword: String, username: String): Boolean {
        val arabicChars = "ضشئءسصثيؤربقفلأىاغةتعهنوزظمخجدطكح"
        var allEnglish = true
        for (char in arabicChars)
            if (char in username) {
                allEnglish = false
                break
            }

        //returns true if userEmail is an Email, and password is 6+ chars.
        return (Patterns.EMAIL_ADDRESS.matcher(userEmail)
            .matches() && allEnglish && userPassword.length >= 6)
    }

    private fun createAccountAndSignIn(username: String, userEmail: String, userPassword: String) {
        creatingAccountBar.visibility = View.VISIBLE
        val fireStoreDatabase = FirebaseFirestore.getInstance()
        val fireBaseAuth = FirebaseAuth.getInstance()

        val newUser = HashMap<String, User>()

        fireBaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
            creatingAccountBar.alpha = 0.5f
            fireBaseAuth.currentUser!!.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(username).build()
            ).addOnSuccessListener {
                fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnSuccessListener {
                        creatingAccountBar.alpha = 0.7f
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

                        fireStoreDatabase.collection("users").document(username).set(newUser)
                            .addOnSuccessListener {
                                Navigation.findNavController(view!!).navigate(
                                    CreateAccountDirections.createToMain(),
                                    NavOptions.Builder().setPopUpTo(R.id.loginFragment, true)
                                        .build()
                                )
                            }
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
            signUpButton.isEnabled = false
            val username = removeSpecialChars(signUpDisplayName.text.toString())
            val userEmail = signUpEmailEditText.text.toString()
            val userPassword = signUpPasswordEditText.text.toString()
            if (fieldsOK(userEmail, userPassword, signUpDisplayName.text.toString())) {
                var isUserNameOK = true
                var isEmailOK = true
                fireStoreDatabaseUsers.get().addOnSuccessListener {

                    for (doc in it) {
                        isEmailOK = !doc.get("user.email", String::class.java).toString().equals(
                            userEmail,
                            ignoreCase = true
                        )
                        isUserNameOK =
                            !doc.get("user.username", String::class.java).toString().equals(
                                username,
                                ignoreCase = false
                            )
                        if (!isEmailOK || !isUserNameOK)
                            break

                    }
                    when {
                        isUserNameOK && isEmailOK -> createAccountAndSignIn(
                            username,
                            userEmail,
                            userPassword
                        )
                        !isEmailOK -> emailExists()
                        else -> userNameExists()
                    }
                }
            } else
                showErrors()
        }
    }

    private fun emailExists() {
        signUpButton.isEnabled = true
        signUpEmailEditText.error = getString(R.string.accountExists)

    }

    private fun userNameExists() {
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

    private fun showErrors() {
        signUpButton.isEnabled = true
        val arabicChars = "ضشئءسصثيؤربقفلأىاغةتعهنوزظمخجدطكح"
        var allEnglish = true
        for (char in arabicChars)
            if (char in signUpDisplayName.text.toString()) {
                allEnglish = false
                break
            }
        if (!allEnglish) {
            signUpDisplayName.error = getString(R.string.containsArChars)
            showSuggestions()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(signUpEmailEditText.text.toString()).matches())
            signUpEmailEditText.error = getString(R.string.invalidEmail)
        else if (signUpPasswordEditText.text!!.length < 6)
            signUpPasswordEditText.error = getString(R.string.passwordTooShort)
    }
}
