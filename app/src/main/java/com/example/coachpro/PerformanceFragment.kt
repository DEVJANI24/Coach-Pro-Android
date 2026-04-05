package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coachpro.databinding.FragmentPerformanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerformanceFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PerformanceAdapter

    private val sessionList = mutableListOf<Session>()
    private var selectedSessionId: String = ""
    private var selectedSessionName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadSessions()

        // Session picker click
        binding.cardSessionPicker.setOnClickListener {
            showSessionPicker()
        }

        // FAB — Add Performance
        binding.fabAddPerformance.setOnClickListener {
            val intent = Intent(requireContext(), AddPerformanceActivity::class.java)
            intent.putExtra("sessionId", selectedSessionId)
            intent.putExtra("sessionName", selectedSessionName)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = PerformanceAdapter(mutableListOf()) { perf ->
            // Edit click — AddPerformance screen pe jaao prefilled data ke saath
            val intent = Intent(requireContext(), AddPerformanceActivity::class.java)
            intent.putExtra("sessionId", selectedSessionId)
            intent.putExtra("sessionName", selectedSessionName)
            intent.putExtra("performanceId", perf.id)
            intent.putExtra("playerId", perf.playerId)
            intent.putExtra("playerName", perf.playerName)
            intent.putExtra("points", perf.points)
            intent.putExtra("assists", perf.assists)
            intent.putExtra("rebounds", perf.rebounds)
            intent.putExtra("effort", perf.effort)
            intent.putExtra("defense", perf.defense)
            startActivity(intent)
        }
        binding.recyclerPerformance.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPerformance.adapter = adapter
    }

    private fun loadSessions() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                sessionList.clear()
                for (doc in snapshot.documents) {
                    sessionList.add(
                        Session(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            dateMillis = doc.getLong("dateMillis") ?: 0L
                        )
                    )
                }
                // Latest pehle
                sessionList.sortByDescending { it.dateMillis }
            }
    }

    private fun showSessionPicker() {
        if (sessionList.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("No Sessions")
                .setMessage("Pehle Sessions tab mein session add karo!")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val names = sessionList.map {
            "${it.name} — ${format.format(Date(it.dateMillis))}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Session")
            .setItems(names) { _, index ->
                selectedSessionId = sessionList[index].id
                selectedSessionName = sessionList[index].name
                binding.tvSelectedSession.text = sessionList[index].name
                loadPerformanceForSession()
            }
            .show()
    }

    private fun loadPerformanceForSession() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .document(selectedSessionId)
            .collection("performance")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Performance>()
                for (doc in snapshot.documents) {
                    list.add(
                        Performance(
                            id = doc.id,
                            playerId = doc.getString("playerId") ?: "",
                            playerName = doc.getString("playerName") ?: "",
                            playerClass = doc.getString("playerClass") ?: "",
                            playerPosition = doc.getString("playerPosition") ?: "",
                            points = doc.getLong("points")?.toInt() ?: 0,
                            assists = doc.getLong("assists")?.toInt() ?: 0,
                            rebounds = doc.getLong("rebounds")?.toInt() ?: 0,
                            effort = doc.getLong("effort")?.toInt() ?: 0,
                            defense = doc.getLong("defense")?.toInt() ?: 0
                        )
                    )
                }

                adapter.updateList(list)

                if (list.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.recyclerPerformance.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.recyclerPerformance.visibility = View.VISIBLE
                }

                // FAB show karo
                binding.fabAddPerformance.visibility = View.VISIBLE
            }
    }

    override fun onResume() {
        super.onResume()
        loadSessions()
        if (selectedSessionId.isNotEmpty()) {
            loadPerformanceForSession()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}