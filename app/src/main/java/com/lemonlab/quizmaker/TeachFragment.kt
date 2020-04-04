package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lemonlab.quizmaker.items.ClassQuiz
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_teach.*


class TeachFragment : Fragment() {

    private lateinit var vm: QuizzesVM


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teach, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm = (activity as MainActivity).vm
        init()
        super.onViewCreated(view, savedInstanceState)
    }


    private fun init() {
        classQuizzesProgressBar.visibility = View.VISIBLE

        val code = TeachFragmentArgs.fromBundle(arguments!!).classCode

        vm.getClass(code).observe(viewLifecycleOwner, Observer { that ->
            setTitle(that.title)
            val isUserCreated = that.teach == vm.getName()
            if (that.open || isUserCreated)
                createQuizButton.visibility = View.VISIBLE
            else createQuizButton.visibility = View.GONE

        })

        val adapter = GroupAdapter<ViewHolder>()
        vm.getClassQuizzes(code).observe(viewLifecycleOwner, Observer { list ->

            if (list == null || list.isEmpty()) {
                classQuizzesProgressBar.visibility = View.GONE
                noClassQuizzes.visibility = View.VISIBLE
                classQuizzesRV.visibility = View.GONE
                return@Observer
            }

            classQuizzesProgressBar.visibility = View.GONE
            noClassQuizzes.visibility = View.GONE

            with(classQuizzesRV) {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context!!)
                removeAllViews()
                adapter.clear()
                for (item in list)
                    adapter.add(ClassQuiz(item, vm, viewLifecycleOwner, code))
                this.adapter = adapter
            }

        })

        createQuizButton.setOnClickListener {
            it.findNavController().navigate(
                TeachFragmentDirections.editClassQuiz().setClassCode(code)
            )
            // val action = TeachFragmentDirections.editClassQuiz().setClassCode(code)

        }
        chatNowButton.setOnClickListener {
            it.findNavController().navigate(TeachFragmentDirections.chatNow(code))
        }

    }


    private fun setTitle(title: String) =
        with(activity as AppCompatActivity) {
            supportActionBar!!.title = title
        }
}
