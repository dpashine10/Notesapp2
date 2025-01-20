package com.example.notesapp2

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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

class Registration : AppCompatActivity() {
    private lateinit var emailadd : EditText
    private lateinit var password : EditText
    private lateinit var registernew : Button
    private lateinit var mainmenu : Button

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        emailadd = findViewById(R.id.emailadd)
        password = findViewById(R.id.password)
        registernew = findViewById(R.id.newregisterbutton)
        mainmenu = findViewById(R.id.btloginnew)

        fun hasSpecialCharactersRobust(password: String, allowedSpecialChars: String = "!@#$%^&*()_+-=[]{}|;':\",./<>?"): Boolean {
            return password.any { it in allowedSpecialChars }
        }
        auth = Firebase.auth
        registernew.setOnClickListener {
            val emailid = emailadd.text.toString()
            val pass = password.text.toString()
            if (emailid.isBlank() and pass.isBlank()){
                Toast.makeText(this,"Please enter correct credentials", Toast.LENGTH_SHORT).show()
            }
            else if (pass.isBlank() || pass.length < 8 || !hasSpecialCharactersRobust(pass)) {
                Toast.makeText(this,"Please enter a valid password", Toast.LENGTH_SHORT).show()
            } else if (emailid.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailid).matches())
                Toast.makeText(this,"Please enter a valid email id", Toast.LENGTH_SHORT).show()
            else
            {
                auth.createUserWithEmailAndPassword(emailid, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            Toast.makeText(this,"Successfully Registered. Verify your email and Login",
                                Toast.LENGTH_SHORT).show()
                            user?.sendEmailVerification()
                            auth.signOut()
                            finish()
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            //update ui here
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            //updateUI here
                        }
                    }
            }
        }
        mainmenu.setOnClickListener {
            finish()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }
}