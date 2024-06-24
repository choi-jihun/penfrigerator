package com.example.fridge.src.calendar

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridge.R
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentCalendarBinding
import com.example.fridge.databinding.ItemDayBinding
import com.example.fridge.databinding.ItemDaySelectedBinding
import com.example.fridge.src.fridge.FoodViewModel
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.michalsvec.singlerowcalendar.utils.DateUtils.getDates
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

private const val TAG = "CalendarFragment_싸피"

class CalendarFragment : BaseFragment<FragmentCalendarBinding>(
    FragmentCalendarBinding::bind,
    R.layout.fragment_calendar
) {

    private var selectedDate: LocalDate? = null
    private lateinit var adapter: CalendarAdapter
    private val foodViewModel: FoodViewModel by activityViewModels()

    private val calendar = Calendar.getInstance()
    private var currentMonth = 0

    private val singleRowCalendar by lazy {
        binding.srcCalendarView.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            setDates(getFutureDatesOfCurrentMonth())
            init()
        }
    }

    private val myCalendarViewManager = object : CalendarViewManager {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun bindDataToCalendarView(
            holder: SingleRowCalendarAdapter.CalendarViewHolder,
            date: Date,
            position: Int,
            isSelected: Boolean
        ) {
            // bind data to calendar item views
            holder.itemView.findViewById<TextView>(R.id.tv_date).text = DateUtils.getDayNumber(date)
            holder.itemView.findViewById<TextView>(R.id.tv_day).text = DateUtils.getDay3LettersName(date)

            if (isSelected) {
                Log.d(TAG, "bindDataToCalendarView: $date")
                selectedDate = dateToLocalDate(date)
                selectedDate?.let { date ->
                    val selectedFoods = foodViewModel.getFoodDate(
                        Date.from(
                            date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                        )
                    )
                    adapter.submitList(selectedFoods)
                }
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun setCalendarViewResourceId(
            position: Int,
            date: Date,
            isSelected: Boolean
        ): Int {
            val cal = Calendar.getInstance()
            cal.time = date
            val localDate = dateToLocalDate(date)
//            Log.d(TAG, "setCalendarViewResourceId: pos[$position] selected[$isSelected]")
            Log.d(TAG, "setCalendarViewResourceId: $date")
            val food = foodViewModel.getFoodDate(
                Date.from(
                    localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
            )
            return if (isSelected)

                when (cal[Calendar.DAY_OF_WEEK]) {

                    else -> {
                        if (food.isEmpty()) {
                            R.layout.item_day_selected_empty
                        } else {
                            R.layout.item_day_selected
                        }

                    }
                }
            else
                when (cal[Calendar.DAY_OF_WEEK]) {
                    else -> {
                        if (food.isEmpty()) {
                            R.layout.item_day_empty
                        } else {
                            R.layout.item_day
                        }
                    }
                }

        }
    }

    // using calendar changes observer we can track changes in calendar
    private val myCalendarChangesObserver = object :
        CalendarChangesObserver {
        // you can override more methods, in this example we need only this one
        @RequiresApi(Build.VERSION_CODES.O)
        override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
            Log.d(
                TAG,"whenSelectionChanged: ${DateUtils.getMonthName(date)}, ${DateUtils.getDayNumber(date)}일, ${DateUtils.getDayName(date)}"
            )
            myCalendarViewManager.setCalendarViewResourceId(position, date, isSelected)

//            binding.let {
//                it.tvSelectedYear.text = "${DateUtils.getYear(date)}년"
//                it.tvSelectedMonth.text = "${DateUtils.getMonthNumber(date)}월"
//                it.tvSelectedDay.text = "${DateUtils.getDayNumber(date)}일"
//                it.tvSelectedName.text = DateUtils.getDayName(date)
//                it.tvYearAndMonth.text =
//                    "${DateUtils.getYear(date).substring(2)}년, ${DateUtils.getMonthNumber(date)}월"
//            }
            

//            tvDate.text = "${DateUtils.getMonthName(date)}, ${DateUtils.getDayNumber(date)} "
//            tvDay.text = DateUtils.getDayName(date)
            super.whenSelectionChanged(isSelected, position, date)
        }
    }

    // selection manager is responsible for managing selection
    private val mySelectionManager = object : CalendarSelectionManager {
        override fun canBeItemSelected(position: Int, date: Date): Boolean {
            return true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar.time = Date()
        currentMonth = calendar[Calendar.MONTH]
        initEvent()
        binding.tvYearAndMonth.text = getStringYearAndMonth()
        singleRowCalendar

        selectedDate = LocalDate.now()


        adapter = CalendarAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        foodViewModel.foods.observe(viewLifecycleOwner) {
            selectedDate?.let { date ->
                val selectedFoods = foodViewModel.getFoodDate(
                    Date.from(
                        date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                    )
                )
                adapter.submitList(selectedFoods)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }


    private fun initEvent() {
        binding.apply {
            btnLeft.setOnClickListener {
                singleRowCalendar.setDates(getDatesOfPreviousMonth())
                singleRowCalendar.clearSelection()
                singleRowCalendar.init()
                tvYearAndMonth.text = getStringYearAndMonth()
            }
            btnRight.setOnClickListener {
                singleRowCalendar.setDates(getDatesOfNextMonth())
                singleRowCalendar.clearSelection()
                singleRowCalendar.init()
                tvYearAndMonth.text = getStringYearAndMonth()
            }
        }
    }

    private fun getStringYearAndMonth(): String {
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        return "${year}년 ${month}월"
    }


    private fun getDatesOfNextMonth(): List<Date> {
        currentMonth++ // + because we want next month
        if (currentMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] + 1)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentMonth = calendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }


    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        list.add(calendar.time)
        while (currentMonth == calendar[Calendar.MONTH]) {
            calendar.add(Calendar.DATE, +1)
            if (calendar[Calendar.MONTH] == currentMonth)
                list.add(calendar.time)
        }
        calendar.add(Calendar.DATE, -1)
        return list
    }
}
