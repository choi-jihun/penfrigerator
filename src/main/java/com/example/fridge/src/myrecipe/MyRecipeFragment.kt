package com.example.fridge.src.myrecipe

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.R
import com.example.fridge.util.SwipeToDeleteCallback
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentMyRecipeBinding

class MyRecipeFragment : BaseFragment<FragmentMyRecipeBinding>(
    FragmentMyRecipeBinding::bind,
    R.layout.fragment_my_recipe
) {
    private lateinit var adapter: MyRecipeAdapter
    private val recipeViewModel: RecipeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MyRecipeAdapter {
            recipeViewModel.selectRecipe(it)
            findNavController().navigate(R.id.action_nav_recipe_to_recipeDetailFragment)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val uid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        if (uid != null) {
            recipeViewModel.loadUserRecipes(uid)
        }

        recipeViewModel.recipes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        recipeViewModel.clearSelectedRecipe()

        binding.floatingButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_recipe_to_recipeDetailFragment)
        }

        val itemTouchHelper =
            ItemTouchHelper(
                SwipeToDeleteCallback(
                    requireContext(),
                    adapter,
                    recipeViewModel = recipeViewModel
                )
            )
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}
