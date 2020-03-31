package com.lemonlab.quizmaker


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject


class NotificationSender {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "quizzer",
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = context.getString(R.string.app_notifications_description)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        context: Context, notificationType: NotificationType,
        title: String, content: String, activity: Class<*>
    ) {
        val intent = Intent(context, activity)
        intent.putExtra("notificationType", notificationType)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val icon =
            if (notificationType == NotificationType.MESSAGE)
                R.drawable.ic_message
            else
                R.drawable.ic_person
        val builder = NotificationCompat.Builder(context, "quizzer")
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(100, builder)
    }

    fun sendNotification(context: Context, quizAuthor: String, notificationType: NotificationType) {

        val username = FirebaseAuth.getInstance().currentUser!!.displayName

        val json = JSONObject()
        val body = if (notificationType == NotificationType.MESSAGE)
            context.getString(R.string.newMessageFrom, username)
        else
            context.getString(R.string.newPointsForYou)

        val title = if (notificationType == NotificationType.MESSAGE)
            context.getString(R.string.newMessage)
        else
            context.getString(R.string.newPoints, quizAuthor)
        val requestQueue = Volley.newRequestQueue(context.applicationContext)
        try {
            val notificationObj = JSONObject()
            notificationObj.put("title", title)
            notificationObj.put("body", body)
            json.put("to", "/topics/$quizAuthor")
            json.put("data", notificationObj)

            val request = object : JsonObjectRequest(
                Method.POST, "https://fcm.googleapis.com/fcm/send",
                json,
                Response.Listener<JSONObject> { }, Response.ErrorListener { }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val header = HashMap<String, String>()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=AIzaSyDRt3oYGglckMbyVu-bRphVkDmh2hDgCSU"
                    return header
                }
            }
            requestQueue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

}
