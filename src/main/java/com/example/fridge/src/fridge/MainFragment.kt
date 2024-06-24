package com.example.fridge.src.fridge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridge.LoginActivity
import com.example.fridge.MainActivity
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.R
import com.example.fridge.util.SwipeToDeleteCallback
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentMainBinding
import com.example.fridge.util.SortOrder

private const val TAG = "MainFragment_싸피"

class MainFragment :
    BaseFragment<FragmentMainBinding>(FragmentMainBinding::bind, R.layout.fragment_main) {

    private lateinit var adapter: MainAdapter
    private val foodViewModel: FoodViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MainAdapter {
            foodViewModel.selectFood(it)
            findNavController().navigate(R.id.action_mainFragment_to_editFragment)
        }

        binding.imgBtnLogout.setOnClickListener {
            ApplicationClass.sharedPreferences.clear()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.rvMainList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMainList.adapter = adapter

        val uid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        uid?.let {
            foodViewModel.fetchFoods(it)
        }

        binding.sortByASC.setOnClickListener {
            foodViewModel.setSortOrder(SortOrder.ASCENDING)
            uid?.let {
                foodViewModel.fetchFoods(it)
            }
        }

        binding.sortByDESC.setOnClickListener {
            foodViewModel.setSortOrder(SortOrder.DESCENDING)
            uid?.let {
                foodViewModel.fetchFoods(it)
            }
        }

        binding.sortByDate.setOnClickListener {
            foodViewModel.setSortOrder(SortOrder.BY_DATE)
            uid?.let {
                foodViewModel.fetchFoods(it)
            }
        }

        binding.addFood.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_editFragment)
        }


        foodViewModel.foods.observe(viewLifecycleOwner) { foodList ->
            adapter.submitList(foodList)
        }
        foodViewModel.clearSelectedFood()

        val itemTouchHelper =
            ItemTouchHelper(SwipeToDeleteCallback(requireContext(), adapter, foodViewModel))
        itemTouchHelper.attachToRecyclerView(binding.rvMainList)
    }

}