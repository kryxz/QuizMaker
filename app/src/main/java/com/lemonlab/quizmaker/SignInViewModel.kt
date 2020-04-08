package com.lemonlab.quizmaker

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.lemonlab.quizmaker.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FireRepo()


    suspend fun isSignedUp(name: String) =
        repo.getUsersRef().document(name).get().await().exists()


    private fun addUserToDataBase(name: String, email: String, bio: String, enter: () -> Unit) {
        val user = User(
            name,
            email,
            repo.getAuth().currentUser!!.uid,
            bio,
            0,
            Calendar.getInstance().timeInMillis
        )

        val newUser = HashMap<String, User>()
        newUser["user"] = user


        repo.getUsersRef().document(name).set(newUser).addOnSuccessListener {
            repo.getAuth().currentUser!!.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build()
            ).addOnSuccessListener {
                enter()
            }
        }

    }

    fun createUser(
        context: Context,
        name: String,
        email: String,
        pass: String,
        enter: () -> Unit,
        failed: () -> Unit
    ) {

        fun showMessage(id: Int) {
            context.showToast(context.getString(id))
        }

        val randomBios = context.resources.getStringArray(R.array.randomBios)
        val bio = randomBios[Random.nextInt(0, randomBios.size)]

        repo.getAuth().createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful)
                addUserToDataBase(name, email, bio, enter)
            else {
                failed()

                when (task.exception) {
                    is FirebaseAuthEmailException ->
                        showMessage(R.string.invalidEmail)

                    is FirebaseAuthUserCollisionException ->
                        showMessage(R.string.userNameExists)

                    else ->
                        showMessage(R.string.cannotLogin)

                }
            }
        }


    }

    fun loginEmail(
        context: Context, email: String,
        pass: String, enter: () -> Unit, loginFailed: () -> Unit
    ) {

        fun showMessage(id: Int) {
            context.showToast(context.getString(id))
        }
        repo.getAuth().signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                enter() // goes to main
            } else {
                loginFailed()
                when (it.exception) {
                    is FirebaseAuthInvalidCredentialsException ->
                        showMessage(R.string.invalidLoginInfo)

                    else ->
                        showMessage(R.string.cannotLogin)

                }
            }

        }

    }

    private suspend fun getEmail(name: String): String {
        return repo.getUsersRef().document(name).get().await().get("user.email", String::class.java)
            ?: ""

    }

    fun loginUsername(
        context: Context, name: String, pass: String,
        enter: () -> Unit, loginFailed: () -> Unit
    ) {
        fun showMessage(id: Int) {
            context.showToast(context.getString(id))
        }

        viewModelScope.launch {
            if (!isSignedUp(name)) {
                showMessage(R.string.noUser)
                loginFailed()
                return@launch
            }
            val email = getEmail(name)
            if (email.isEmpty()) {
                loginFailed()
                showMessage(R.string.noUser)
            } else
                loginEmail(context, getEmail(name), pass, enter, loginFailed)

        }
    }

}
