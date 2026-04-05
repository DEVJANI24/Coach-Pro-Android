package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.coachpro.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadProfileData()
        loadStats()

        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: "—"

        // Email fields set karo
        binding.tvEmail.text = email
        binding.tvEmailInfo.text = email

        // Firestore se naam aur school laao
        db.collection("coaches")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Coach"
                val school = doc.getString("school") ?: "—"

                // Avatar — naam ka pehla letter
                binding.tvAvatar.text = name.first().uppercase()

                binding.tvCoachName.text = name
                binding.tvSchool.text = school
                binding.tvName.text = name
                binding.tvSchoolInfo.text = school
            }
    }

    private fun loadStats() {
        val uid = auth.currentUser?.uid ?: return

        // Players count
        db.collection("coaches")
            .document(uid)
            .collection("players")
            .get()
            .addOnSuccessListener { result ->
                binding.tvTotalPlayers.text = result.size().toString()
            }

        // Sessions count
        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .get()
            .addOnSuccessListener { result ->
                binding.tvTotalSessions.text = result.size().toString()
            }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        // Login screen pe redirect
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}