package com.example.myapplication.classes.poule

import java.time.LocalDate

data class Evenement(
    val date: String = "",
    val type: String = "", // ex : "maladie", "reproduction", "couvaison"
    val description: String = ""
)
