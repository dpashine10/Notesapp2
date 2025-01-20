package com.example.notesapp2

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var usernameinput: EditText
    private lateinit var passwordinput: EditText
    private lateinit var login: Button
    private lateinit var forgot: Button
    private lateinit var register: Button

    private lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        usernameinput = findViewById(R.id.userinp)
        passwordinput = findViewById(R.id.passinp)
        login = findViewById(R.id.login_button)
        forgot = findViewById(R.id.forgotbutton)
        register = findViewById(R.id.registerbutton)
        auth = Firebase.auth
        login.setOnClickListener {
            val username = usernameinput.text.toString()
            val password = passwordinput.text.toString()
            if (username.isBlank() and password.isBlank()){
                Toast.makeText(this,"Please enter credentials", Toast.LENGTH_SHORT).show()
            }
            else if (username.isBlank()) {
                Toast.makeText(this, "Please enter Name", Toast.LENGTH_SHORT).show()
            } else if (password.isBlank()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            if (user != null) {
                                if (user.isEmailVerified) {
                                    Toast.makeText(this,"Sign-in Successful",Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, mainpage::class.java)
                                    startActivity(intent)
                                }
                                else
                                {
                                    Toast.makeText(this,"Verify your email first",Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    finish()
                                    val intent = Intent(this,MainActivity::class.java)
                                    startActivity(intent)
                                }                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
        forgot.setOnClickListener {
            val intent = Intent(this, Forgotpassword::class.java)
            startActivity(intent)
        }
        register.setOnClickListener {
            val intent = Intent(this,Registration::class.java)
            startActivity(intent)
        }
    }
}
