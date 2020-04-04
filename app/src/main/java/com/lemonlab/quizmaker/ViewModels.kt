package com.lemonlab.quizmaker

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


class FireRepo {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getQuizzesRef() = db.collection("Quizzes")

    fun getQuizRef(quizID: String) = getQuizzesRef().document(quizID)

    fun getClassQuiz(id: String, quizID: String) =
        getClassRef(id).collection("Quizzes").document(quizID)

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

    private val repo = FireRepo()

    private val allQuizzes: MutableLiveData<List<Quiz>> = MutableLiveData()

    fun delQuiz(classCode: String, id: String) {

        val isClass = id.substring(0, 5) == "class"

        val ref = if (isClass)
            repo.getClassRef(classCode).collection("Quizzes")
        else
            repo.getQuizzesRef()

        // remove from user's data
        repo.getUsersRef().document(getName())
            .collection("Quizzes").document(id).delete()

        ref.document(id).delete()
    }

    fun sendQuiz(id: String, quiz: HashMap<String, Quizzer>, classCode: String) {
        val isClass = id.substring(0, 5) == "class"

        val ref = if (isClass)
            repo.getClassRef(classCode).collection("Quizzes")
        else
            repo.getQuizzesRef()

        // add to user's data
        repo.getUsersRef().document(getName())
            .collection("Quizzes").document(id).set(mapOf(id to "id"))

        ref.document(id).set(quiz)

    }

    fun updateQuiz(classCode: String, id: String, quiz: Quizzer) {
        val isClass = classCode != "empty"

        val map = HashMap<String, Quizzer>().apply {
            this["quiz"] = quiz
        }

        val ref = if (isClass)
            repo.getClassRef(classCode).collection("Quizzes")
        else
            repo.getQuizzesRef()

        ref.document(id).set(map)


    }

    fun leaveClass(that: TheClass) {
        if (!that.members.contains(getName())) return

        that.members.remove(getName())
        if (that.members.isEmpty())
            repo.getClassesRef().document(that.id).delete()
        else
            repo.getClassesRef().document(that.id).set(that)
        repo.getUsersRef().document(repo.getUserName())
            .collection("class").document(that.id).delete()
    }

    fun joinClassWithCode(context: Context, code: String) {

        fun couldNotJoinMessage() =
            Toast.makeText(
                context,
                context.getString(R.string.classNotFound), Toast.LENGTH_SHORT
            ).show()

        repo.getClassRef(code).get().addOnSuccessListener {

            if (it == null || it.data == null) {
                couldNotJoinMessage()
                return@addOnSuccessListener

            }
            val that = it.toObject(TheClass::class.java) ?: TheClass()
            if (that.date == 0L) {
                couldNotJoinMessage()
                return@addOnSuccessListener
            }
            joinClass(that)


        }
    }

    fun joinClass(that: TheClass) {
        if (that.members.contains(getName())) return

        that.members.add(getName())
        repo.getClassesRef().document(that.id).set(that)
        repo.getUsersRef().document(repo.getUserName())
            .collection("class").document(that.id).set(mapOf("id" to that.id))

    }

