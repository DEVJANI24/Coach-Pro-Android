package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.coachpro.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //Back Button
        binding.tvBack.setOnClickListener {
            finish() // is screen ko band karo
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val school = binding.etSchool.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validation
            if (name.isEmpty()) {
                binding.tilName.error = "Name required"
                return@setOnClickListener
            }
            if (school.isEmpty()) {
                binding.tilSchool.error = "School/Club name required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.tilPassword.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            // Clear errors
            binding.tilName.error = null
            binding.tilSchool.error = null
            binding.tilEmail.error = null
            binding.tilPassword.error = null

            showLoading(true)

            // Firebase Auth — user banao
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    // Auth successful — ab Firestore mein coach data save karo
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    val coachData  = hashMapOf(
                        "name" to name,
                        "school" to school,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("coaches")
                        .document(userId)
                        .set(coachData)
                        .addOnSuccessListener {
                            showLoading(false)
                            // Registration done — Dashboard pe jaao
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity() // Login + Register dono stack se hata do
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            showError("Profile save failed: ${e.message}")
                        }
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    showError(exception.message ?: "Registration failed")
                }
                }
        }
    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE

    }

}
