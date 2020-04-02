
package com.lemonlab.quizmaker

import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

enum class QuizType(val id: Int) {
    MultipleChoice(R.string.mulChoice), TrueFalse(R.string.trueFalse)
}

enum class NotificationType {
    MESSAGE, QUIZ
}

enum class ViewType {
    ViewAnswers, InClass
}

data class TheClass(
    val teach: String,
    val title: String,
    val date: Long,
    val members: ArrayList<String>,
    val id: String
) {
    constructor() : this("", "", 0, ArrayList(), "")

    private fun addMember(name: String) =
        members.add(name)

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

abstract class Quizzer {

    abstract fun score(answers: SparseArray<*>): Int
    abstract fun getQuestion(pos: Int): String

    abstract fun getSize(): Int
    abstract fun getAuthor(): String
    abstract fun getTitle(): String
    abstract fun getID(): String

}

data class MultipleChoiceQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, MultipleChoiceQuestion>?
) : Quizzer() {
    constructor() : this(null, null)

    override fun getAuthor() = quiz!!.quizAuthor
    override fun getID() = quiz!!.quizUUID
    override fun getTitle() = quiz!!.quizTitle

    override fun score(answers: SparseArray<*>): Int {
        var score = 0
        answers.forEach { key, value ->
            if (questions!![key.toString()]!!.correctAnswer == value)
                score = score.inc()
        }
        return score
    }

    override fun getSize() = quiz!!.questionsCount

    override fun getQuestion(pos: Int) =
        questions!![pos.toString()]!!.question


    fun getChoiceOne(pos: Int) = questions!![pos.toString()]!!.first
    fun getChoiceTwo(pos: Int) = questions!![pos.toString()]!!.second
    fun getChoiceThree(pos: Int) = questions!![pos.toString()]!!.third
    fun getChoiceFour(pos: Int) = questions!![pos.toString()]!!.fourth


}

data class TrueFalseQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, TrueFalseQuestion>?
) : Quizzer() {
    constructor() : this(null, null)

    override fun getSize() = quiz!!.questionsCount

    override fun getAuthor() = quiz!!.quizAuthor
    override fun getID() = quiz!!.quizUUID
    override fun getTitle() = quiz!!.quizTitle

    override fun score(answers: SparseArray<*>): Int {
        var score = 0
        answers.forEach { key, value ->
            if (questions!![key.toString()]!!.answer == value)
                score = score.inc()
        }
        return score
    }

    override fun getQuestion(pos: Int) =
        questions!![pos.toString()]!!.question


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