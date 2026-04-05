package com.example.coachpro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coachpro.databinding.ItemAttendanceBinding

class AttendanceAdapter(
    private val attendanceList: MutableList<Attendance>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = attendanceList[position]
        val binding = holder.binding
        val context = holder.itemView.context

        // Avatar initials
        val initials = item.playerName
            .split(" ")
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials

        binding.tvPlayerName.text = item.playerName

        // Button state update
        updateButtons(binding, item.isPresent, context)

        // Present click
        binding.btnPresent.setOnClickListener {
            item.isPresent = true
            updateButtons(binding, true, context)
        }

        // Absent click
        binding.btnAbsent.setOnClickListener {
            item.isPresent = false
            updateButtons(binding, false, context)
        }
    }

    private fun updateButtons(
        binding: ItemAttendanceBinding,
        isPresent: Boolean,
        context: android.content.Context
    ) {
        if (isPresent) {
            // Present selected
            binding.btnPresent.setBackgroundResource(R.drawable.bg_btn_present_sel)
            binding.btnPresent.setTextColor(context.getColor(R.color.text_white))
            // Absent unselected
            binding.btnAbsent.setBackgroundResource(R.drawable.bg_btn_absent_unsel)
            binding.btnAbsent.setTextColor(context.getColor(R.color.status_red))
        } else {
            // Absent selected
            binding.btnAbsent.setBackgroundResource(R.drawable.bg_btn_absent_sel)
            binding.btnAbsent.setTextColor(context.getColor(R.color.text_white))
            // Present unselected
            binding.btnPresent.setBackgroundResource(R.drawable.bg_btn_present_unsel)
            binding.btnPresent.setTextColor(context.getColor(R.color.status_green))
        }
    }

    override fun getItemCount() = attendanceList.size

    // Attendance data wapas lo — save karne ke liye
    fun getAttendanceData(): List<Attendance> = attendanceList
}