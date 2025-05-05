package com.example.myapplication.poulailler.poule

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.*
import java.util.*


data class Photo(
    val uri: String = "",
    val date: String = ""
)

data class Evenement(
    val date: String = "",
    val type: String = "", // ex : "maladie", "reproduction", "couvaison"
    val description: String = ""
)

data class Ponte(
    val date: String? = null,
    val nbOeufs: Int? = null,
    val commentaire: String? = null
)


data class Vente(
    val id: String = "",
    val type: String = "",
    val date: String = "",
    val quantite: Int? = null,
    val prixUnitaire: Double = 0.0,
    val client: String = "",
    val pouleId: String? = null
)


@IgnoreExtraProperties
data class Poule(
    val id: String = UUID.randomUUID().toString(),
    var nom: String = "",
    var photos: List<Photo> = listOf(),
    var photoPrincipaleUri: String? = null,

    var dateAcquisition: String? = null,
    var acquisitionPresumee: Boolean = false,
    var lieuAchat: String? = null,
    var prixAchat: Double? = null,

    var dateNaissance: String? = null,
    var naissancePresumee: Boolean = false,

    var variete: String? = null,
    var debutPonte: String? = null,
    var particularitesComportementales: String? = null,

    var evenements: List<Evenement> = listOf(),

    var dateDeces: String? = null,
    var dateDecesPresumee: Boolean? = null,
    var causeDeces: String? = null,
    var causePresumee: Boolean = false,

    var pontes: Map<String, Ponte> = emptyMap(),
    var statistiquesMensuelles: Map<String, Int> = emptyMap(),
    var statistiquesAnnuelles: Map<String, Int> = emptyMap(),
    var estVendue: Boolean = false,
    val creeLe: Timestamp = Timestamp.now(),
    var modifieLe: Timestamp = Timestamp.now()
)
