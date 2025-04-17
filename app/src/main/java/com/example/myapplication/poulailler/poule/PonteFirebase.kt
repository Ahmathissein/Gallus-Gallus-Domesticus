package com.example.myapplication.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


fun ajouterDansFirestore(
    pouleId: String,
    documentId: String,
    nbOeufs: Int,
    commentaire: String? = null,
    date: String,
    onSuccess: () -> Unit = {},
    onError: (Exception) -> Unit = {}
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    val ponte = hashMapOf(
        "date" to date,
        "nbOeufs" to nbOeufs,
        "commentaire" to commentaire
    )

    val pouleRef = firestore.collection("poules")
        .document(userId)
        .collection("liste")
        .document(pouleId)

    pouleRef.collection("pontes")
        .document(documentId)
        .set(ponte)
        .addOnSuccessListener {
            // Met à jour les statistiques mensuelles/annuelles
            pouleRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val mois = date.substring(0, 7)
                    val annee = date.substring(0, 4)

                    val statsMois = (document.get("statistiquesMensuelles") as? Map<String, Long>)?.toMutableMap() ?: mutableMapOf()
                    val statsAnnee = (document.get("statistiquesAnnuelles") as? Map<String, Long>)?.toMutableMap() ?: mutableMapOf()

                    statsMois[mois] = (statsMois[mois] ?: 0) + nbOeufs
                    statsAnnee[annee] = (statsAnnee[annee] ?: 0) + nbOeufs

                    val updates = mapOf(
                        "statistiquesMensuelles" to statsMois,
                        "statistiquesAnnuelles" to statsAnnee
                    )

                    pouleRef.update(updates)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e) }
                }
            }.addOnFailureListener { e -> onError(e) }
        }
        .addOnFailureListener { e -> onError(e) }
}

