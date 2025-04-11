package com.example.myapplication.classes.poule

import java.time.*
import java.util.*


data class Poule(
    val id: String = UUID.randomUUID().toString(),
    var nom: String,
    var photos: List<Photo> = listOf(),
    var photoPrincipaleUri: String? = null,

    var dateAcquisition: LocalDate? = null,
    var lieuAchat: String? = null,
    var prixAchat: Double? = null,

    var dateNaissance: YearMonth? = null,
    var naissancePresumee: Boolean = false,

    var variete: String? = null,
    var debutPonte: LocalDate? = null,
    var particularitesComportementales: String? = null,

    var evenements: List<Evenement> = listOf(),

    var dateDeces: LocalDate? = null,
    var causeDeces: String? = null,
    var causePresumee: Boolean = false,

    var historiquePonte: Map<LocalDate, Boolean> = emptyMap(),
    var statistiquesMensuelles: Map<YearMonth, Int> = emptyMap(),
    var statistiquesAnnuelles: Map<Year, Int> = emptyMap(),

    val creeLe: LocalDateTime = LocalDateTime.now(),
    var modifieLe: LocalDateTime = LocalDateTime.now()
)
