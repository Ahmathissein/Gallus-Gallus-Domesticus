package com.example.myapplication.auth

import com.example.myapplication.ui.theme.MyApplicationTheme
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : ComponentActivity() {

    private val auth by lazy { Firebase.auth }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // Utilisateur déjà connecté → Page d'accueil directement
                    startActivity(Intent(this, com.example.myapplication.MainActivity::class.java))
                    finish()
                }else{
                    auth.signOut()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                AuthScreen(onAuthSuccess = {
                    startActivity(Intent(this, com.example.myapplication.MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}
