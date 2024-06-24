package com.example.fridge.src.fridge

import android.util.Log
import com.example.fridge.util.Constants
import com.example.fridge.util.SortOrder
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "FoodService_싸피"

class FoodService {
    private val database = FirebaseDatabase.getInstance()

    private fun getUserFoodsRef(uid: String) =
        database.reference.child(Constants.USERS_COLLECTION).child(uid).child(Constants.USER_FOOD)

    private suspend fun fetchUserFoods(uid: String): List<Food> {
        val snapshot = getUserFoodsRef(uid).get().await()
        val foodList = mutableListOf<Food>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        snapshot.children.forEach { foodSnapshot ->
            val foodMap = foodSnapshot.value as? Map<*, *>
            if (foodMap != null) {
                try {
                    val food = Food(
                        id = foodSnapshot.key ?: "",
                        proName = foodMap["proName"] as? String ?: "",
                        img = foodMap["img"] as? String ?: "",
                        date = dateFormat.parse(foodMap["date"] as? String ?: "") ?: Date(),
                        category = foodMap["category"] as? String ?: "",
                        expirationDate = dateFormat.parse(
                            foodMap["expirationDate"] as? String ?: ""
                        ) ?: Date(),
                        remainAmount = (foodMap["remainingAmount"] as? String)?.toIntOrNull() ?: 0
                    )
                    foodList.add(food)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing food item", e)
                }
            } else {
                Log.e(TAG, "foodMap is null for dataSnapshot: ${foodSnapshot.key}")
            }
        }

        return foodList
    }

    suspend fun getUserFoods(uid: String, sortOrder: SortOrder): List<Food> {
        val foodList = fetchUserFoods(uid)
        return when (sortOrder) {
            SortOrder.BY_DATE -> foodList.sortedBy { it.date }
            SortOrder.ASCENDING -> foodList.sortedBy { it.expirationDate }
            SortOrder.DESCENDING -> foodList.sortedByDescending { it.expirationDate }
        }
    }

    suspend fun saveUserFood(uid: String, food: Food) {
        val foodRef = if (food.id.isEmpty()) {
            getUserFoodsRef(uid).push()
        } else {
            getUserFoodsRef(uid).child(food.id)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val foodData = mapOf(
            "proName" to food.proName,
            "img" to food.img,
            "date" to dateFormat.format(food.date),
            "category" to food.category,
            "expirationDate" to dateFormat.format(food.expirationDate),
            "remainingAmount" to food.remainAmount.toString()
        )

        foodRef.setValue(foodData).await()
    }

    suspend fun deleteUserFood(uid: String, foodId: String) {
        getUserFoodsRef(uid).child(foodId).removeValue().await()
    }
}
