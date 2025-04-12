package com.example.myapplication.classes.poule

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import java.time.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore




@Composable
fun CreerPoule(
    onValider: (Poule) -> Unit
) {
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val today = LocalDateTime.now().toString()

    // États
    var nom by remember { mutableStateOf("") }
    var variete by remember { mutableStateOf("") }
    var lieuAchat by remember { mutableStateOf("") }
    var prixAchat by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var dateNaissancePresumee by remember { mutableStateOf(false) }
    var dateAcquisition by remember { mutableStateOf("") }
    var dateAcquisitionPresumee by remember { mutableStateOf(false) }
    var particularites by remember { mutableStateOf("") }

    val listePhotos = remember { mutableStateListOf<Photo>() }
    var photoPrincipaleUri by remember { mutableStateOf<Uri?>(null) }

    // Erreurs
    var nomError by remember { mutableStateOf(false) }
    var dateNaissanceError by remember { mutableStateOf(false) }
    var varieteError by remember { mutableStateOf(false) }
    var particularitesError by remember { mutableStateOf(false) }
    var dateAcquisitionError by remember { mutableStateOf(false) }
    var photoPrincipaleError by remember { mutableStateOf(false) }

    // Sélecteurs
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var isPickingAlbumImage by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers
    val genericCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        pendingUri?.let { uri ->
            if (success) {
                if (isPickingAlbumImage) {
                    listePhotos.add(Photo(uri.toString(), today))
                } else {
                    photoPrincipaleUri = uri
                    photoPrincipaleError = false
                }
            }
            pendingUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingUri?.let { uri ->
                genericCameraLauncher.launch(uri)
            }
        } else {
            println("Permission refusée pour la caméra")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            if (isPickingAlbumImage) {
                listePhotos.add(Photo(it.toString(), today))
            } else {
                photoPrincipaleUri = it
                photoPrincipaleError = false
            }
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            println("Permission refusée pour la galerie")
        }
    }

    fun createImageUri(context: Context, name: String): Uri {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${name}_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Impossible de créer une URI")
    }

    fun resetErrors() {
        nomError = false
        dateNaissanceError = false
        varieteError = false
        particularitesError = false
        dateAcquisitionError = false
        photoPrincipaleError = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Créer une fiche poule",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = nom,
            onValueChange = {
                nom = it
                resetErrors()            },
            label = {
                Row {
                    Text("Nom de la poule")
                    Text("*", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = nomError,
            supportingText = {
                if (nomError) Text("Champ obligatoire", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(
            value = dateAcquisition,
            onValueChange = {
                dateAcquisition = it
                resetErrors()
                            },
            label = {
                Text("Date d'acquisition (yyyy-mm-dd) — Facultatif")
            },
            isError = dateAcquisitionError,
            supportingText = {
                if (dateAcquisitionError) Text("Format invalide. Utilisez yyyy-mm-dd", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth()
        )


        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateAcquisitionPresumee,
                onCheckedChange = { dateAcquisitionPresumee = it }
            )
            Text(text = "Date d'acquisition présumée")
        }

        OutlinedTextField(
            value = lieuAchat,
            onValueChange = { newValue -> lieuAchat = newValue },
            label = { Text("Lieu d'achat") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = prixAchat,
            onValueChange = { prixAchat = it },
            label = { Text("Prix d'achat (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dateNaissance,
            onValueChange = {
                dateNaissance = it
                resetErrors() },
            label = {
                Row {
                    Text("Date de naissance (yyyy-mm)")
                    Text("*", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = dateNaissanceError,
            supportingText = {
                if (dateNaissanceError) Text("Champ obligatoire. Format : yyyy-mm", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth()
        )


        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateNaissancePresumee,
                onCheckedChange = { dateNaissancePresumee = it }
            )
            Text(text = "Date de naissance présumée")
        }


        OutlinedTextField(
            value = variete,
            onValueChange = {
                variete = it
                resetErrors() },
            label = {
                Row {
                    Text("Variété")
                    Text("*", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = varieteError,
            supportingText = {
                if (varieteError) Text("Champ obligatoire", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth()
        )

        /*
        OutlinedTextField(
            value = debutPonte,
            onValueChange = { debutPonte = it },
            label = { Text("Début de ponte (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        */
        OutlinedTextField(
            value = particularites,
            onValueChange = {
                particularites = it
                resetErrors() },
            label = {
                Row {
                    Text("Particularités comportementales")
                    Text("*", color = MaterialTheme.colorScheme.error)
                }
                    },
            isError = particularitesError,
            supportingText = {
                if (particularitesError) Text("Champ Obligatoire", color = MaterialTheme.colorScheme.error )
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        /*
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = dateDeces,
            onValueChange = { dateDeces = it },
            label = { Text("Date de deces (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateDecesPresumee,
                onCheckedChange = { dateDecesPresumee = it }
            )
            Text(text = "Date de deces présumée")
        }

        OutlinedTextField(
            value = causeDeces,
            onValueChange = { causeDeces = it },
            label = { Text("Cause du décès") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = causePresumee, onCheckedChange = { causePresumee = it })
            Text("Cause présumée")
        }
        */
        Button(
            onClick = {
                resetErrors()
                isPickingAlbumImage = false
                showImagePickerDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12))
        ) {
            Text("Choisir une image principale", color = Color.White)
        }

        photoPrincipaleUri?.let { uri ->
            Spacer(Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                isPickingAlbumImage = true
                showImagePickerDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
        ) {
            Text("Ajouter une photo à l'album", color = Color.White)
        }

        if (showImagePickerDialog) {
            AlertDialog(
                onDismissRequest = { showImagePickerDialog = false },
                title = { Text("Choisir une source") },
                text = { Text("Sélectionnez la source de l'image") },
                confirmButton = {
                    TextButton(onClick = {
                        showImagePickerDialog = false
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            galleryPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                            galleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        } else {
                            galleryLauncher.launch("image/*")
                        }
                    }) {
                        Text("Galerie")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showImagePickerDialog = false
                        pendingUri = createImageUri(context, if (isPickingAlbumImage) "album" else "principale")
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }) {
                        Text("Caméra")
                    }
                }
            )
        }

        if (listePhotos.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Album photo :", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listePhotos) { photo ->
                    Image(
                        painter = rememberAsyncImagePainter(photo.uri),
                        contentDescription = "Photo album",
                        modifier = Modifier.padding(4.dp).aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            Text("Aucune photo ajoutée à l'album", style = MaterialTheme.typography.bodySmall)
        }



        Button(
            onClick = {
                val isNomValide = nom.isNotBlank()
                val isDateNaissanceValide = dateNaissance.isValideYearMonth()
                val isVarieteValide = variete.isNotBlank()
                val isParticularitesValide = particularites.isNotBlank()
                val isDateAcquisitionValide = dateAcquisition.isBlank() || dateAcquisition.isValidDate()
                val isPhotoPrincipaleValide = photoPrincipaleUri != null

                // Mise à jour des erreurs
                nomError = !isNomValide
                dateNaissanceError = !isDateNaissanceValide
                varieteError = !isVarieteValide
                particularitesError = !isParticularitesValide
                dateAcquisitionError = !isDateAcquisitionValide
                photoPrincipaleError = !isPhotoPrincipaleValide

                val isFormValid = isNomValide &&
                        isDateNaissanceValide &&
                        isParticularitesValide &&
                        isVarieteValide &&
                        isDateAcquisitionValide &&
                        isPhotoPrincipaleValide

                if (!isFormValid) return@Button

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) return@Button

                val pouleId = UUID.randomUUID().toString()
                val storageRef = FirebaseStorage.getInstance().reference

                // 1️ Upload de la photo principale
                val principaleRef = storageRef.child("poules/$userId/$pouleId/principale.jpg")

                photoPrincipaleUri?.let { uri ->
                    principaleRef.putFile(uri).continueWithTask { task ->
                        if (!task.isSuccessful) throw task.exception ?: Exception("Erreur upload principale")
                        principaleRef.downloadUrl
                    }.addOnSuccessListener { principaleUrl ->

                        // 2️ Upload des photos de l’album (si présentes)
                        val uploadedPhotos = mutableListOf<Photo>()
                        val albumPhotos = listePhotos.toList()
                        if (albumPhotos.isEmpty()) {
                            // Pas de photo d’album, on peut créer la poule directement
                            val poule = Poule(
                                id = pouleId,
                                nom = nom,
                                variete = variete,
                                lieuAchat = lieuAchat,
                                prixAchat = prixAchat.toDoubleOrNull(),
                                dateAcquisition = dateAcquisition.ifBlank { null },
                                dateNaissance = dateNaissance,
                                naissancePresumee = dateNaissancePresumee,
                                particularitesComportementales = particularites,
                                photoPrincipaleUri = principaleUrl.toString(),
                                photos = emptyList()
                            )
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.collection("poules")
                                .document(userId) // Utilisateur
                                .collection("liste") // Sous-collection de poules
                                .document(pouleId) // ID unique de la poule
                                .set(poule)
                                .addOnSuccessListener {
                                    println("Poule enregistrée dans Firestore")
                                    onValider(poule)
                                }
                                .addOnFailureListener { e ->
                                    println("Erreur Firestore: ${e.message}")
                                }
                            println("creation avec succes 1")

                        } else {
                            // Upload des photos d’album une par une
                            val total = albumPhotos.size
                            var count = 0

                            albumPhotos.forEachIndexed { index, photo ->
                                val albumRef = storageRef.child("poules/$userId/$pouleId/album/photo_$index.jpg")
                                albumRef.putFile(Uri.parse(photo.uri)).continueWithTask { task ->
                                    if (!task.isSuccessful) throw task.exception ?: Exception("Erreur upload album")
                                    albumRef.downloadUrl
                                }.addOnSuccessListener { downloadUrl ->
                                    uploadedPhotos.add(
                                        Photo(
                                            uri = downloadUrl.toString(),
                                            date = photo.date
                                        )
                                    )
                                    count++
                                    if (count == total) {
                                        val poule = Poule(
                                            id = pouleId,
                                            nom = nom,
                                            variete = variete,
                                            lieuAchat = lieuAchat,
                                            prixAchat = prixAchat.toDoubleOrNull(),
                                            dateAcquisition = dateAcquisition.ifBlank() { null },
                                            dateNaissance = dateNaissance,
                                            naissancePresumee = dateNaissancePresumee,
                                            particularitesComportementales = particularites,
                                            photoPrincipaleUri = principaleUrl.toString(),
                                            photos = uploadedPhotos
                                        )
                                        val firestore = FirebaseFirestore.getInstance()
                                        firestore.collection("poules")
                                            .document(userId) // Utilisateur
                                            .collection("liste") // Sous-collection de poules
                                            .document(pouleId) // ID unique de la poule
                                            .set(poule)
                                            .addOnSuccessListener {
                                                println("Poule enregistrée dans Firestore")
                                                onValider(poule)
                                            }
                                            .addOnFailureListener { e ->
                                                println("Erreur Firestore: ${e.message}")
                                            }
                                        println("creation avec succes 2")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76E12))
        ) {
            Text("Créer la fiche", color = Color.White)
        }

    }
}

fun String.isValideYearMonth(): Boolean {
    return this.matches(Regex("""\d{4}-\d{2}"""))
}

fun String.isValidDate(): Boolean {
    return try {
        LocalDate.parse(this)
        true
    } catch (e: Exception) {
        false
    }
}





