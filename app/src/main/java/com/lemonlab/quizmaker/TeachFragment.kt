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

        vm.getClassTitle(code).observe(viewLifecycleOwner, Observer { title ->
            setTitle(title)
        })
        val adapter = GroupAdapter<ViewHolder>()
        vm.getClassQuizzes(code).observe(viewLifecycleOwner, Observer { list ->
            if (list.isEmpty()) return@Observer
            with(classQuizzesRV) {
                layoutManager = LinearLayoutManager(context!!)
                classQuizzesProgressBar.visibility = View.GONE
                removeAllViews()
                adapter.clear()
                for (item in list)
                    adapter.add(ClassQuiz(item, vm, viewLifecycleOwner))
                this.adapter = adapter
            }

        })

        createQuizButton.setOnClickListener {
            it.findNavController().navigate(TeachFragmentDirections.createNewQuiz(code))
        }


    }


    private fun setTitle(title: String) =
        with(activity as AppCompatActivity) {
            supportActionBar!!.title = title
        }
}
