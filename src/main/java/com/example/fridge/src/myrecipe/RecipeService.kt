package com.example.fridge.src.myrecipe

import android.util.Log
import com.example.fridge.util.Constants
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

private const val TAG = "RecipeService_싸피"

class RecipeService {
    private val database = FirebaseDatabase.getInstance()

    private fun getUserRecipesRef(uid: String) =
        database.reference.child(Constants.USERS_COLLECTION).child(uid).child(Constants.USER_RECIPE)

    suspend fun getUserRecipes(uid: String): List<Recipe> {
        val snapshot = getUserRecipesRef(uid).get().await()
        val recipeList = mutableListOf<Recipe>()

        snapshot.children.forEach { recipeSnapshot ->
            val recipeMap = recipeSnapshot.value as? Map<*, *>
            if (recipeMap != null) {
                try {
                    val recipe = Recipe(
                        id = recipeSnapshot.key ?: "",
                        foodName = recipeMap["foodName"] as? String ?: "",
                        img = recipeMap["img"] as? String ?: "",
                        ingredients = recipeMap["ingredients"] as? String ?: "",
                        recipe = recipeMap["recipe"] as? String ?: ""
                    )
                    recipeList.add(recipe)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing recipe item", e)
                }
            } else {
                Log.e(TAG, "recipeMap is null for dataSnapshot: ${recipeSnapshot.key}")
            }
        }

        return recipeList
    }

    suspend fun saveUserRecipe(uid: String, recipe: Recipe) {
        val recipeRef = if (recipe.id.isEmpty()) {
            getUserRecipesRef(uid).push()
        } else {
            getUserRecipesRef(uid).child(recipe.id)
        }

        val recipeData = mapOf(
            "foodName" to recipe.foodName,
            "img" to recipe.img,
            "ingredients" to recipe.ingredients,
            "recipe" to recipe.recipe
        )

        recipeRef.setValue(recipeData).await()
    }

    suspend fun deleteUserRecipe(uid: String, recipeId: String) {
        getUserRecipesRef(uid).child(recipeId).removeValue().await()
    }
}
