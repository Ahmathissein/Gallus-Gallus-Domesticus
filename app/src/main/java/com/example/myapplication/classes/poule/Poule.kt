package com.example.myapplication.classes.poule

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.*
import java.util.*


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

    var historiquePonte: Map<String, Boolean> = emptyMap(),
    var statistiquesMensuelles: Map<String, Int> = emptyMap(),
    var statistiquesAnnuelles: Map<String, Int> = emptyMap(),

    val creeLe: Timestamp = Timestamp.now(),
    var modifieLe: Timestamp = Timestamp.now()
)
