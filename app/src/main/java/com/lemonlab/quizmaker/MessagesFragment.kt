package com.lemonlab.quizmaker


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.adapters.MessagesAdapter
import com.lemonlab.quizmaker.adapters.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.fragment_messages.*


class MessagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getMessages()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun noMessages() {
        if (view != null)
            with(noMessagesTextView) {
                visibility = View.VISIBLE
                MessagesProgressBar.visibility = View.GONE
            }
    }

    private fun getMessages() {
        if (view == null)
            return
        MessagesProgressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.displayName!!)
            .collection("messages").get().addOnSuccessListener { documents ->
                val listOfMessages = mutableListOf<Message>()
                for (item in documents)
                    listOfMessages.add(item.get("message", Message::class.java)!!)
                if (listOfMessages.isNotEmpty() && view != null)
                    with(MessagesRecyclerView) {

                        with(listOfMessages) {
                            sortWith(compareBy { it.milliSeconds })
                            reverse()
                        }
                        layoutManager = LinearLayoutManager(context!!)
                        adapter = MessagesAdapter(context!!, listOfMessages)
                        val swipeHandler = object : SwipeToDeleteCallback(context) {
                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                val adapter = MessagesRecyclerView.adapter as MessagesAdapter
                                adapter.deleteMessage(viewHolder.adapterPosition)
                                Handler().postDelayed(::getMessages, 1000)

                            }

                        }
                        val itemTouchHelper = ItemTouchHelper(swipeHandler)
                        itemTouchHelper.attachToRecyclerView(this)
                        MessagesProgressBar.visibility = View.GONE

                    } else
                    noMessages()
            }

    }

    override fun onDestroyView() {
        with(MessagesRecyclerView) {
            layoutManager = null
            adapter = null
            onFlingListener = null
        }
        //Hides keypad
        (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view!!.windowToken,
            0
        )
        super.onDestroyView()
    }
}
