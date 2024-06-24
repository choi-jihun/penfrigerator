package com.example.fridge.src.fridge

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.example.fridge.databinding.ItemMainFoodBinding
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAdapter(private val onItemClicked: (Food) -> Unit) :
    ListAdapter<Food, MainAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemMainFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: Food) {
            val density = itemView.context.resources.displayMetrics.density
            val cornerRadiusInPx = (16 * density).toInt()
            binding.productTextView.text = food.proName
            binding.productImageView.load(food.img) {
                transformations(RoundedCornersTransformation(cornerRadiusInPx.toFloat()))
                scale(Scale.FIT)
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(food.expirationDate)
            binding.expirationDate.text = formattedDate

            val currentDate = Date()
            val differenceInMillis = food.expirationDate.time - currentDate.time
            val differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            binding.remainTime.text = differenceInDays.toString()

            configureCircularProgressBar(binding.circularProgress, differenceInDays)

            binding.root.setOnClickListener {
                onItemClicked(food)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMainFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun removeItem(position: Int) {
        val currentList = currentList.toMutableList()
        currentList.removeAt(position)
        submitList(currentList)
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

        private fun configureCircularProgressBar(
            progressBar: CircularProgressBar,
            daysRemaining: Int
        ) {
            progressBar.apply {
                progressMax = 100f
                val progress = daysRemaining.toFloat()
                setProgressWithAnimation(progress, 1000)

                val colorAnimator = ValueAnimator.ofFloat(0f, 100f)
                colorAnimator.duration = 1000
                colorAnimator.addUpdateListener { animator ->
                    val color = getColorForDaysRemaining(daysRemaining.toFloat())
                    progressBarColor = color
                }

                val percentage = (daysRemaining / 100f).coerceIn(0f, 1f)
                colorAnimator.currentPlayTime = (percentage * colorAnimator.duration).toLong()
                colorAnimator.start()
            }
        }

        private fun getColorForDaysRemaining(percent: Float): Int {
            val red: Int
            val green: Int

            when {
                percent <= 50 -> {
                    red = 255
                    green = (percent * 5.1f).toInt()
                }

                percent <= 100 -> {
                    red = ((100 - percent) * 5.1f).toInt()
                    green = 255
                }

                else -> {
                    red = 0
                    green = 255
                }
            }

            return Color.rgb(red, green, 0)
        }
    }
}