import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.myapplication.auth.User

@Composable
fun ProfilScreen(onProfileUpdated: () -> Unit) {
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: return
    val context = LocalContext.current

    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("utilisateurs")
                .document(userId)
                .get()
                .await()

            val u = snapshot.toObject(User::class.java)
            if (u != null) {
                prenom = u.prenom
                nom = u.nom
                email = u.email
            }
        } catch (_: Exception) {
            message = "Erreur de chargement du profil."
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF6EC))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Mon profil",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037)
            )

            Spacer(Modifier.height(24.dp))

            // ───── Infos personnelles ─────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Informations", color = Color(0xFF5D4037), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        label = { Text("Prénom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (prenom.isBlank() && nom.isBlank()) {
                                message = "Veuillez saisir un prénom ou un nom."
                                return@Button
                            }

                            FirebaseFirestore.getInstance()
                                .collection("utilisateurs")
                                .document(userId)
                                .set(User(uid = userId, prenom = prenom, nom = nom, email = email))
                                .addOnSuccessListener {
                                    message = "Profil mis à jour."
                                    auth.currentUser?.updateEmail(email)
                                    onProfileUpdated()
                                }
                                .addOnFailureListener {
                                    message = "Erreur lors de la mise à jour."
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ───── Mot de passe ─────
            // ───── Mot de passe ─────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Mot de passe", color = Color(0xFF5D4037), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Si vous souhaitez modifier votre mot de passe, cliquez sur le bouton ci-dessous. Vous recevrez un email de réinitialisation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val email = Firebase.auth.currentUser?.email
                            if (!email.isNullOrBlank()) {
                                Firebase.auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        message = "Email de réinitialisation envoyé à $email"
                                    }
                                    .addOnFailureListener {
                                        message = "Erreur lors de l’envoi de l’email."
                                    }
                            } else {
                                message = "Email introuvable. Veuillez vérifier votre compte."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Réinitialiser le mot de passe", color = Color.White)
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            // Message
            if (message.isNotEmpty()) {
                Text(message, color = Color(0xFF4CAF50))
            }
        }
    }
}
