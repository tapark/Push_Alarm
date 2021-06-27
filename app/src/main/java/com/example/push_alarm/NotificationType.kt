package com.example.push_alarm

enum class NotificationType(val title: String, val id: Int) {
    NORMAL("일반알림", 0),
    EXPANDABLE("확장형알림", 1),
    CUSTOM("커스텀알림", 3)
}