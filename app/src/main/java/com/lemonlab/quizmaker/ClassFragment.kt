package com.lemonlab.quizmaker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.items.ClassItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_class.*


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


    private fun createClass(thatClass: TheClass) {
        vm.addClass(thatClass)
        classProgressBar.visibility = View.GONE

    }


    private fun init() {
        classProgressBar.visibility = View.VISIBLE

        val adapter = GroupAdapter<ViewHolder>()
        fun leaveClass(that: TheClass, whatGroup: ClassItem) {
            context!!.showYesNoDialog({
                vm.leaveClass(that)
                adapter.remove(whatGroup)
                adapter.notifyDataSetChanged()
                noClasses.visibility = if (adapter.itemCount == 0)
                    View.VISIBLE
                else View.GONE
            }, {},
                getString(R.string.leaveClass),
                getString(R.string.leaveClassConfirm)
            )
        }
        vm.getClasses().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            classProgressBar.visibility = View.GONE
            if (it == null || it.isEmpty()) {
                return@Observer
            }
            adapter.clear()
            val name = vm.getName()
            it.also { items ->
                for (item in items)
                    adapter.add(ClassItem(item, ::leaveClass, name))
            }
            with(classesRV) {
                removeAllViews()
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context!!)
                this.adapter = adapter
            }
            noClasses.visibility = if (adapter.itemCount == 0)
                View.VISIBLE
            else View.GONE


        })

        createClassBtn.setOnClickListener {
            showClassDialog()
        }
        joinClassBtn.setOnClickListener {
            it.findNavController().navigate(ClassFragmentDirections.goToJoin())
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
        val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.confirmClass)
        val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelClass)

        val isClassOpen = dialogView.findViewById<AppCompatCheckBox>(R.id.isClassOpen)
        val helpText = dialogView.findViewById<AppCompatTextView>(R.id.createClassHelpText)
        var isOpen = false

        isClassOpen.setOnCheckedChangeListener { _, isChecked ->
            isOpen = isChecked

            helpText.text = if (isChecked)
                getString(R.string.createClassOpen)
            else
                getString(R.string.createClassClosed)


        }

        cancelButton.setOnClickListener { dialogBuilder.dismiss() }
        confirmButton.setOnClickListener {
            val title = titleText.text.toString()
            if (title.isEmpty()) return@setOnClickListener
            classProgressBar.visibility = View.VISIBLE

            val idGen = StringBuilder()

            for (i in 1..10) {
                if (i % 4 == 0) idGen.append('-')
                idGen.append(i.toString())
            }
            val id = idGen.toString()
            val teach = vm.getName()


            val theClass =
                TheClass(teach, title, System.currentTimeMillis(), ArrayList(), id, isOpen)
            createClass(theClass)
            dialogBuilder.dismiss()
        }

        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

}
