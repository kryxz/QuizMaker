package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.faq_item.view.*
import kotlinx.android.synthetic.main.fragment_faq.*


class FaqFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        faqAdBanner.loadAd()
        val adapter = GroupAdapter<ViewHolder>()
        val arrayOfFAQs = resources.getStringArray(R.array.faqs)
        val arrayOfAnswers = resources.getStringArray(R.array.faqsAnswers)

        for ((index) in arrayOfFAQs.withIndex())
            adapter.add(FAQItem(arrayOfFAQs[index], arrayOfAnswers[index]))

        faqRV.adapter = adapter
    }
}


class FAQItem(
    private var questionText: String,
    private var questionAnswer: String
) : Item<ViewHolder>() {
    override fun getLayout() = R.layout.faq_item

    override fun bind(viewHolder: ViewHolder, position: Int) {
        loadTexts(viewHolder.itemView.faqQuestion, viewHolder.itemView.faqAnswer)
    }

    private fun loadTexts(questionTextView: AppCompatTextView, answerTextView: AppCompatTextView) {
        questionTextView.text = questionText
        questionTextView.setOnClickListener {
            answerTextView.text = questionAnswer
            answerTextView.visibility = View.VISIBLE
        }

        answerTextView.setOnClickListener {
            answerTextView.visibility = View.GONE
        }

    }
}