package com.lemonlab.quizmaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class ChatMessage(
    var text: String, var username: String,
    var time: Long
) {
    constructor() : this("", "", 0)
}


class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FireRepo()
    private val messages: MutableLiveData<List<ChatMessage>> = MutableLiveData()

    fun sendMessage(code: String, msg: ChatMessage) {
        repo.getClassRef(code).collection("chat").add(msg)
    }


    fun getMessages(code: String): LiveData<List<ChatMessage>> {

        repo.getClassRef(code).collection("chat")
            .addSnapshotListener { query, e ->
                if (e != null) return@addSnapshotListener
                if (query == null || query.isEmpty) return@addSnapshotListener
                val msgArray = ArrayList<ChatMessage>()
                for (item in query)
                    msgArray.add(item.toObject(ChatMessage::class.java))
                msgArray.sortBy {
                    it.time
                }
                messages.value = msgArray
            }
        return messages
    }
}