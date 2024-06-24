package com.example.fridge.src.myrecipe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.fridge.MainActivity
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.util.ImagePickerUtil
import com.example.fridge.R
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentRecipeDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailBinding>(
    FragmentRecipeDetailBinding::bind,
    R.layout.fragment_recipe_detail
) {

    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var imageUri: Uri? = null
    private lateinit var imagePickerUtil: ImagePickerUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imagePickerUtil = ImagePickerUtil(this)

        recipeViewModel.selectedRecipe.observe(viewLifecycleOwner) { recipe ->
            if (recipe != null) {
                bindRecipe(recipe)
            }
        }

        binding.foodImageView.setOnClickListener {
            imagePickerUtil.showImagePickerDialog()
        }

        binding.floatingButton.setOnClickListener {
            saveRecipeToFirebase()
        }
    }

    private fun bindRecipe(recipe: Recipe) {
        binding.foodImageView.load(recipe.img)
        binding.etFoodName.setText(recipe.foodName)
        binding.ingredients.setText(recipe.ingredients)
        binding.recipe.setText(recipe.recipe)
    }

    private fun saveRecipeToFirebase() {
        val foodName = binding.etFoodName.text.toString()
        val ingredients = binding.ingredients.text.toString()
        val recipeText = binding.recipe.text.toString()

        val newRecipe = Recipe(
            id = recipeViewModel.selectedRecipe.value?.id ?: "",
            foodName = foodName,
            img = recipeViewModel.selectedRecipe.value?.img ?: "",
            ingredients = ingredients,
            recipe = recipeText
        )

        val uid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        if (uid != null) {
            binding.floatingButton.showProgress()

            lifecycleScope.launch {
                (requireActivity() as MainActivity).showLoadingDialog()
                recipeViewModel.saveUserRecipe(uid, newRecipe, imageUri)
                delay(2000) // Simulate network delay
                withContext(Dispatchers.Main) {
                    binding.floatingButton.showCheckAnimation()
                    delay(1000) // Allow check animation to play
                    binding.floatingButton.resetIcon()
                    findNavController().navigate(R.id.action_recipeDetailFragment_to_nav_recipe)
                    (requireActivity() as MainActivity).hideLoadingDialog()
                }

            }
        } else {
            showToast( "User ID not found. Please log in again.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = imagePickerUtil.handleActivityResult(requestCode, resultCode, data)
        uri?.let {
            imageUri = it
            binding.foodImageView.setImageURI(it)
        }
    }
}
