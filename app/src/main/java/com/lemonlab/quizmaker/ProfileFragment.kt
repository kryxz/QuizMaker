package com.lemonlab.quizmaker


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.items.QuizLog
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    private lateinit var vm: QuizzesVM


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.hideKeypad()
    }


    private fun init() {
        vm = (activity as MainActivity).vm

        val args = ProfileFragmentArgs.fromBundle(arguments!!)

        if (args.isViewer)
            viewProfile(args.username)
        else
            getDataToSetupProfile()

    }


    private fun viewProfile(username: String) {
        vm.getUser(name = username).observe(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer
            joinDateTextView.text = user.joinTimeAsAString()
            currentPointsTextView.text = user.points.toString()
            userBioTextView.text = user.userBio

            userBioTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null, null
            )
            userNameTextView.text = getString(R.string.userNameText, username)

            (activity as AppCompatActivity).supportActionBar!!.title =
                getString(R.string.userProfileLabel, username)

            with(logEmptyTextView) {
                visibility = View.VISIBLE
                text = getString(R.string.cannotViewLog)
            }
        })
        //
        vm.getLog(username).observe(viewLifecycleOwner, Observer { log ->
            quizzesTakenTextView.text = log.userLog.size.toString()
        })

    }

    private fun showEditBioDialog() {
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
            vm.updateBio(newBio)
            showToast(context!!, getString(R.string.editedSuccessfully))
            userBioTextView.text = newBio
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
        val userName = vm.getName()
        getLog(userName)

        vm.getUser(userName).observe(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer
            setUpProfileUI(user)
        })


    }

    private fun getLog(username: String) {
        fun loadLog(log: MutableList<String>) {
            if (log.isEmpty()) return
            profileLogProgressBar.visibility = View.VISIBLE
            val listOfQuizzes = mutableListOf<Quiz>()

            val adapter = GroupAdapter<ViewHolder>()
            userLogRecyclerView.layoutManager = LinearLayoutManager(context!!)

            vm.getAllQuizzes().observe(viewLifecycleOwner, Observer { list ->
                if (list == null || list.isEmpty()) {
                    adapter.clear()
                    return@Observer
                }
                adapter.clear()
                listOfQuizzes.clear()

                for (item in list) {
                    val id = item.quizUUID
                    if (log.contains(id))
                        listOfQuizzes.add(item)
                }
                for (item in listOfQuizzes)
                    adapter.add(QuizLog(item))

                userLogRecyclerView.adapter = adapter
                profileLogProgressBar.visibility = View.GONE

            })

        }
        vm.getLog(username).observe(viewLifecycleOwner, Observer { log ->
            if (log == null) {
                quizzesTakenTextView.text = getString(R.string.placeHolderZero)
                return@Observer
            }
            quizzesTakenTextView.text = log.userLog.size.toString()
            loadLog(log.userLog)
        })

    }


    private fun setUpProfileUI(user: User) {

        profileFragmentProgressBar.visibility = View.GONE
        joinDateTextView.text = user.joinTimeAsAString()
        currentPointsTextView.text = user.points.toString()
        userBioTextView.text = user.userBio

        userBioTextView.setOnClickListener {
            showEditBioDialog()
        }

        userNameTextView.text = getString(R.string.userNameText, user.username)

    }

}
