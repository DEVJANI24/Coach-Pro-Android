package com.example.coachpro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coachpro.databinding.ItemPlayerBinding

class PlayerAdapter(
    private val players: MutableList<Player>,
    private val onPlayerClick: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>(){

    inner class PlayerViewHolder(val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        val binding = holder.binding

        // Avatar — naam ke pehle 2 letters
        val initials = player.name
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials

        // Player info
        binding.tvPlayerName.text = player.name
        binding.tvPlayerInfo.text = "Class ${player.className} • ${player.position}"

        // Status badge
        if (player.status == "Active") {
            binding.tvStatus.text = "Active"
            binding.tvStatus.setTextColor(
                holder.itemView.context.getColor(R.color.status_green)
            )
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
        } else {
            binding.tvStatus.text = player.status
            binding.tvStatus.setTextColor(
                holder.itemView.context.getColor(R.color.status_red)
            )
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
        }

        // Click listener
        holder.itemView.setOnClickListener {
            onPlayerClick(player)
        }
    }
        override fun getItemCount() = players.size

    // Naya player list mein add karo
    fun updateList(newPlayers: List<Player>) {
        players.clear()
        players.addAll(newPlayers)
        notifyDataSetChanged()
    }
}