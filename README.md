# 푸시알람

### Firebase Cloud Messaging 초기설정
Firebase 프로젝트 생성 : https://console.firebase.google.com/
google_services.json 파일 다운로드 -> app 디렉토리에 저장
~~~kotlin
// in build.gradle
applicationId : "[ID]" // 프로젝트에 입력
implementation 'com.google.firebase:firebase-messaging-ktx' // 추가
~~~

### Cloud Messaging Background vs Foreground
Background 상태에서는 Firebase 단에서 메세지를 알림  
Foreground 상태에서는 직접 정의 해줘야 함 (onMessageReceived)
~~~kotlin
// create MyFirebaseMessagingService.kt
// FirebaseMessagingService() 를 상속
class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}

// in AndroidMenifest.xml
// MyFirebaseMessagingService가 com.google.firebase.MESSAGING_EVENT를 수신
<service android:name=".MyFirebaseMessagingService"
	android:exported="false"> // 외부와 공유 false
	<intent-filter>
		<action android:name="com.google.firebase.MESSAGING_EVENT"/>
	</intent-filter>
</service>
~~~

### Notification Channel 생성
~~~kotlin
// in MyFirebaseMessagingService.kt
class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
		// 메세지가 수신될 경우 채널을 생성
        createNotificationChannel()

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
            // 채널 설정완료
            // 채널을 NotificationManager에 추가
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
			// 채널 생성완료
        }
    }

    companion object {
        private const val CHANNEL_NAME = "Teddy's Alarm Test"
        private const val CHANNEL_DESCRIPTION = "gg"
        private const val CHANNEL_ID = "Channel Id"
    }
}
~~~

### Get Value from Cloud Messaging API
~~~kotlin
// in MyFirebaseMessagingService.kt
override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)

    createNotificationChannel() // 채널생성

    // key-value data를 key(type, title, text)값 기준으로 get
    val type = message.data["type"]?.let {
        NotificationType.valueOf(it)
    }
    val title = message.data["title"]
    val text = message.data["text"]
    type ?: return

    // Notification(알림)을 생성
    NotificationManagerCompat.from(this)
        .notify(type.id, createNotification(type, title, text))
    }
    // createNotification 함수 : Notification Type
~~~

### Notification 생성
~~~kotlin
private fun createNotification(
        type: NotificationType,
        title: String?,
        text: String?
    ): Notification {

        // intent로 MainActivity를 호출 하고 type을 putExtra
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notificationType", "${type.title}타입")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // MainActivity 실행 중 재실행 시 기존화면을 갱신
            // A B (B: 실행) -> (X)(A B B) / (O) (A B)
        }
        // 제 3자에게 intent를 넘겨줄 수있는 pendingIntent를 생성
        // 푸시알람, 위젯 등에 사용 (제3자 = Adroid OS)
        val pendingIntent = PendingIntent.getActivity(this, type.id, intent, FLAG_UPDATE_CURRENT)

        // Notification 기본형 셋팅
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.pet_icon) //아이콘
            .setContentTitle(title) // 제목
            .setContentText(text) // 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 중요도
            .setContentIntent(pendingIntent) // MainActivity 실행
            .setAutoCancel(true) // 클릭시 알림 사라짐

            // 확정형 셋팅 (아래 내용을 추가)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "this can be fill with big text"
                )
            )

            // 커스텀 셋팅 RemoteViews (아래내용을 추가)
            // R.layout.view_custom_notification 은 LinearLayout으로 생성
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(
                        RemoteViews(packageName, R.layout.view_custom_notification).apply {
                            setTextViewText(R.id.title, title)
                            setTextViewText(R.id.message, text)
                        }
                    )

        return notificationBuilder.build() // Notification을 반환
    }
~~~

### get Firebase Token
https://firebase.google.com/docs/cloud-messaging/android/client?hl=ko#kotlin+ktx_1
~~~kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
    }
}
~~~
