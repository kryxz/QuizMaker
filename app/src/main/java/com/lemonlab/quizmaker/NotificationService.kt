package com.lemonlab.quizmaker

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        val title = p0.data["title"].toString()
        val message = p0.data["body"].toString()

        val notificationType =
            if (title==getString(R.string.newMessage))
                NotificationType.MESSAGE
            else
                NotificationType.QUIZ

        NotificationSender().createNotificationChannel(this)

        NotificationSender().createNotification(this, notificationType, title, message, MainActivity::class.java)


        super.onMessageReceived(p0)
    }

}
