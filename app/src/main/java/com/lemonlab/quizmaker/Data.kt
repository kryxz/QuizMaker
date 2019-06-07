package com.lemonlab.quizmaker


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
        var currentQuizzes: List<Quiz>? = null
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
    val quizAuthor: String
) {

    constructor() : this("", false, false, 0, "", null, "")
}

data class MultipleChoiceQuiz(
    val quiz: Quiz?,
    val questions: HashMap<String, MultipleChoiceQuestion>?
) {

    constructor() : this(null, null)
}

