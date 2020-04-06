package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lemonlab.quizmaker.items.ClassQuiz
import com.lemonlab.quizmaker.items.MemberItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_teach.*


class TeachFragment : Fragment() {

    private lateinit var vm: QuizzesVM

    private lateinit var code: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_teach, container, false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.teach_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.showMembers)
            participantsDialog()

        return super.onOptionsItemSelected(item)
    }

    private fun participantsDialog() {
        val dialogBuilder = AlertDialog.Builder(context!!).create()
        val dialogView = with(LayoutInflater.from(context)) {
            inflate(
                R.layout.members_dialog,
                null
            )
        }
        with(dialogView) {
            val rv = findViewById<RecyclerView>(R.id.membersRV)
            val membersCount = findViewById<AppCompatTextView>(R.id.membersCount)
            val ok = findViewById<AppCompatButton>(R.id.okButton)
            ok.setOnClickListener {
                dialogBuilder.dismiss()
            }

            val adapter = GroupAdapter<ViewHolder>()
            fun initRV() {
                vm.getClass(code).observe(viewLifecycleOwner, Observer { that ->
                    if (that == null) {
                        dialogBuilder.dismiss()
                        view!!.findNavController().popBackStack()
                        return@Observer
                    }

                    val members = that.members

                    membersCount.text = getString(R.string.membersCount, members.size)

                    adapter.clear()
                    for (item in members)
                        adapter.add(MemberItem(item, that, vm, ::initRV))

                    rv.adapter = adapter
                })
            }
            initRV()
        }

        with(dialogBuilder) {
            setView(dialogView)
            show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm = (activity as MainActivity).vm
        code = TeachFragmentArgs.fromBundle(arguments!!).classCode
        init()
        super.onViewCreated(view, savedInstanceState)
    }


    private fun init() {
        classQuizzesProgressBar.visibility = View.VISIBLE

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
