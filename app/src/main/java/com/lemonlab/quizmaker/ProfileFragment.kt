package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.adapters.QuizAdapter
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isUserOrViewer()
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

    private fun isUserOrViewer() {
        val args = ProfileFragmentArgs.fromBundle(arguments!!)
        if (args.isViewer)
            viewProfile(args.username)
        else
            getDataToSetupProfile()
    }

    private fun viewProfile(username: String) {
        profileFragmentProgressBar.visibility = View.VISIBLE
        val profileRef = FirebaseFirestore.getInstance().collection("users").document(username)
        tapAQuizTextView.visibility = View.GONE
        profileRef.get().addOnSuccessListener {
            if (view != null) {
                profileFragmentProgressBar.visibility = View.GONE
                val user = it.get("user", User::class.java)!!
                joinDateTextView.text = user.joinTimeAsAString()
                currentPointsTextView.text = user.points.toString()
                userBioTextView.text = user.userBio
                userBioTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                userNameTextView.text = getString(R.string.userNameText, username)
                (activity as AppCompatActivity).supportActionBar!!.title =
                    getString(R.string.userProfileLabel, username)
                with(logEmptyTextView) {
                    visibility = View.VISIBLE
                    text = getString(R.string.cannotViewLog)
                }
                profileRef.collection("userData").document("taken").get().addOnSuccessListener { document ->
                    val log = document.get("log", QuizLog::class.java)
                    if (log != null && view != null) {
                        quizzesTakenTextView.text = log.userLog.size.toString()
                    }
                }

            }
        }
    }

    private fun showEditBioDialog(profileRef: DocumentReference) {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.change_bio_dialog,
                null
            )
        }
        val bioEditText = dialogView.findViewById<TextInputEditText>(R.id.userBioEditText)
        val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmUserBio)
        val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelUserBio)

        fun saveBio() {
            val newBio = bioEditText.text.toString()
            profileRef.update("user.userBio", newBio).addOnSuccessListener {
                showToast(context!!, getString(R.string.editedSuccessfully))
                userBioTextView.text = newBio
            }

        }
        bioEditText.hint = userBioTextView.text
        confirmButton.setOnClickListener {
            if (bioEditText.text.toString().isEmpty())
                showToast(context!!, getString(R.string.cannotBeEmpty))
            else
                saveBio()
            dialogBuilder.dismiss()
        }
        cancelButton.setOnClickListener { dialogBuilder.dismiss() }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }


    private fun getDataToSetupProfile() {
        val userName = FirebaseAuth.getInstance().currentUser!!.displayName!!
        val profileRef = FirebaseFirestore.getInstance().collection("users").document(userName)
        getLog(userName)

        if (TempData.user != null)
            setUpProfileUI(TempData.user!!, profileRef)
        else
            profileRef.get().addOnSuccessListener {
                if (view != null) {
                    val user = it.get("user", User::class.java)!!
                    TempData.user = user
                    setUpProfileUI(user, profileRef)
                }
            }

    }

    private fun getLog(username: String) {
        fun loadLog(log: MutableList<String>) {
            profileLogProgressBar.visibility = View.VISIBLE
            val listOfQuizzes = mutableListOf<Quiz>()
            FirebaseFirestore.getInstance().collection("Quizzes").get().addOnSuccessListener { documents ->
                for (item in documents) {
                    val quiz = item.get("quiz.quiz", Quiz::class.java)!!
                    if (log.contains(quiz.quizUUID))
                        listOfQuizzes.add(quiz)
                }
                with(listOfQuizzes) {
                    sortWith(compareBy { it.milliSeconds })
                    reverse()
                }
                if (view != null)
                    with(userLogRecyclerView) {
                        layoutManager = LinearLayoutManager(context!!)
                        adapter = QuizAdapter(context!!, listOfQuizzes, ViewType.ViewAnswers)
                        profileLogProgressBar.visibility = View.GONE
                    }
            }
        }
        FirebaseFirestore.getInstance().collection("users").document(username)
            .collection("userData").document("taken")
            .get()
            .addOnSuccessListener { document ->
                if (view != null) {
                    val log = document.get("log", QuizLog::class.java)
                    if (log != null) {
                        quizzesTakenTextView.text = log.userLog.size.toString()
                        loadLog(log.userLog)
                    } else {
                        quizzesTakenTextView.text = getString(R.string.placeHolderZero)
                        logEmptyTextView.visibility = View.VISIBLE
                    }

                }
            }


    }


    private fun setUpProfileUI(user: User, profileRef: DocumentReference) {
        if (view == null)
            return
        profileFragmentProgressBar.visibility = View.GONE
        joinDateTextView.text = user.joinTimeAsAString()
        currentPointsTextView.text = user.points.toString()
        userBioTextView.text = user.userBio
        userBioTextView.setOnClickListener {
            showEditBioDialog(profileRef)
        }
        userNameTextView.text = getString(R.string.userNameText, user.username)

        profileRef.get().addOnSuccessListener {
            val newUser = it.get("user", User::class.java)!!
            if (newUser != TempData.user) {
                TempData.user = newUser
                setUpProfileUI(newUser, profileRef)
            }

        }
    }

}
