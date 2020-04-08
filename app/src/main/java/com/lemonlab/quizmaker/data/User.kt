package com.lemonlab.quizmaker.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.quizmaker.NotificationSender
import com.lemonlab.quizmaker.timeAsAString

enum class NotificationType {
    MESSAGE, QUIZ
}

data class User(
    var username: String,
    val email: String,
    var id: String,
    var userBio: String,
    var points: Int,
    val joinDate: Long
) {
    constructor() : this("", "", "", "", 0, 0)

    fun joinTimeAsAString() = joinDate.timeAsAString()

}


data class TheClass(
    val teach: String,
    val title: String,
    val date: Long,
    val members: ArrayList<String>,
    val id: String,
    val open: Boolean
) {
    constructor() : this("", "", 0, ArrayList(), "", false)


}


data class Message(
    val sender: String,
    val message: String,
    val milliSeconds: Long,
    val id: String
) {
    constructor() : this("", "", 0, "")
}


data class QuizLog(
    val userLog: MutableList<String>
) {
    fun addQuiz(
        quizUUID: String,
        userName: String,
        pointsToGet: Int,
        quizAuthor: String,
        total: Int,
        context: Context
    ) {
        if (!userLog.contains(quizUUID)) {
            userLog.add(quizUUID)
            val usersRef = FirebaseFirestore.getInstance().collection("users")
            usersRef.document(userName).get().addOnSuccessListener { document ->
                var points = document.get("user.points", Int::class.java)!!
                points += pointsToGet
                usersRef.document(userName).update("user.points", points)
                usersRef.document(quizAuthor).get().addOnSuccessListener { doc ->
                    val authorPoints = doc.get("user.points", Int::class.java)!! + total
                    usersRef.document(quizAuthor).update("user.points", authorPoints)
                    if (userName != quizAuthor)
                        NotificationSender().sendNotification(
                            context,
                            quizAuthor,
                            NotificationType.QUIZ
                        )
                }
            }

        }
    }

    constructor() : this(mutableListOf())
}


