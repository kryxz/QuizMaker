package com.lemonlab.quizmaker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lemonlab.quizmaker.ClassFragmentDirections
import com.lemonlab.quizmaker.R
import com.lemonlab.quizmaker.TheClass
import com.lemonlab.quizmaker.timeAsAString

class ClassVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val titleText = itemView.findViewById(R.id.classTitleText) as AppCompatTextView
    val instructorText = itemView.findViewById(R.id.classInstructorText) as AppCompatTextView
    val creationDateText = itemView.findViewById(R.id.classCreationDateText) as AppCompatTextView
    val enterBtn = itemView.findViewById(R.id.enterClass) as AppCompatButton

}


class ClassAdapter(
    private val context: Context, private val classes: List<TheClass>
) : RecyclerView.Adapter<ClassVH>() {


    override fun getItemCount() = classes.size


    override fun onBindViewHolder(holder: ClassVH, position: Int) {
        holder.titleText.text = classes[position].title
        holder.instructorText.text = classes[position].teach
        holder.creationDateText.text = classes[position].date.timeAsAString()

        holder.enterBtn.setOnClickListener {
            it.findNavController().navigate(ClassFragmentDirections.goToClass(classes[position].id))
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassVH {
        return ClassVH(
            LayoutInflater.from(context).inflate(
                R.layout.class_item,
                parent,
                false
            )
        )
    }
}