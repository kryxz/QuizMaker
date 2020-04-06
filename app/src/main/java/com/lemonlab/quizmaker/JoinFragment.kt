package com.lemonlab.quizmaker

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.lemonlab.quizmaker.items.PublicClassItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_join.*


class JoinFragment : Fragment() {

    private lateinit var vm: QuizzesVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        init()
    }

    private fun init() {
        val code = JoinFragmentArgs.fromBundle(arguments!!).joinCode
        if (code != "empty")
            joinClassViaCode(code)

        allClassesProgressBar.visibility = View.VISIBLE
        val adapter = GroupAdapter<ViewHolder>()

        fun joinClass(that: TheClass) {
            vm.joinClass(that)
        }

        vm.getPublicClasses().observe(viewLifecycleOwner, Observer { list ->
            allClassesProgressBar.visibility = View.GONE
            if (list == null || list.isEmpty()) {
                noOpenClasses.visibility = View.VISIBLE
                return@Observer

            }
            with(allClassesRV) {
                visibility = View.VISIBLE
                removeAllViews()
                adapter.clear()
                for (item in list)
                    adapter.add(PublicClassItem(item, ::joinClass))
                this.adapter = adapter
            }
            noOpenClasses.visibility = if (adapter.itemCount == 0)
                View.VISIBLE
            else View.GONE

        })
        joinClassViaCode.setOnClickListener {
            showJoinDialog()
        }
    }

    private fun showJoinDialog() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.enter_class_code_dialog,
                null
            )
        }
        with(dialogView) {
            val input = findViewById<TextInputEditText>(R.id.classCodeInput)

            val clipboard: ClipboardManager =
                context!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val copied = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context!!)
                .toString()

            if (copied.contains('-') && copied.length == 11) {
                input.setText(copied)
                context.showToast(getString(R.string.contentPasted))
            }

            input.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null && s.length in listOf(2, 5, 8))
                        input.text!!.append('-')
                }
            })
            val confirm = findViewById<AppCompatButton>(R.id.joinViaCodeConfirm)
            val cancel = findViewById<AppCompatButton>(R.id.joinViaCodeCancel)

            cancel.setOnClickListener {
                dialogBuilder.dismiss()
            }

            confirm.setOnClickListener {
                val text = input.text.toString().trim().replace(".", "")
                if (text.isEmpty()) return@setOnClickListener
                joinClassViaCode(text)
                dialogBuilder.dismiss()
            }


        }
        with(dialogBuilder) {
            setView(dialogView)
            show()
        }

    }

    private fun joinClassViaCode(code: String) {
        allClassesProgressBar.visibility = View.VISIBLE
        vm.joinClassWithCode(context!!, code)
        Handler().postDelayed({
            if (view != null)
                view!!.findNavController().popBackStack()
        }, 1000)
    }
}
