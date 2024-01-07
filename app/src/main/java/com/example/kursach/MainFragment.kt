package com.example.kursach

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kursach.databinding.FragmentMainBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var textDate: TextView
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        textDate = binding.textDate
        setCurrentWeek()
        Log.d("Week", "Current Week: $currentWeek")
        loadSchedule("12002108", currentWeek)
        binding.iconLeft.setOnClickListener {
            loadNextWeek(false)
        }

        binding.iconRight.setOnClickListener {
            loadNextWeek(true)
        }
        return binding.root
    }

    private fun setCurrentWeek() {
        val formatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val startCalendar = Calendar.getInstance()
        startCalendar.time = currentDate

        // Если текущий день не понедельник, установим начало недели на предыдущий понедельник
        if (startCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            startCalendar.add(Calendar.WEEK_OF_YEAR, -1)
        }

        val endCalendar = Calendar.getInstance()
        endCalendar.time = startCalendar.time
        endCalendar.add(
            Calendar.DATE, 6
        ) // Добавим 6 дней, чтобы получить воскресенье текущей недели

        val startDateString = formatter.format(startCalendar.time)
        val endDateString = formatter.format(endCalendar.time)

        currentWeek = "$startDateString$endDateString"

        textDate.text = "$startDateString - $endDateString"
    }


    private fun loadNextWeek(isNext: Boolean) {
        val formatter = SimpleDateFormat("ddMMyyyy", Locale.getDefault())

        try {
            val startDate = formatter.parse(currentWeek.substring(0, 8))
            val endDate = formatter.parse(currentWeek.substring(8))

            val startCalendar = Calendar.getInstance()
            startCalendar.time = startDate

            val endCalendar = Calendar.getInstance()
            endCalendar.time = endDate

            if (isNext) {
                startCalendar.add(Calendar.DATE, 7)
                endCalendar.add(Calendar.DATE, 7)
            } else {
                startCalendar.add(Calendar.DATE, -7)
                endCalendar.add(Calendar.DATE, -7)
            }

            val newStartDate = formatter.format(startCalendar.time)
            val newEndDate = formatter.format(endCalendar.time)

            currentWeek = "$newStartDate$newEndDate"

            val currentDateString =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startCalendar.time)
            val endDateString =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endCalendar.time)

            textDate.text = "$currentDateString - $endDateString"

            Log.d("WeekSwitch", "Switched to week: $currentWeek")

            loadSchedule("12002108", currentWeek)
        } catch (e: ParseException) {
            Log.e("WeekSwitch", "Error parsing date", e)
        }
    }

    private var currentWeek: String = "0101202407012024"

    private fun loadSchedule(group: String, week: String) {
        viewModel.loadSchedule(group, week)
        viewModel.scheduleItems.observe(viewLifecycleOwner) {
            updateScheduleUI(it)
        }
    }

    private fun updateScheduleUI(scheduleItems: List<ScheduleItem>) {
        val recyclerView: RecyclerView = binding.scheduleRecyclerView
        if (scheduleItems.isEmpty()) {
            val noClassesTextView: TextView = binding.noClassesTextView
            noClassesTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            val adapter = ScheduleAdapter(scheduleItems)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val noClassesTextView: TextView = binding.noClassesTextView
            noClassesTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

}