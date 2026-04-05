package com.example.coachpro

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.coachpro.databinding.ActivityAddPerformanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPerformanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPerformanceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var sessionId: String = ""
    private var sessionName: String = ""
    private var performanceId: String = ""
    private var playerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Intent se data lo
        sessionId = intent.getStringExtra("sessionId") ?: ""
        sessionName = intent.getStringExtra("sessionName") ?: ""
        performanceId = intent.getStringExtra("performanceId") ?: ""
        playerId = intent.getStringExtra("playerId") ?: ""

        binding.tvSessionName.text = "Session: $sessionName"
        binding.tvBack.setOnClickListener { finish() }

        // Edit mode — existing data prefill karo
        if (performanceId.isNotEmpty()) {
            binding.tvTitle.text = "Edit Performance"
            binding.etPlayerName.setText(intent.getStringExtra("playerName") ?: "")
            binding.etPoints.setText(intent.getIntExtra("points", 0).toString())
            binding.etAssists.setText(intent.getIntExtra("assists", 0).toString())
            binding.etRebounds.setText(intent.getIntExtra("rebounds", 0).toString())
            binding.seekEffort.progress = intent.getIntExtra("effort", 3)
            binding.seekDefense.progress = intent.getIntExtra("defense", 3)
            binding.tvEffortVal.text = "${binding.seekEffort.progress}/5"
            binding.tvDefenseVal.text = "${binding.seekDefense.progress}/5"
        }

        // SeekBar listeners
        binding.seekEffort.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvEffortVal.text = "$progress/5"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekDefense.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvDefenseVal.text = "$progress/5"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnSave.setOnClickListener {
            savePerformance()
        }
    }

    private fun savePerformance() {
        val playerName = binding.etPlayerName.text.toString().trim()
        val points = binding.etPoints.text.toString().trim().toIntOrNull() ?: 0
        val assists = binding.etAssists.text.toString().trim().toIntOrNull() ?: 0
        val rebounds = binding.etRebounds.text.toString().trim().toIntOrNull() ?: 0
        val effort = binding.seekEffort.progress
        val defense = binding.seekDefense.progress

        if (playerName.isEmpty()) {
            binding.tilPlayerName.error = "Player name required"
            return
        }
        binding.tilPlayerName.error = null

        showLoading(true)

        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "playerName" to playerName,
            "playerId" to playerId,
            "points" to points,
            "assists" to assists,
            "rebounds" to rebounds,
            "effort" to effort,
            "defense" to defense,
            "sessionId" to sessionId,
            "createdAt" to System.currentTimeMillis()
        )

        val colRef = db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .collection("performance")

        // Edit mode — existing doc update karo
        val task = if (performanceId.isNotEmpty()) {
            colRef.document(performanceId).set(data)
        } else {
            colRef.add(data)
        }

        task.addOnSuccessListener {
            showLoading(false)
            finish()
        }.addOnFailureListener { e ->
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