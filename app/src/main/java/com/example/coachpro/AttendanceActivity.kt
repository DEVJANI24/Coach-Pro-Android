package com.example.coachpro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coachpro.databinding.ActivityAttendanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AttendanceAdapter

    private var sessionId: String = ""
    private var sessionName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        sessionId = intent.getStringExtra("sessionId") ?: ""
        sessionName = intent.getStringExtra("sessionName") ?: "Session"

        binding.tvSessionInfo.text = sessionName
        binding.tvBack.setOnClickListener { finish() }

        setupRecyclerView()
        loadPlayers()

        binding.tvSave.setOnClickListener {
            saveAttendance()
        }
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(mutableListOf())
        binding.recyclerAttendance.layoutManager = LinearLayoutManager(this)
        binding.recyclerAttendance.adapter = adapter
    }

    private fun loadPlayers() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        // Pehle check karo — attendance already saved hai?
        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .collection("attendance")
            .get()
            .addOnSuccessListener { attendanceSnapshot ->

                if (!attendanceSnapshot.isEmpty) {
                    // Already saved attendance load karo
                    val list = mutableListOf<Attendance>()
                    for (doc in attendanceSnapshot.documents) {
                        list.add(
                            Attendance(
                                playerId = doc.id,
                                playerName = doc.getString("playerName") ?: "",
                                isPresent = doc.getBoolean("isPresent") ?: true
                            )
                        )
                    }
                    adapter = AttendanceAdapter(list)
                    binding.recyclerAttendance.adapter = adapter
                    updateCounts(list)
                    binding.progressBar.visibility = View.GONE

                } else {
                    // Fresh — players list se load karo
                    loadPlayersFromRoster(uid)
                }
            }
    }

    private fun loadPlayersFromRoster(uid: String) {
        db.collection("coaches")
            .document(uid)
            .collection("players")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE

                val list = result.documents.map { doc ->
                    Attendance(
                        playerId = doc.id,
                        playerName = doc.getString("name") ?: "",
                        isPresent = true // Default: sabko present rakhte hain
                    )
                }.toMutableList()

                adapter = AttendanceAdapter(list)
                binding.recyclerAttendance.adapter = adapter
                updateCounts(list)
            }
    }

    private fun updateCounts(list: List<Attendance>) {
        val presentCount = list.count { it.isPresent }
        val absentCount = list.count { !it.isPresent }
        binding.tvPresentCount.text = presentCount.toString()
        binding.tvAbsentCount.text = absentCount.toString()
    }

    private fun saveAttendance() {
        val uid = auth.currentUser?.uid ?: return
        val attendanceData = adapter.getAttendanceData()

        binding.progressBar.visibility = View.VISIBLE
        binding.tvSave.isEnabled = false

        val batch = db.batch()
        val sessionRef = db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(sessionId)

        // Har player ki attendance save karo
        for (item in attendanceData) {
            val attRef = sessionRef.collection("attendance").document(item.playerId)
            val data = hashMapOf(
                "playerName" to item.playerName,
                "isPresent" to item.isPresent,
                "savedAt" to System.currentTimeMillis()
            )
            batch.set(attRef, data)
        }

        // Session status update karo
        batch.update(sessionRef, "status", "Completed")

        batch.commit()
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.tvSave.isEnabled = true
                Toast.makeText(this, "Attendance saved! ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.tvSave.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}