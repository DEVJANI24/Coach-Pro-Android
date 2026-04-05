package com.example.coachpro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coachpro.databinding.ItemPerformanceBinding

class PerformanceAdapter(
    private val list: MutableList<Performance>,
    private val onEditClick: (Performance) -> Unit
) : RecyclerView.Adapter<PerformanceAdapter.PerformanceViewHolder>() {

    inner class PerformanceViewHolder(val binding: ItemPerformanceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceViewHolder {
        val binding = ItemPerformanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PerformanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PerformanceViewHolder, position: Int) {
        val perf = list[position]
        val binding = holder.binding

        // Avatar initials
        val initials = perf.playerName
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials

        binding.tvPlayerName.text = perf.playerName
        binding.tvPlayerInfo.text = "Class ${perf.playerClass} • ${perf.playerPosition}"
        binding.tvPoints.text = perf.points.toString()
        binding.tvAssists.text = perf.assists.toString()
        binding.tvRebounds.text = perf.rebounds.toString()
        binding.tvEffort.text = "${perf.effort}/5"
        binding.tvDefense.text = "${perf.defense}/5"

        // Progress bars — width percentage calculate karo
        binding.effortBar.post {
            val parent = binding.effortBar.parent as android.widget.FrameLayout
            val totalWidth = parent.width
            binding.effortBar.layoutParams.width = (totalWidth * perf.effort / 5)
            binding.effortBar.requestLayout()
        }

        binding.defenseBar.post {
            val parent = binding.defenseBar.parent as android.widget.FrameLayout
            val totalWidth = parent.width
            binding.defenseBar.layoutParams.width = (totalWidth * perf.defense / 5)
            binding.defenseBar.requestLayout()
        }

        binding.btnEdit.setOnClickListener {
            onEditClick(perf)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Performance>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}