package com.example.coachpro

data class Session(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val focus: String = "",
    val dateMillis: Long = 0L,
    val status: String = "Upcoming"
)