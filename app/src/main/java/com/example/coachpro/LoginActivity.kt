package com.example.coachpro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.View
import com.example.coachpro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    // ViewBinding — no more findViewById!
    private lateinit var binding: ActivityLoginBinding

    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding setup
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth initialize
        auth = FirebaseAuth.getInstance()

        // Auto-login check — agar already logged in hai toh dashboard pe jaao
        if (auth.currentUser != null) {
            goToDashboard()
            return
        }

        // Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Basic Validation
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Password required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.tilPassword.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            // Clear errors
            binding.tilEmail.error = null
            binding.tilPassword.error = null

            // Show loading
            showLoading(true)

            // Firebase Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    showLoading(false)
                    goToDashboard()
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    showError(exception.message ?: "Login failed")
                }
        }

        // Register Link Click
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Back press pe login screen nahi aani chahiye
    }
}