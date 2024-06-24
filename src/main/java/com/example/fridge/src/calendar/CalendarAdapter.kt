package com.example.fridge.src.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.example.fridge.src.fridge.Food
import com.example.fridge.databinding.ItemCalendarFoodBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarAdapter : ListAdapter<Food, CalendarAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemCalendarFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.productTextView.text = food.proName
            binding.productImageView.load(food.img) {
                scale(Scale.FILL)
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.expirationDate.text = dateFormat.format(food.expirationDate)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCalendarFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}