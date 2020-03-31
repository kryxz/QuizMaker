@file:Suppress("unused")

package com.lemonlab.quizmaker

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


enum class QuizType {
    MultipleChoice, TrueFalse
}

enum class NotificationType {
    MESSAGE, QUIZ
}

enum class ViewType {
    TakeQuiz, ViewAnswers
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

    fun joinTimeAsAString(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = joinDate
        return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(
            Calendar.YEAR
        ).toString().substring(
            2,
            4
        )}"
    }
}

class TempData {
    companion object {
        //Data we pass between fragments
        var quizTitle = ""
        var isPasswordProtected = false
        var questionsCount = 0
        var quizPin = "notRequired"
        var quizType: QuizType? = null

        //temporary data to make fragments smoother
        //This is used in the main fragment
        var currentQuizzes: List<Quiz>? = null

        //This is used in ViewEditQuestions
        var multiChoiceCachedQuestions: LinkedHashMap<String, MultipleChoiceQuestion>? = null
        var trueFalseCachedQuestions: LinkedHashMap<String, TrueFalseQuestion>? = null

        //reassigns all variables to their defaults.
        fun resetData() {
            quizTitle = ""

            isPasswordProtected = false
            questionsCount = 0
            quizPin = "notRequired"
            quizType = null

            currentQuizzes = null
            multiChoiceCachedQuestions = null
            trueFalseCachedQuestions = null
        }

        fun deleteCached() {
            multiChoiceCachedQuestions = null
            trueFalseCachedQuestions = null
        }

        var user: User? = null
    }
}

data class MultipleChoiceQuestion(
    val question: String,
    val first: String, val second: String,
    val third: String, val fourth: String,
    val correctAnswer: Int
) {
    constructor() : this("", "", "", "", "", 0)

}

data class TrueFalseQuestion(
    val question: String,
    val answer: Boolean
) {
    constructor() : this("", false)
}


data class MultipleChoiceQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, MultipleChoiceQuestion>?
) {
    constructor() : this(null, null)
}

data class TrueFalseQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, TrueFalseQuestion>?
) {
    constructor() : this(null, null)
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

data class Quiz(
    val quizTitle: String,
    val passwordProtected: Boolean,
    val questionsCount: Int,
    val quizPin: String,
    val quizType: QuizType?,
    val quizAuthor: String,
    val quizUUID: String,
    var rating: Float,
    var totalRatingsCount: Int,
    val milliSeconds: Long
) {

    fun setNewRating(aNewRating: Float) {
        totalRatingsCount += 1
        rating += (aNewRating - rating) / totalRatingsCount

    }

    constructor() : this(
        "", false, 0,
        "", null, "", "", 0f, 0, 0
    )
}

data class Message(
    val sender: String,
    val message: String,
    val milliSeconds: Long,
    val id: String
) {
    constructor() : this("", "", 0, "")
}

data class Report(
    var userIDs: String,
    var count: Int
) {
    constructor() : this("", 0)

    fun report(id: String, quizID: String) {
        if (!userIDs.contains(id)) {
            userIDs += " $id"
            count = count.inc()
            if (count >= 5) {
                val dataRef = FirebaseFirestore.getInstance()
                dataRef.collection("Quizzes").document(quizID).delete()
                dataRef.collection("userReports").document(quizID).delete()
                TempData.currentQuizzes = null
            }

        }
    }
}

enum class Option {
    CACHE, FAQ, APPS, LOGOUT, ABOUT, PRIVACY, THEME, FEEDBACK
}

data class OptionsItem(
    val icon: Int,
    val text: String,
    val type: Option
)