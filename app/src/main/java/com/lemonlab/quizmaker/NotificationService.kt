package com.lemonlab.quizmaker

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class NotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage?) {
        var title = p0!!.notification!!.title.toString()
        val message = p0.notification!!.body.toString()
        val notificationType =
            if (title.contains("MESSAGE"))
                NotificationType.MESSAGE
            else
                NotificationType.QUIZ

        title = title.substringAfter("MESSAGE").substringAfter("QUIZ")
        NotificationSender().createNotification(
            this,
            notificationType,
            title,
            message,
            MainActivity::class.java
        )
        NotificationSender().createNotificationChannel(this)

        super.onMessageReceived(p0)

    }
}
