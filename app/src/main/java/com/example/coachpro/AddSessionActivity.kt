package com.example.coachpro

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.coachpro.databinding.ActivityAddSessionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSessionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedDateMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvBack.setOnClickListener { finish() }

        // Date picker
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveSession()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = calendar.timeInMillis

                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.etDate.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveSession() {
        val name = binding.etName.text.toString().trim()
        val time = binding.etTime.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val focus = binding.etFocus.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Session name required"
            return
        }
        if (selectedDateMillis == 0L) {
            binding.tilDate.error = "Please select a date"
            return
        }
        if (time.isEmpty()) {
            binding.tilTime.error = "Time required"
            return
        }
        if (location.isEmpty()) {
            binding.tilLocation.error = "Location required"
            return
        }

        binding.tilName.error = null
        binding.tilDate.error = null
        binding.tilTime.error = null
        binding.tilLocation.error = null

        showLoading(true)

        val uid = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        val sessionData = hashMapOf(
            "name" to name,
            "time" to time,
            "location" to location,
            "focus" to focus,
            "dateMillis" to selectedDateMillis,
            "status" to if (selectedDateMillis >= now) "Upcoming" else "Completed",
            "createdAt" to now
        )

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .add(sessionData)
            .addOnSuccessListener {
                showLoading(false)
                finish()
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