package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coachpro.databinding.ActivitySessionDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var sessionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        sessionId = intent.getStringExtra("sessionId") ?: ""

        binding.tvBack.setOnClickListener { finish() }
        loadSessionDetail()

        binding.btnAttendance.setOnClickListener {
            val intent = Intent(this, AttendanceActivity::class.java)
            intent.putExtra("sessionId", sessionId)
            intent.putExtra("sessionName", binding.tvSessionName.text.toString())
            startActivity(intent)
        }
    }

    private fun loadSessionDetail() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .get()
            .addOnSuccessListener { doc ->
                binding.tvSessionName.text = doc.getString("name") ?: ""
                binding.tvTime.text = doc.getString("time") ?: "—"
                binding.tvLocation.text = doc.getString("location") ?: "—"
                binding.tvFocus.text = doc.getString("focus") ?: "—"

                val millis = doc.getLong("dateMillis") ?: 0L
                val format = SimpleDateFormat("dd MMM yyyy, EEE", Locale.getDefault())
                binding.tvDate.text = format.format(Date(millis))
            }
    }

    private fun deleteSession() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .delete()
            .addOnSuccessListener { finish() }
    }
}