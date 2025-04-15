package com.example.myapplication.classes.poule

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
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
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip


@Composable
fun CreerPoule(
    pouleExistante: Poule? = null,
    onValider: (Poule) -> Unit
) {
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val today = LocalDateTime.now().toString()

    val initialPoule = pouleExistante
    // États
    var nom by remember { mutableStateOf(initialPoule?.nom ?: "") }
    var variete by remember { mutableStateOf(initialPoule?.variete ?: "") }
    var lieuAchat by remember { mutableStateOf(initialPoule?.lieuAchat ?: "") }
    var prixAchat by remember { mutableStateOf(initialPoule?.prixAchat?.toString() ?: "") }
    var dateNaissance by remember { mutableStateOf(initialPoule?.dateNaissance ?: "") }
    var dateNaissancePresumee by remember { mutableStateOf(initialPoule?.naissancePresumee ?: false) }
    var dateAcquisition by remember { mutableStateOf(initialPoule?.dateAcquisition ?: "") }
    var dateAcquisitionPresumee by remember { mutableStateOf(initialPoule?.acquisitionPresumee ?: false) }
    var particularites by remember { mutableStateOf(initialPoule?.particularitesComportementales ?: "") }
    var debutPonte by remember { mutableStateOf(initialPoule?.debutPonte ?: "") }
    var dateDeces by remember { mutableStateOf(initialPoule?.dateDeces ?: "") }
    var causeDeces by remember { mutableStateOf(initialPoule?.causeDeces ?: "") }
    var dateDecesPresume by remember { mutableStateOf(initialPoule?.causePresumee ?: false) }
    var causeDecesPresume by remember { mutableStateOf(initialPoule?.causePresumee ?: false) }

    val listePhotos = remember { mutableStateListOf<Photo>().apply { addAll(initialPoule?.photos ?: emptyList()) } }
    var photoPrincipaleUri by remember { mutableStateOf<Uri?>(initialPoule?.photoPrincipaleUri?.let { Uri.parse(it) }) }


    // Erreurs
    var nomError by remember { mutableStateOf(false) }
    var dateNaissanceError by remember { mutableStateOf(false) }
    var varieteError by remember { mutableStateOf(false) }
    var particularitesError by remember { mutableStateOf(false) }
    var dateAcquisitionError by remember { mutableStateOf(false) }
    var photoPrincipaleError by remember { mutableStateOf(false) }
    var debutPonteError by remember { mutableStateOf(false) }


    // Sélecteurs
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var isPickingAlbumImage by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }

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


        OutlinedTextField(
            value = debutPonte,
            onValueChange = { debutPonte = it },
            label = { Text("Début de ponte (yyyy-mm-dd)") },
            isError = debutPonteError,
            supportingText = {
                if (debutPonteError) Text("Format invalide. Utilisez yyyy-mm-dd", color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
        if (pouleExistante != null) {
            OutlinedTextField(
                value = dateDeces,
                onValueChange = { dateDeces = it },
                label = { Text("Date de décès (yyyy-mm-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = dateDecesPresume,
                    onCheckedChange = { dateDecesPresume = it }
                )
                Text("Date de décès présumée")
            }

            OutlinedTextField(
                value = causeDeces,
                onValueChange = { causeDeces = it },
                label = { Text("Cause du décès") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = causeDecesPresume,
                    onCheckedChange = { causeDecesPresume = it }
                )
                Text("Cause présumée")
            }
        }

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
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(photo.uri),
                            contentDescription = "Photo album",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { photoToDelete = photo },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            Text("Aucune photo ajoutée à l'album", style = MaterialTheme.typography.bodySmall)
        }

        if (photoToDelete != null) {
            AlertDialog(
                onDismissRequest = { photoToDelete = null },
                title = { Text("Supprimer cette photo ?") },
                text = { Text("Voulez-vous vraiment supprimer cette photo de l’album ?") },
                confirmButton = {
                    TextButton(onClick = {
                        listePhotos.remove(photoToDelete)
                        photoToDelete = null
                    }) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { photoToDelete = null }) {
                        Text("Annuler")
                    }
                }
            )
        }




        Button(
            onClick = {
                val isNomValide = nom.isNotBlank()
                val isDateNaissanceValide = dateNaissance.isValideYearMonth()
                val isVarieteValide = variete.isNotBlank()
                val isParticularitesValide = particularites.isNotBlank()
                val isDateAcquisitionValide = dateAcquisition.isBlank() || dateAcquisition.isValidDateOrEmpty()
                val isPhotoPrincipaleValide = photoPrincipaleUri != null
                val isDebutPonteValide = debutPonte.isValidDateOrEmpty()


                // Mise à jour des erreurs
                nomError = !isNomValide
                dateNaissanceError = !isDateNaissanceValide
                varieteError = !isVarieteValide
                particularitesError = !isParticularitesValide
                dateAcquisitionError = !isDateAcquisitionValide
                photoPrincipaleError = !isPhotoPrincipaleValide
                debutPonteError = !isDebutPonteValide


                val isFormValid = isNomValide &&
                        isDateNaissanceValide &&
                        isParticularitesValide &&
                        isVarieteValide &&
                        isDateAcquisitionValide &&
                        isPhotoPrincipaleValide && isDebutPonteValide

                if (!isFormValid) return@Button

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) return@Button

                val pouleId = pouleExistante?.id ?: UUID.randomUUID().toString()
                val storageRef = FirebaseStorage.getInstance().reference

                // 1️ Upload de la photo principale

                val isFirebaseUrl = initialPoule?.photoPrincipaleUri?.startsWith("https://") == true &&
                        photoPrincipaleUri?.toString()?.startsWith("https://") == true
                val principaleRef = storageRef.child("poules/$userId/$pouleId/principale.jpg")

                val albumPhotos = listePhotos.toList()
                val uploadedPhotos = mutableListOf<Photo>()

                fun enregistrerDansFirestore(principaleUrl: String) {
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
                        photoPrincipaleUri = principaleUrl,
                        debutPonte = debutPonte.ifBlank { null },
                        photos = uploadedPhotos
                    )
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("poules")
                        .document(userId!!)
                        .collection("liste")
                        .document(pouleId)
                        .set(poule)
                        .addOnSuccessListener {
                            println("Poule enregistrée dans Firestore")
                            onValider(poule)
                        }
                        .addOnFailureListener { e ->
                            println("Erreur Firestore: ${e.message}")
                        }
                }

                if (isFirebaseUrl) {
                    // Pas besoin de re-uploader l’image principale
                    if (albumPhotos.isEmpty()) {
                        enregistrerDansFirestore(photoPrincipaleUri.toString())
                    } else {
                        var count = 0
                        albumPhotos.forEachIndexed { index, photo ->
                            if (photo.uri.startsWith("https://")) {
                                // Déjà uploadée, on la garde telle quelle
                                uploadedPhotos.add(photo)
                                count++
                                if (count == albumPhotos.size) {
                                    enregistrerDansFirestore(photoPrincipaleUri.toString())
                                }
                            } else {
                                // Uri local, on doit uploader
                                val albumRef = storageRef.child("poules/$userId/$pouleId/album/photo_$index.jpg")
                                albumRef.putFile(Uri.parse(photo.uri)).continueWithTask { task ->
                                    if (!task.isSuccessful) throw task.exception ?: Exception("Erreur upload album")
                                    albumRef.downloadUrl
                                }.addOnSuccessListener { downloadUrl ->
                                    uploadedPhotos.add(Photo(uri = downloadUrl.toString(), date = photo.date))
                                    count++
                                    if (count == albumPhotos.size) {
                                        enregistrerDansFirestore(photoPrincipaleUri.toString())
                                    }
                                }
                            }
                        }

                    }
                } else {
                    // Upload de la nouvelle image principale
                    photoPrincipaleUri?.let { uri ->
                        // Supprimer l'ancienne photo principale si elle existe et qu'on en a sélectionné une nouvelle
                        val uriString = uri.toString()
                        val isLocalUri = uriString.startsWith("content://") || uriString.startsWith("file://")

                        if (!isLocalUri) {
                            println("ERREUR : L’image principale n’est pas une URI locale. Abandon de l’upload.")
                            return@let // on ne tente pas de l’upload
                        }

                        // Supprimer l'ancienne image si c'était une URL Firebase
                        // Supprimer l'ancienne photo si elle est en ligne
                        if (pouleExistante?.photoPrincipaleUri?.startsWith("https://") == true) {
                            val ancienneRef = FirebaseStorage.getInstance().getReferenceFromUrl(pouleExistante.photoPrincipaleUri!!)
                            ancienneRef.delete()
                                .addOnSuccessListener { println("Ancienne photo principale supprimée") }
                                .addOnFailureListener { println("Erreur suppression ancienne image : ${it.message}") }
                        }


                        principaleRef.putFile(uri).continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception ?: Exception("Erreur upload principale")
                            principaleRef.downloadUrl
                        }.addOnSuccessListener { principaleUrl ->
                            if (albumPhotos.isEmpty()) {
                                enregistrerDansFirestore(principaleUrl.toString())
                            } else {
                                var count = 0
                                albumPhotos.forEachIndexed { index, photo ->
                                    if (photo.uri.startsWith("https://")) {
                                        uploadedPhotos.add(photo)
                                        count++
                                        if (count == albumPhotos.size) {
                                            enregistrerDansFirestore(principaleUrl.toString())
                                        }
                                    } else {
                                        // cas des URI locales (content:// ou file://)
                                        val albumRef = storageRef.child("poules/$userId/$pouleId/album/photo_$index.jpg")
                                        albumRef.putFile(Uri.parse(photo.uri)).continueWithTask { task ->
                                            if (!task.isSuccessful) throw task.exception ?: Exception("Erreur upload album")
                                            albumRef.downloadUrl
                                        }.addOnSuccessListener { downloadUrl ->
                                            uploadedPhotos.add(Photo(uri = downloadUrl.toString(), date = photo.date))
                                            count++
                                            if (count == albumPhotos.size) {
                                                enregistrerDansFirestore(principaleUrl.toString())
                                            }
                                        }
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
            Text(if (pouleExistante != null) "Enregistrer les modifications" else "Créer la fiche")
        }

    }
}

fun String.isValideYearMonth(): Boolean {
    return this.matches(Regex("""\d{4}-\d{2}"""))
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.isValidDateOrEmpty(): Boolean {
    return this.isBlank() || try {
        LocalDate.parse(this)
        true
    } catch (e: Exception) {
        false
    }
}






