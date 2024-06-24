package com.example.fridge.src.myrecipe

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.src.FirebaseStorageService
import kotlinx.coroutines.launch

private const val TAG = "RecipeViewModel_싸피"

class RecipeViewModel : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    private val _selectedRecipe = MutableLiveData<Recipe?>()
    val selectedRecipe: LiveData<Recipe?> get() = _selectedRecipe

    private val recipeService = RecipeService()
    private val firebaseStorageService = FirebaseStorageService()

    fun loadUserRecipes(uid: String) {
        viewModelScope.launch {
            try {
                val recipeList = recipeService.getUserRecipes(uid)
                _recipes.value = recipeList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipes", e)
            }
        }
    }

    fun saveUserRecipe(uid: String, recipe: Recipe, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val imgUrl = imageUri?.let {
                    firebaseStorageService.uploadImage(it, "images/${System.currentTimeMillis()}.jpg")
                } ?: recipe.img

                val newRecipe = recipe.copy(img = imgUrl)
                recipeService.saveUserRecipe(uid, newRecipe)
                loadUserRecipes(uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving recipe", e)
            }
        }
    }

    fun deleteUserRecipe(recipe: Recipe) {
        val userUid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        userUid?.let { uid ->
            viewModelScope.launch {
                try {
                    recipeService.deleteUserRecipe(uid, recipe.id)
                    loadUserRecipes(uid)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting recipe", e)
                }
            }
        }
    }

    fun selectRecipe(recipe: Recipe) {
        _selectedRecipe.value = recipe
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }
}
