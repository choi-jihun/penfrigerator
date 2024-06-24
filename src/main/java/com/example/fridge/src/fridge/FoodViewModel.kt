package com.example.fridge.src.fridge

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridge.util.Constants
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.SortOrder
import kotlinx.coroutines.launch
import java.util.Date

private const val TAG = "FoodViewModel_싸피"

class FoodViewModel : ViewModel() {
    private val _foods = MutableLiveData<List<Food>>()
    val foods: LiveData<List<Food>> get() = _foods

    private val _selectedFood = MutableLiveData<Food?>()
    val selectedFood: LiveData<Food?> get() = _selectedFood

    private var selectedFoodId: String? = null
    private val foodService = FoodService()

    private val _sortOrder = MutableLiveData<SortOrder>()

    fun fetchFoods(uid: String) {
        viewModelScope.launch {
            val foodList = foodService.getUserFoods(uid, _sortOrder.value ?: SortOrder.BY_DATE)
            _foods.value = foodList
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun saveUserFood(uid: String, food: Food) {
        viewModelScope.launch {
            try {
                foodService.saveUserFood(uid, food)
                fetchFoods(uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving food", e)
            }
        }
    }

    fun deleteFood(food: Food) {
        val userUid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        userUid?.let { uid ->
            viewModelScope.launch {
                try {
                    foodService.deleteUserFood(uid, food.id)
                    fetchFoods(uid)
                } catch (e: Exception) {
                    Log.d(TAG, "Error deleting food", e)
                }
            }
        }
    }

    fun selectFood(food: Food) {
        _selectedFood.value = food
        selectedFoodId = food.id
    }

    fun clearSelectedFood() {
        _selectedFood.value = null
        selectedFoodId = null
    }

    fun getSelectedFoodId(): String? {
        return selectedFoodId
    }

    fun getFoodDate(date: Date): List<Food> {
        return _foods.value?.filter { it.expirationDate == date } ?: emptyList()
    }
}