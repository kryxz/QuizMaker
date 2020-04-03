package com.lemonlab.quizmaker


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lemonlab.quizmaker.items.QuizItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment() {

    private lateinit var viewModel: QuestionsVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun checkURL() {
        viewModel = (activity as MainActivity).questionsVM
        if (viewModel.getClassJoinCode() != "empty") {
            (activity as AppCompatActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_drawer)
            viewModel.setClassJoinCode("empty")
            Handler().postDelayed({
                if (view != null)
                    view!!.findNavController().navigate(MainFragmentDirections.goToClasses())
            }, 2000)
        }
    }

    private fun init() {
        checkURL()
        val vm = (activity as MainActivity).vm

        if (!vm.isLoggedIn())
            view!!.findNavController().navigate(
                MainFragmentDirections.MainToLogin(),
                NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
            )

        val adapter = GroupAdapter<ViewHolder>()

        getDataFromIntent()
        vm.getAllQuizzes().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) return@Observer
            with(QuizzesRecyclerView) {
                layoutManager = LinearLayoutManager(context!!)
                MainFragmentProgressBar.visibility = View.GONE
                removeAllViews()
                adapter.clear()
                for (item in it)
                    adapter.add(QuizItem(item, vm, lifecycleOwner = viewLifecycleOwner))
                this.adapter = adapter
            }

        })
    }


    private fun getDataFromIntent() {
        val intent = activity!!.intent
        if (intent != null
            && intent.extras != null
        ) {
            if (intent.extras!!.get("notificationType") != null) {
                val notificationType =
                    (intent.extras!!.get("notificationType") as NotificationType)
                if (notificationType == NotificationType.MESSAGE)
                    Navigation.findNavController(view!!).navigate(R.id.messagesFragment)
                else {
                    Navigation.findNavController(view!!).navigate(R.id.profileFragment)
                }

            }
        }

    }

}
