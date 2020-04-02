package com.lemonlab.quizmaker

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


class FireRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getQuizzesRef() = db.collection("Quizzes")

    fun getQuizRef(quizID: String) = getQuizzesRef().document(quizID)

    private fun getClassQuizzesRef() = db.collection("classQuizzes")

    fun getClassQuiz(quizID: String) = getClassQuizzesRef().document(quizID)

    fun getClassRef(id: String) = db.collection("class").document(id)

    fun getUsersRef() = db.collection("users")

    fun getClassesRef() = db.collection("class")


    fun getFeedbackRef() = db.collection("feedback")

    fun getUserLog(name: String) = getUsersRef()
        .document(name)
        .collection("userData").document("taken")

    fun getUserName() = auth.currentUser?.displayName ?: ""

    fun getAuth() = auth

}

class QuizzesVM(application: Application) : AndroidViewModel(application) {

    private val repo: FireRepo = FireRepo()

    private val allQuizzes: MutableLiveData<List<Quiz>> = MutableLiveData()
    private val classQuizzes: MutableLiveData<List<Quiz>> = MutableLiveData()
    private val allClasses: MutableLiveData<List<TheClass>> = MutableLiveData()

    private val currentQuiz: MutableLiveData<Quizzer> = MutableLiveData()

    fun getQuiz(id: String, isClass: Boolean): MutableLiveData<Quizzer> {
        val ref = if (isClass)
            repo.getClassQuiz(id)
        else
            repo.getQuizRef(id)

        ref.get().addOnSuccessListener { document ->
            if (document == null) return@addOnSuccessListener
            val quiz = document.get("quiz.quiz", Quiz::class.java)!!

            if (quiz.quizType == QuizType.MultipleChoice)
                currentQuiz.value = document.get("quiz", MultipleChoiceQuiz::class.java)!!
            else
                currentQuiz.value = document.get("quiz", TrueFalseQuiz::class.java)!!
        }

        return currentQuiz
    }


    fun isLoggedIn(): Boolean {
        return repo.getAuth().currentUser != null
    }


    suspend fun getClass(ref: DocumentReference): TheClass {
        return withContext(Dispatchers.IO) {
            ref.get().await().toObject(TheClass::class.java)!!
        }
    }

    private suspend fun getQuiz(ref: DocumentReference): Quiz {
        return withContext(Dispatchers.IO) {
            ref.get().await().get("quiz.quiz", Quiz::class.java)!!
        }
    }


    fun addClass(that: TheClass) {
        repo.getClassesRef().document(that.id).set(that)
        repo.getUsersRef().document(repo.getUserName())
            .collection("class").add(hashMapOf("id" to that.id))
    }

    fun sendMessage(context: Context, to: String, what: String) {
        val msg = HashMap<String, Message>(1)
        val id = UUID.randomUUID().toString().substring(0, 8)
        msg["message"] = Message(getName(), what, Calendar.getInstance().timeInMillis, id)
        repo.getUsersRef().document(to).collection("messages").document(id).set(msg)
        NotificationSender().sendNotification(
            context,
            to,
            NotificationType.MESSAGE
        )

    }

    private suspend fun shouldRate(id: String): Boolean {
        return repo.getUserLog(getName()).get().await()
            .get("log", QuizLog::class.java)!!.userLog.contains(id)
    }

    fun canRate(id: String): LiveData<Boolean> {
        val bool: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch {
            bool.value = shouldRate(id)
        }
        return bool
    }

    fun rateQuiz(id: String, isClass: Boolean, rating: Float) {
        val ref = if (isClass)
            repo.getClassQuiz(id)
        else
            repo.getQuizRef(id)

        ref.get().addOnSuccessListener { document ->
            val theQuiz = document.get("quiz.quiz", Quiz::class.java)!!
            theQuiz.setNewRating(rating)
            ref.update("quiz.quiz", theQuiz)
        }
    }

    fun logQuiz(id: String, score: Int, author: String, total: Int, context: Context) {
        repo.getUserLog(getName()).get().addOnSuccessListener { data ->
            var quizzesLog = QuizLog(mutableListOf())
            if (data != null)
                quizzesLog = if (data.get("log", QuizLog::class.java) != null)
                    data.get("log", QuizLog::class.java)!!
                else
                    QuizLog(mutableListOf())

            val log = HashMap<String, QuizLog>(1)
            quizzesLog.addQuiz(
                id,
                getName(),
                score,
                author,
                total,
                context
            )
            log["log"] = quizzesLog
            repo.getUserLog(getName()).set(log)
        }
    }

    fun signOut() = repo.getAuth().signOut()
    fun getName() = repo.getUserName()

    fun sendFeedback(text: String) {
        val feedback = HashMap<String, Pair<String, String>>()
        feedback["feedback"] = Pair(text, getName())
        repo.getFeedbackRef().add(feedback)
    }

    fun getClassQuizzes(id: String): MutableLiveData<List<Quiz>> {
        repo.getClassRef(id).collection("Quizzes").get().addOnSuccessListener { data ->
            if (data.isEmpty) return@addOnSuccessListener
            val listOfQuizzes = mutableListOf<Quiz>()
            for (item in data.documents) {
                val quizCode = item.get("id").toString()
                viewModelScope.launch {
                    val quiz =
                        getQuiz(repo.getClassQuiz(quizCode))
                    listOfQuizzes.add(quiz)

                    classQuizzes.value = listOfQuizzes
                }
                with(listOfQuizzes) {
                    sortWith(compareBy { it.milliSeconds })
                    reverse()
                }
                classQuizzes.value = listOfQuizzes
            }

        }

        return classQuizzes
    }


