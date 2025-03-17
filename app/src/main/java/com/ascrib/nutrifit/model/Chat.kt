package com.ascrib.nutrifit.model

data class Chat(
    var id: Int,
    var name: String,
    var message: String,
    var img: Int,
    var time: String,
    var read: Boolean,
    var isOnline :Boolean,
    var isCurrentUser: Boolean,
)