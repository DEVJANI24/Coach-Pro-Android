package com.example.coachpro

data class Performance(
    val id: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val playerClass: String = "",
    val playerPosition: String = "",
    val points: Int = 0,
    val assists: Int = 0,
    val rebounds: Int = 0,
    val effort: Int = 0,
    val defense: Int = 0,
    val sessionId: String = "",
    val createdAt: Long = 0L
)