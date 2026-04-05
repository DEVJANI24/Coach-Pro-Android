package com.example.coachpro

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.coachpro.databinding.ActivityAddPlayerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlayerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            savePlayer()
        }
    }

    private fun savePlayer() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val className = binding.etClass.text.toString().trim()
        val position = binding.etPosition.text.toString().trim()
        val parentName = binding.etParentName.text.toString().trim()
        val parentPhone = binding.etParentPhone.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.tilName.error = "Name required"
            return
        }
        if (age.isEmpty()) {
            binding.tilAge.error = "Age required"
            return
        }
        if (className.isEmpty()) {
            binding.tilClass.error = "Class required"
            return
        }
        if (position.isEmpty()) {
            binding.tilPosition.error = "Position required"
            return
        }

        binding.tilName.error = null
        binding.tilAge.error = null
        binding.tilClass.error = null
        binding.tilPosition.error = null

        showLoading(true)

        val uid = auth.currentUser?.uid ?: return

        val playerData = hashMapOf(
            "name" to name,
            "age" to age,
            "className" to className,
            "position" to position,
            "parentName" to parentName,
            "parentPhone" to parentPhone,
            "coachNotes" to notes,
            "status" to "Active",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("coaches")
            .document(uid)
            .collection("players")
            .add(playerData)
            .addOnSuccessListener {
                showLoading(false)
                finish() // Back jaao — player list refresh ho jayegi
            }
            .addOnFailureListener { e ->
                showLoading(false)
                binding.tvError.text = e.message
                binding.tvError.visibility = View.VISIBLE
            }
    }
    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
    }
    }