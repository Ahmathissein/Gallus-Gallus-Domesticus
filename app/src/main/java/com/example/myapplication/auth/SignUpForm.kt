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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@Composable
fun SignupForm(
    onAuthSuccess: () -> Unit,
    switchToLoginTab: (() -> Unit)? = null
) {
    MyApplicationTheme {
        val auth = Firebase.auth

        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }

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
                        "Créer un compte",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF5D4037)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Prénom", color = Color(0xFF5D4037)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Nom", color = Color(0xFF5D4037)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))


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
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe", color = Color(0xFF5D4037)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Masquer" else "Afficher"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            message = ""

                            // 1. Vérification côté client
                            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                message = "Veuillez remplir tous les champs."
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                message = "Format d'email invalide."
                            } else if (password != confirmPassword) {
                                message = "Les mots de passe ne correspondent pas."
                            } else if (password.length < 8) {
                                message = "Le mot de passe doit contenir au moins 6 caractères."
                            }else if (!isPasswordStrong(password)) {
                                message = "Le mot de passe doit contenir au moins 8 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial."
                            }
                            else {
                                // 2. Tentative de création du compte
                                try {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                try {
                                                    val uid = task.result?.user?.uid
                                                        ?: throw Exception("UID introuvable.")

                                                    val user = User(
                                                        uid = uid,
                                                        prenom = firstName,
                                                        nom = lastName,
                                                        email = email
                                                    )

                                                    FirebaseFirestore.getInstance().collection("utilisateurs")
                                                        .document(uid)
                                                        .set(user)
                                                        .addOnSuccessListener {
                                                            auth.signOut()
                                                            switchToLoginTab?.invoke()
                                                            message = "Compte créé avec succès. Veuillez vous connecter."
                                                        }
                                                        .addOnFailureListener { e ->
                                                            message = "Utilisateur créé, mais Firestore a échoué : ${e.localizedMessage}"
                                                        }
                                                } catch (e: Exception) {
                                                    message = "Une erreur interne est survenue : ${e.localizedMessage}"
                                                }
                                            } else {
                                                val exception = task.exception
                                                when {
                                                    exception is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                                                        message = "Cet e-mail est déjà utilisé par un autre compte. Veuillez vous diriger vers l'onglet de connection"

                                                    exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                                        message = "L'adresse e-mail est invalide."

                                                    else ->
                                                        message = exception?.localizedMessage ?: "Erreur inconnue. Réessayez plus tard."
                                                }
                                            }
                                        }
                                } catch (e: Exception) {
                                    message = "Erreur lors de la création du compte : ${e.localizedMessage}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("S'inscrire", color = Color.White)
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

fun isPasswordStrong(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&.\\-])[A-Za-z\\d@\$!%*?&.\\-]{8,}$")
    return passwordRegex.matches(password)
}
