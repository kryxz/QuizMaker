package com.lemonlab.quizmaker


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_create_account.*


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
    private fun setUpUI() {
        val fireStoreDatabase = FirebaseFirestore.getInstance()
        val fireBaseAuth =  FirebaseAuth.getInstance()
        signUpButton.setOnClickListener { view ->
            val username = signUpDisplayName.text.toString()
            val userEmail = signUpEmailEditText.text.toString()
            val userPassword = signUpPasswordEditText.text.toString()
            if (userEmail.contains('@') && userPassword.length >= 6 && username.length > 3) {
                val newUser = HashMap<String, String>()
                newUser["username"] = username
                newUser["email"] = userEmail
                fireBaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnSuccessListener {
                    fireBaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    newUser["id"] = fireBaseAuth.currentUser!!.uid
                    fireStoreDatabase.collection("users").add(newUser)
                    Navigation.findNavController(view).navigate(R.id.loginFragment)

                }
            }else
                ;
                //TODO Notify user if they should fill the fields again... wrong email formatting, password too short, username empty, etc.
        }
    }
}
