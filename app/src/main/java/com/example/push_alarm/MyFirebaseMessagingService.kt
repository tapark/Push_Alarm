package com.example.push_alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        createNotificationChannel()

        val type = message.data["type"]?.let {
            NotificationType.valueOf(it)
        }
        val title = message.data["title"]
        val text = message.data["text"]

        type ?: return

        // 알림을 구현
        NotificationManagerCompat.from(this)
            .notify(type.id, createNotification(type, title, text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //oreo 버젼 이상 일 경우 채널생성
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION
            // 채널 생성완료
            // 채널을 NotificationManager에 추가
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // 알림을 구성(Build) 아이콘 제목 텍스트 중요도
    private fun createNotification(
        type: NotificationType,
        title: String?,
        text: String?
    ): Notification {

        // 수신알람 클릭시 동작을 정의
        // intent 를 통해 Activity를 호출
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notificationType", "${type.title}타입")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) // MainActivity 실행 중 재실행 시 기존화면을 갱신
        }

        // 제 3자에게 intent를 다룰 권한을 전달
        val pendingIntent = PendingIntent.getActivity(this, type.id, intent, FLAG_UPDATE_CURRENT)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.pet_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 클릭시 알림 사라짐

        when (type) {
            NotificationType.NORMAL -> Unit
            NotificationType.EXPANDABLE -> {
                notificationBuilder.setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            "나는 고양이야\n" +
                                    "너는 고양이야\n" +
                                    "나도 고양이야\n" +
                                    "너도 고양이야\n" +
                                    "단추 고양이야\n"
                        )
                )
            }
            NotificationType.CUSTOM -> {
                notificationBuilder
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(
                        RemoteViews(packageName, R.layout.view_custom_notification).apply {
                            setTextViewText(R.id.title, title)
                            setTextViewText(R.id.message, text)
                        }
                    )

            }
        }

        return notificationBuilder.build()
    }

    companion object {
        private const val CHANNEL_NAME = "Teddy's Alarm Test"
        private const val CHANNEL_DESCRIPTION = "Teddy's Alarm Test"
        private const val CHANNEL_ID = "Channel Id"
    }
}