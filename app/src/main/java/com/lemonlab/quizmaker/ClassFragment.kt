package com.lemonlab.quizmaker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.adapters.ClassAdapter
import kotlinx.android.synthetic.main.fragment_class.*
import java.util.*
import kotlin.collections.ArrayList


class ClassFragment : Fragment() {

    private lateinit var vm: QuizzesVM
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        init()
    }


    private fun createClass(thatClass: TheClass) =
        vm.addClass(thatClass)


    private fun init() {
        classProgressBar.visibility = View.VISIBLE

        vm.getClasses().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isEmpty() || it == null) return@Observer

            with(classesRV) {
                layoutManager = LinearLayoutManager(context!!)
                adapter = ClassAdapter(context!!, it)
            }
            classProgressBar.visibility = View.GONE
        })

        createClassBtn.setOnClickListener {
            showClassDialog()
        }


    }

    private fun showClassDialog() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.create_class_dialog,
                null
            )
        }

        val titleText = dialogView.findViewById<TextInputEditText>(R.id.classNameText)
        val teachText = dialogView.findViewById<TextInputEditText>(R.id.classInstructorText)
        val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmClass)
        val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelClass)

        teachText.setText(vm.getName())

        cancelButton.setOnClickListener { dialogBuilder.dismiss() }
        confirmButton.setOnClickListener {
            val title = titleText.text.toString()
            val teach = teachText.text.toString()
            if (title.isEmpty()) return@setOnClickListener

            val id = vm.getName() + UUID.randomUUID().toString().substring(0, 8).replace("-", "")
            val theClass = TheClass(teach, title, System.currentTimeMillis(), ArrayList(), id)
            createClass(theClass)
            classProgressBar.visibility = View.VISIBLE
            dialogBuilder.dismiss()
        }

        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

}
