package com.example.coachpro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coachpro.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionAdapter(
    private val sessions: MutableList<Session>,
    private val onSessionClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(val binding: ItemSessionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        val binding = holder.binding

        val date = Date(session.dateMillis)
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        binding.tvDay.text = dayFormat.format(date).uppercase()
        binding.tvDate.text = dateFormat.format(date)
        binding.tvSessionName.text = session.name
        binding.tvSessionTime.text = session.time
        binding.tvSessionLocation.text = "📍 ${session.location}"
        binding.tvSessionFocus.text = session.focus
        binding.tvStatus.text = session.status

        val context = holder.itemView.context
        if (session.status == "Completed") {
            binding.tvStatus.setTextColor(context.getColor(R.color.status_green))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
            binding.layoutDateBox.setBackgroundResource(R.drawable.bg_date_box_grey)
        } else {
            binding.tvStatus.setTextColor(context.getColor(R.color.accent_orange))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange)
            binding.layoutDateBox.setBackgroundResource(R.drawable.bg_date_box)
        }

        holder.itemView.setOnClickListener {
            onSessionClick(session)
        }
    }

    override fun getItemCount() = sessions.size

    fun updateList(newSessions: List<Session>) {
        sessions.clear()
        sessions.addAll(newSessions)
        notifyDataSetChanged()
    }
}