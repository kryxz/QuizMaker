package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.lemonlab.quizmaker.items.ChatItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    private lateinit var vm: QuizzesVM
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = (activity as MainActivity).vm
        init()
    }

    private fun init() {
        val code = ChatFragmentArgs.fromBundle(arguments!!).code
        val adapter = GroupAdapter<ViewHolder>()

        var teach = vm.getName()


        vm.viewModelScope.launch {
            teach = vm.getTeachName(code)
        }

        chatViewModel.getMessages(code).observe(viewLifecycleOwner, Observer { list ->
            if (list == null || list.isEmpty()) return@Observer
            adapter.clear()
            for (item in list)
                adapter.add(ChatItem(item, teach))
            with(chatRV) {
                this.adapter = adapter
                smoothScrollToPosition(adapter.itemCount)
            }


        })

        sendMessageBtn.setOnClickListener {
            sendMessage(code)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.hideKeypad()
    }

    private fun sendMessage(code: String) {
        val text = messageText.text.toString().trim()
        if (text.isEmpty()) return
        messageText.text?.clear()

        val message = ChatMessage(
            text, vm.getName(), System.currentTimeMillis()
        )
        chatViewModel.sendMessage(code, message)

    }
}
