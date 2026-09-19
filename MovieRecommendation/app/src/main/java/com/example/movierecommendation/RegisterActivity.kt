package com.example.movierecommendation

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerBtn: Button
    private lateinit var loginRedirectBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.emailInput)
        password = findViewById(R.id.passwordInput)
        registerBtn = findViewById(R.id.registerBtn)
        loginRedirectBtn = findViewById(R.id.loginRedirectBtn)

        registerBtn.setOnClickListener {
            val userEmail = email.text.toString()
            val userPass = password.text.toString()

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginRedirectBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
