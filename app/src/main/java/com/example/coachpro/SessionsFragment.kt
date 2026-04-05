package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coachpro.databinding.FragmentSessionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SessionsFragment : Fragment() {

    private var _binding: FragmentSessionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: SessionAdapter

    private val allSessions = mutableListOf<Session>()
    private var showingUpcoming = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadSessions()
        setupTabs()

        binding.fabAddSession.setOnClickListener {
            startActivity(Intent(requireContext(), AddSessionActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = SessionAdapter(mutableListOf()) { session ->
            val intent = Intent(requireContext(), SessionDetailActivity::class.java)
            intent.putExtra("sessionId", session.id)
            startActivity(intent)
        }
        binding.recyclerSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSessions.adapter = adapter
    }

    private fun loadSessions() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                allSessions.clear()
                for (doc in snapshot.documents) {
                    val session = Session(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        location = doc.getString("location") ?: "",
                        focus = doc.getString("focus") ?: "",
                        dateMillis = doc.getLong("dateMillis") ?: 0L,
                        status = doc.getString("status") ?: "Upcoming"
                    )
                    allSessions.add(session)
                }

                // Date ke hisaab se sort karo
                allSessions.sortBy { it.dateMillis }
                filterSessions()
            }
    }

    private fun setupTabs() {
        binding.tabUpcoming.setOnClickListener {
            showingUpcoming = true
            binding.tabUpcoming.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabUpcoming.setTextColor(requireContext().getColor(R.color.text_white))
            binding.tabPast.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabPast.setTextColor(requireContext().getColor(R.color.text_muted))
            filterSessions()
        }

        binding.tabPast.setOnClickListener {
            showingUpcoming = false
            binding.tabPast.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabPast.setTextColor(requireContext().getColor(R.color.text_white))
            binding.tabUpcoming.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabUpcoming.setTextColor(requireContext().getColor(R.color.text_muted))
            filterSessions()
        }
    }
    private fun filterSessions() {
        val now = System.currentTimeMillis()
        val filtered = if (showingUpcoming) {
            allSessions.filter { it.dateMillis >= now }
        } else {
            allSessions.filter { it.dateMillis < now }.reversed()
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerSessions.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerSessions.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadSessions()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}