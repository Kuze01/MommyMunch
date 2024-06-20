package com.dicoding.mommymunch.ui.ui_activity.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.mommymunch.databinding.ActivitySignupBinding
import com.dicoding.mommymunch.ui.ui_activity.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.loginButtonInSignup.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }

        binding.signupButton.setOnClickListener {
            processSignup()
        }
    }

    private fun processSignup() {
        val username = binding.usernameSignupEditText.text.toString()
        val email = binding.emailSignupEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val passwordConf = binding.passwordSignupEditTextConf.text.toString()

        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && passwordConf.isNotEmpty()) {
            if (password == passwordConf) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = username
                            }
                            user!!.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileUpdateTask ->
                                    if (profileUpdateTask.isSuccessful) {
                                        Toast.makeText(this, "Sign up successful. Please log in.", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
        }
    }
}
