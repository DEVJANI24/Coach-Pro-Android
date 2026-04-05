package com.example.coachpro

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coachpro.databinding.ActivityPlayerDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var playerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        playerId = intent.getStringExtra("playerId") ?: ""

        binding.tvBack.setOnClickListener { finish() }

        loadPlayerDetail()

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadPlayerDetail() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("players")
            .document(playerId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                val age = doc.getString("age") ?: ""
                val className = doc.getString("className") ?: ""
                val position = doc.getString("position") ?: ""
                val parentName = doc.getString("parentName") ?: "—"
                val parentPhone = doc.getString("parentPhone") ?: "—"
                val notes = doc.getString("coachNotes") ?: "No notes added"
                val status = doc.getString("status") ?: "Active"

                // Avatar initials
                val initials = name.split(" ")
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                binding.tvAvatar.text = initials

                binding.tvName.text = name
                binding.tvClassPosition.text = "Class $className • $position • Age $age"
                binding.tvStatus.text = status
                binding.tvParentName.text = parentName
                binding.tvParentPhone.text = parentPhone
                binding.tvNotes.text = notes
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Player")
            .setMessage("Are you sure you want to delete this player?")
            .setPositiveButton("Delete") { _, _ ->
                deletePlayer()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlayer() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("players")
            .document(playerId)
            .delete()
            .addOnSuccessListener {
                finish()
            }
    }
}