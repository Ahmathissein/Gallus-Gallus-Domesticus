package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginForm(onAuthSuccess: () -> Unit) {
    MyApplicationTheme {
        val auth = Firebase.auth
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var showResetDialog by remember { mutableStateOf(false) }
        var resetEmail by remember { mutableStateOf("") }
        var resetMessage by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF6EC))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Connexion",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF5D4037)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color(0xFF5D4037)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe", color = Color(0xFF5D4037)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                message = "Veuillez remplir tous les champs."
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onAuthSuccess()
                                        } else {
                                            val errorMessage = when (task.exception?.message) {
                                                "There is no user record corresponding to this identifier." -> "Aucun compte ne correspond à cet email."
                                                "The password is invalid or the user does not have a password." -> "Mot de passe incorrect."
                                                "The user account has been disabled by an administrator." -> "Ce compte a été désactivé."
                                                else -> task.exception?.localizedMessage ?: "Erreur inconnue lors de la connexion."
                                            }
                                            message = errorMessage
                                        }
                                    }
                                    .addOnFailureListener {
                                        message = "Erreur de connexion. Vérifiez votre réseau ou réessayez plus tard."
                                    }

                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Se connecter", color = Color.White)
                    }
                    TextButton(onClick = { showResetDialog = true }) {
                        Text("Mot de passe oublié ?", color = Color(0xCC9C5700))
                    }
                    if (showResetDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (resetEmail.isBlank()) {
                                        resetMessage = "Veuillez entrer votre email."
                                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                                        resetMessage = "Format d'email invalide."
                                    } else {
                                        Firebase.auth.sendPasswordResetEmail(resetEmail)
                                            .addOnSuccessListener {
                                                resetMessage = "Email de réinitialisation envoyé."
                                            }
                                            .addOnFailureListener {
                                                resetMessage = "Erreur : ${it.localizedMessage}"
                                            }
                                    }
                                }) {
                                    Text("Envoyer", color = Color(0xCC9C5700))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false }) {
                                    Text("Annuler")
                                }
                            },
                            title = { Text("Réinitialisation du mot de passe") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = resetEmail,
                                        onValueChange = { resetEmail = it },
                                        label = { Text("Email") },
                                        singleLine = true
                                    )
                                    if (resetMessage.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(resetMessage, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }


                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
