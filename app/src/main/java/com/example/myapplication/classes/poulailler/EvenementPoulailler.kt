package com.example.myapplication.classes.poulailler

data class EvenementPoulailler(
    val id: String = "",
    val titre: String = "",
    val type: String = "",        // Ex : "Installation", "Entretien", etc.
    val description: String = "",
    val date: String = "",        // Format: "yyyy-MM-dd"
    val cout: Double? = null      // Coût en euros (nullable si aucun)
)

