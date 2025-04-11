package com.example.myapplication.classes.poule

import java.time.LocalDate

data class Evenement(
    val date: LocalDate,
    val type: String, // ex : "maladie", "reproduction", "couvaison"
    val description: String
)
