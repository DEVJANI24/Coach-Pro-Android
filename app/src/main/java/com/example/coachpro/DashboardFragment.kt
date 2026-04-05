package com.example.coachpro
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coachpro.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCoachNamw()
        loadPlayerCount()
        loadUpcomingSession()
    }

    // Coach ka naam Firebase se laao
    private fun loadCoachNamw(){
        val uid = auth.currentUser?.uid?:return

        db.collection("coaches")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name  = doc.getString("name")?: "Coach"
                binding.tvCoachName.text = "Coach $name \uD83D\uDC4B"
            }
    }

    // Players ki count Firestore se laao
    private fun loadPlayerCount(){
        val uid = auth.currentUser?.uid ?: return

        db.collection("coaches")
            .document(uid)
            .collection("players")
            .get()
            .addOnSuccessListener { result ->
                binding.tvTotalPlayers.text = result.size().toString()
            }
    }

    // Upcoming session Firestore se laao
    private fun loadUpcomingSession() {
        val uid = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .whereGreaterThan("dateMillis", now)
            .orderBy("dateMillis")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val session = result.documents[0]
                    val name = session.getString("name") ?: ""
                    val time = session.getString("time") ?: ""
                    val focus = session.getString("focus") ?: ""
                    val dateMillis = session.getLong("dateMillis") ?: now

                    // Date format karo
                    val date = Date(dateMillis)
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

                    binding.tvSessionDay.text = dayFormat.format(date).uppercase()
                    binding.tvSessionDate.text = dateFormat.format(date)
                    binding.tvSessionName.text = name
                    binding.tvSessionTime.text = time
                    binding.tvSessionFocus.text = focus

                    // Upcoming count update karo
                    binding.tvUpcomingCount.text = result.size().toString()

                } else {
                    binding.tvSessionName.text = "No upcoming session"
                    binding.tvUpcomingCount.text = "0"
                }

            }
        // Sessions this week count
        val weekStart = now - (7 * 24 * 60 * 60 * 1000)
        db.collection("coaches")
            .document(uid)
            .collection("sessions")
            .whereGreaterThan("dateMillis", weekStart)
            .get()
            .addOnSuccessListener { result ->
                binding.tvSessionsCount.text = result.size().toString()
            }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}