    fun getQuiz(classCode: String, id: String): MutableLiveData<Quizzer> {
        val currentQuiz = MutableLiveData<Quizzer>()

        val isClass = id.substring(0, 5) == "class"

        val ref = if (isClass)
            repo.getClassQuiz(classCode, id)
        else
            repo.getQuizRef(id)


        ref.get().addOnSuccessListener { document ->
            if (document.data == null || document == null) {
                currentQuiz.value = null
                return@addOnSuccessListener
            }

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


    fun addClass(that: TheClass) {
        that.members.add(getName())
        repo.getClassesRef().document(that.id).set(that)
        repo.getUsersRef().document(getName())
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
        return (repo.getUserLog(getName()).get().await()
            .get("log", QuizLog::class.java)?.userLog?.contains(id))?.not() ?: true
    }

    fun canRate(id: String): LiveData<Boolean> {
        val bool: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch {
            bool.value = shouldRate(id)
        }
        return bool
    }

    fun rateQuiz(id: String, rating: Float, classCode: String) {
        val isClass = id.substring(0, 5) == "class"

        val ref = if (isClass)
            repo.getClassQuiz(classCode, id)
        else
            repo.getQuizRef(id)

        ref.get().addOnSuccessListener { document ->
            val quiz = document.get("quiz.quiz", Quiz::class.java)!!
            quiz.setNewRating(rating)
            ref.update("quiz.quiz", quiz)
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
        val classQuizzes: MutableLiveData<List<Quiz>> = MutableLiveData()
        repo.getClassRef(id).collection("Quizzes").get().addOnSuccessListener { data ->
            if (data == null || data.isEmpty) {
                classQuizzes.value = null
                return@addOnSuccessListener
            }
            val listOfQuizzes = mutableListOf<Quiz>()
            for (item in data.documents)
                listOfQuizzes.add(item.get("quiz.quiz", Quiz::class.java)!!)

            with(listOfQuizzes) {
                sortWith(compareBy { it.milliSeconds })
                reverse()
            }
            classQuizzes.value = listOfQuizzes


        }

        return classQuizzes
    }

    suspend fun getTeachName(id: String): String {
        return repo.getClassRef(id).get().await().get("teach", String::class.java)!!

    }

    fun getClass(id: String): MutableLiveData<TheClass> {
        val thatClass: MutableLiveData<TheClass> = MutableLiveData()
        viewModelScope.launch {
            thatClass.value = repo.getClassRef(id).get().await().toObject(TheClass::class.java)
        }

        return thatClass
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


    fun getPublicClasses(): MutableLiveData<List<TheClass>> {
        val items: MutableLiveData<List<TheClass>> = MutableLiveData()
        repo.getClassesRef().get().addOnSuccessListener { snapShot ->
            if (snapShot == null || snapShot.isEmpty) {
                items.value = null
                return@addOnSuccessListener
            }
            val classes = ArrayList<TheClass>()
            for (item in snapShot) {
                val c = item.toObject(TheClass::class.java)
                if (c.open && getName() !in c.members)
                    classes.add(c)
            }
            classes.sortBy {
                it.date
            }
            items.value = classes


        }
        return items
    }

    fun getClasses(): MutableLiveData<List<TheClass>> {
        val allClasses: MutableLiveData<List<TheClass>> = MutableLiveData()
        val classes = ArrayList<TheClass>()

        repo.getUsersRef().document(repo.getUserName()).collection("class")
            .get().addOnSuccessListener { snapShot ->
                if (snapShot == null || snapShot.isEmpty) {
                    allClasses.value = null
                    return@addOnSuccessListener
                }
                for (item in snapShot.documents) {
                    val id = item.get("id").toString()
                    repo.getClassRef(id).get().addOnSuccessListener {
                        val classItem = it.toObject(TheClass::class.java) ?: TheClass()
                        if (classItem.title != "")
                            classes.add(classItem)
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

    companion object {
        private const val QUIZ_TYPE = "QUIZ_TYPE"

        private const val SIZE = "SIZE"
        private const val HAS_PIN = "HAS_PIN"
        private const val PIN = "PIN"

        private const val TITLE = "TITLE"
        private const val CLASS_JOIN_CODE = "CLASS_JOIN_CODE"

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

    fun setClassJoinCode(code: String) =
        savedStateHandle.set(CLASS_JOIN_CODE, code)

    fun getClassJoinCode(): String =
        savedStateHandle.get(CLASS_JOIN_CODE) ?: "empty"

    fun getQuizType(): QuizType =
        savedStateHandle.get(QUIZ_TYPE) ?: QuizType.TrueFalse

    fun setQuizType(type: QuizType) =
        savedStateHandle.set(QUIZ_TYPE, type)

    fun getMultiChoice(): LinkedHashMap<String, MultipleChoiceQuestion> =
        multiChoice

    fun getTrueFalse(): LinkedHashMap<String, TrueFalseQuestion> =
        trueFalse


    fun setPin(pin: String) = savedStateHandle.set(PIN, pin)
    fun getPin(): String = savedStateHandle.get(PIN) ?: ""

    fun hasPin(): Boolean = savedStateHandle.get(HAS_PIN) ?: false
    fun setHasPin(has: Boolean) = savedStateHandle.set(HAS_PIN, has)

    fun setTitle(title: String) = savedStateHandle.set(TITLE, title)
    fun getTitle(): String = savedStateHandle.get(TITLE) ?: ""

    fun setSize(size: Int) = savedStateHandle.set(SIZE, size)
    fun getSize() = savedStateHandle.get(SIZE) ?: 0

    fun removeAll() {
        val keys = savedStateHandle.keys()
        for (key in keys)
            savedStateHandle.set(key, null)
        multiChoice.clear()
        trueFalse.clear()
    }


}