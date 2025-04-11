package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onAuthSuccess()
                                    } else {
                                        message = "Email ou mot de passe incorrect."
                                    }
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC9C5700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Se connecter", color = Color.White)
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
