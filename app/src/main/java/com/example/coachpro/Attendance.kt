package com.example.coachpro

data class Attendance(
    val playerId: String = "",
    val playerName: String = "",
    var isPresent: Boolean = true
)