    fun getClassTitle(id: String): MutableLiveData<String> {
        val title: MutableLiveData<String> = MutableLiveData()
        viewModelScope.launch {
            title.value = repo.getClassRef(id).get().await()
                .get("title", String::class.java).toString()
        }

        return title
    }


    fun getUser(name: String): LiveData<User> {
        val user: MutableLiveData<User> = MutableLiveData()
        viewModelScope.launch {
            user.value = repo.getUsersRef()
                .document(name).get().await().get("user", User::class.java)
        }

        return user
    }


    fun updateBio(update: String) {
        repo.getUsersRef().document(getName()).update("user.userBio", update)

    }

    fun getLog(name: String): LiveData<QuizLog> {
        val log: MutableLiveData<QuizLog> = MutableLiveData()
        viewModelScope.launch {
            val qLog = repo.getUserLog(name).get().await().get("log", QuizLog::class.java)
                ?: QuizLog(mutableListOf())
            log.value = qLog

        }
        return log
    }

    fun cancelNotifications() =
        FirebaseMessaging.getInstance().unsubscribeFromTopic(getName())

    fun getClasses(): MutableLiveData<List<TheClass>> {
        repo.getUsersRef().document(repo.getUserName()).collection("class")
            .get().addOnSuccessListener { snapShot ->
                val classes = ArrayList<TheClass>()
                for (item in snapShot.documents) {
                    val id = item.get("id").toString()
                    viewModelScope.launch {
                        val thatClass =
                            getClass(repo.getClassRef(id))

                        with(classes) {
                            sortWith(compareBy { it.date })
                            reverse()
                        }
                        classes.add(thatClass)
                        allClasses.value = classes
                    }
                }
            }
        return allClasses
    }

    fun getAllQuizzes(): MutableLiveData<List<Quiz>> {

        repo.getQuizzesRef().addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot == null) return@addSnapshotListener
            if (snapshot.isEmpty) return@addSnapshotListener

            val quizzes = ArrayList<Quiz>()
            for (item in snapshot)
                quizzes.add(item.get("quiz.quiz", Quiz::class.java)!!)

            with(quizzes) {
                sortWith(compareBy { it.milliSeconds })
                reverse()
            }
            allQuizzes.value = quizzes

        }

        return allQuizzes
    }


}


class QuestionsVM(state: SavedStateHandle) : ViewModel() {

    // Keep the key as a constant
    companion object {
        private const val QUIZ_TYPE = "QUIZ_TYPE"

        private const val SIZE = "SIZE"
        private const val HAS_PIN = "HAS_PIN"
        private const val PIN = "PIN"

        private const val TITLE = "TITLE"

        private const val MULTIPLE_CHOICE = "MULTIPLE_CHOICE"

        private const val TRUE_FALSE = "TRUE_FALSE"

    }

    private val savedStateHandle = state
    private val multiChoice: LinkedHashMap<String, MultipleChoiceQuestion> = LinkedHashMap()
    private val trueFalse: LinkedHashMap<String, TrueFalseQuestion> = LinkedHashMap()


    fun setTFQuestion(position: Int, q: TrueFalseQuestion) {
        trueFalse[position.toString()] = q
    }

    fun getTFQuestion(position: Int): TrueFalseQuestion =
        trueFalse[position.toString()] ?: TrueFalseQuestion()


    fun setMultiChoiceQuestion(position: Int, q: MultipleChoiceQuestion) {
        multiChoice[position.toString()] = q
    }


    fun getMultiChoiceQuestion(position: Int): MultipleChoiceQuestion =
        multiChoice[position.toString()] ?: MultipleChoiceQuestion()


    fun saveQuizType(quizType: QuizType) =
        savedStateHandle.set(QUIZ_TYPE, quizType)


    fun getQuizType(): QuizType =
        savedStateHandle.get(QUIZ_TYPE) ?: QuizType.TrueFalse


    fun saveMultiChoice() =
        savedStateHandle.set(MULTIPLE_CHOICE, multiChoice)


    fun saveTrueFalse() =
        savedStateHandle.set(TRUE_FALSE, trueFalse)


    fun getMultiChoice(): LinkedHashMap<String, MultipleChoiceQuestion> =
        savedStateHandle.get(MULTIPLE_CHOICE) ?: LinkedHashMap()


    fun getTrueFalse(): LinkedHashMap<String, TrueFalseQuiz> =
        savedStateHandle.get(TRUE_FALSE) ?: LinkedHashMap()


    fun setPin(pin: String) = savedStateHandle.set(PIN, pin)
    fun getPin(): String = savedStateHandle.get(PIN) ?: ""

    fun hasPin(): Boolean = savedStateHandle.get(HAS_PIN) ?: false
    fun setHasPin(has: Boolean) = savedStateHandle.set(HAS_PIN, has)

    fun setTitle(title: String) = savedStateHandle.set(TITLE, title)
    fun getTitle(): String = savedStateHandle.get(TITLE) ?: ""

    fun setSize(size: Int) = savedStateHandle.set(SIZE, size)
    fun getSize() = savedStateHandle.get(SIZE) ?: 1


}