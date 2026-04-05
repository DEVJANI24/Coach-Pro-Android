package com.example.coachpro

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coachpro.databinding.FragmentPlayersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayersFragment : Fragment() {

    private var _binding: FragmentPlayersBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PlayerAdapter

    private val allPlayers = mutableListOf<Player>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadPlayers()
        setupSearch()

        // FAB — Add Player screen pe jaao
        binding.fabAddPlayer.setOnClickListener {
            startActivity(Intent(requireContext(), AddPlayerActivity::class.java))
        }
    }
    private fun setupRecyclerView() {
        adapter = PlayerAdapter(mutableListOf()) { player ->
            // Player click — Detail screen pe jaao
            val intent = Intent(requireContext(), PlayerDetailActivity::class.java)
            intent.putExtra("playerId", player.id)
            intent.putExtra("playerName", player.name)
            startActivity(intent)
        }

        binding.recyclerPlayers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlayers.adapter = adapter
    }

    private fun loadPlayers() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("players")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                allPlayers.clear()
                for (doc in snapshot.documents) {
                    val player = Player(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        age = doc.getString("age") ?: "",
                        className = doc.getString("className") ?: "",
                        position = doc.getString("position") ?: "",
                        parentName = doc.getString("parentName") ?: "",
                        parentPhone = doc.getString("parentPhone") ?: "",
                        status = doc.getString("status") ?: "Active",
                        coachNotes = doc.getString("coachNotes") ?: ""
                    )
                    allPlayers.add(player)
                }

                adapter.updateList(allPlayers)
                updateEmptyState()

                // Player count update karo
                binding.tvPlayerCount.text = "${allPlayers.size} players"
            }
    }
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = allPlayers.filter {
                    it.name.lowercase().contains(query) ||
                            it.className.lowercase().contains(query) ||
                            it.position.lowercase().contains(query)
                }
                adapter.updateList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateEmptyState() {
        if (allPlayers.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerPlayers.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerPlayers.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlayers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}