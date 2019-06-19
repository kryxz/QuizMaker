@file:Suppress("unused")

package com.lemonlab.quizmaker

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


enum class QuizType {
    MultipleChoice, TrueFalse
}

class TempData {
    companion object {
        //Data we pass between fragments
        var quizTitle = ""
        var isPasswordProtected = false
        var isOneTimeQuiz = false
        var questionsCount = 0
        var quizPin = "notRequired"
        var quizType: QuizType? = null

        //temporary data to make fragments smoother
        //This is used in the main fragment
        var currentQuizzes: List<Quiz>? = null

        //This is used in ViewEditQuestions
        var cachedQuestions: LinkedHashMap<String, MultipleChoiceQuestion>? = null


        //reassigns all variables to their defaults.
        fun resetData() {
            quizTitle = ""

            isPasswordProtected = false
            isOneTimeQuiz = false
            questionsCount = 0
            quizPin = "notRequired"
            quizType = null

            currentQuizzes = null
            cachedQuestions = null
        }

        fun deleteCached() {
            cachedQuestions = null
        }
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

data class Quiz(
    val quizTitle: String,
    val passwordProtected: Boolean,
    val oneTimeQuiz: Boolean,
    val questionsCount: Int,
    val quizPin: String,
    val quizType: QuizType?,
    val quizAuthor: String,
    val userUID:String
) {

    constructor() : this("", false, false, 0, "", null, "", "")
}
data class MultipleChoiceQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, MultipleChoiceQuestion>?
) {
    constructor() : this(null, null)
}

