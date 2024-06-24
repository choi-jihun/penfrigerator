package com.example.fridge.src.gpt

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.util.ImagePickerUtil
import com.example.fridge.util.ProgressFloatingActionButton
import com.example.fridge.R
import com.example.fridge.src.myrecipe.Recipe
import com.example.fridge.chatGpt.Sender
import com.example.fridge.chatGpt.client.RetrofitClient
import com.example.fridge.chatGpt.dto.ChatContent
import com.example.fridge.chatGpt.dto.ChatRequest
import com.example.fridge.chatGpt.dto.ChatResponse
import com.example.fridge.chatGpt.dto.Message
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentGptBinding
import com.example.fridge.src.fridge.FoodViewModel
import com.example.fridge.src.myrecipe.RecipeViewModel
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "GptFragment_싸피"

class GptFragment :
    BaseFragment<FragmentGptBinding>(FragmentGptBinding::bind, R.layout.fragment_gpt) {

    private lateinit var gptAdapter: GptAdapter
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val foodViewModel: FoodViewModel by activityViewModels()
    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private var selectedChatContent: ChatContent? = null
    private lateinit var imagePickerUtil: ImagePickerUtil
    private var imageUri: Uri? = null
    private var dialogView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePickerUtil = ImagePickerUtil(this)
        gptAdapter = GptAdapter { chatContent ->
            if (chatContent.sender == Sender.GPT) {
                selectedChatContent = chatContent
                selectedChatContent!!.content?.let { parseAndNavigateToRecipeDetail(it) }
            }
        }
        binding.rvText.adapter = gptAdapter
        initEvent()
        observeViewModel()

        val userUid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        userUid?.let {
            chatViewModel.loadUserMessages(it)
            foodViewModel.fetchFoods(it)
        }
    }

    private fun initEvent() {
        binding.apply {
            btnSend.setOnClickListener {
                val inputText = etUserInput.text.toString()
                if (inputText.isNotBlank()) {
                    val userMessage = ChatContent(sender = Sender.USER, content = inputText)
                    chatViewModel.saveUserMessage(userMessage)
                    sendMessageToAPI(inputText)
                    etUserInput.text?.clear()
                }
            }
        }
    }

    private fun parseAndNavigateToRecipeDetail(botReply: String) {
        val recipeName = botReply.substringAfter("***").substringBefore("***").ifEmpty { null }
        val ingredients = botReply.substringAfter("@@@").substringBefore("@@@").ifEmpty { null }
        val recipeInstructions =
            botReply.substringAfter("###").substringBefore("###").ifEmpty { null }

        if (recipeName != null && ingredients != null && recipeInstructions != null) {
            val recipe = Recipe(
                foodName = recipeName,
                img = "",
                ingredients = ingredients,
                recipe = recipeInstructions
            )

            showRecipeDialog(recipe)
        }
    }

    private fun showRecipeDialog(recipe: Recipe) {
        dialogView = layoutInflater.inflate(R.layout.fragment_recipe_detail, null)

        val foodImageView: ImageView = dialogView!!.findViewById(R.id.foodImageView)
        val foodNameEditText: TextInputEditText = dialogView!!.findViewById(R.id.et_food_name)
        val ingredientsEditText: TextInputEditText = dialogView!!.findViewById(R.id.ingredients)
        val recipeEditText: TextInputEditText = dialogView!!.findViewById(R.id.recipe)
        val floatingButton: ProgressFloatingActionButton = dialogView!!.findViewById(R.id.floatingButton)
        floatingButton.visibility = View.GONE

        foodNameEditText.setText(recipe.foodName)
        ingredientsEditText.setText(recipe.ingredients)
        recipeEditText.setText(recipe.recipe)

        foodImageView.setOnClickListener {
            imagePickerUtil.showImagePickerDialog()
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
            .setPositiveButton("저장") { dialog, _ ->
                saveRecipeToFirebase(foodNameEditText, ingredientsEditText, recipeEditText)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun saveRecipeToFirebase(
        foodNameEditText: TextInputEditText,
        ingredientsEditText: TextInputEditText,
        recipeEditText: TextInputEditText
    ) {
        val foodName = foodNameEditText.text.toString()
        val ingredients = ingredientsEditText.text.toString()
        val recipeText = recipeEditText.text.toString()

        val newRecipe = Recipe(
            foodName = foodName,
            img = recipeViewModel.selectedRecipe.value?.img ?: "",
            ingredients = ingredients,
            recipe = recipeText
        )

        val uid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        if (uid != null) {
            recipeViewModel.saveUserRecipe(uid, newRecipe, imageUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = imagePickerUtil.handleActivityResult(requestCode, resultCode, data)
        uri?.let {
            imageUri = it
            val foodImageView: ImageView? = dialogView?.findViewById(R.id.foodImageView)
            foodImageView?.setImageURI(it)
        }
    }

    private fun sendMessageToAPI(userMessage: String) {
        val foodNames = foodViewModel.foods.value?.joinToString(", ") { it.proName } ?: ""
        val messageContent = if (foodNames.isNotBlank()) {
            "$userMessage\n\n현재 냉장고에 있는 음식들: $foodNames\n\n요리명은 ***\n\n메인재료는 @@@\n\n 레시피는 ###으로 둘러싸서 대답해줘"
        } else {
            userMessage
        }

        Log.d(TAG, "sendMessageToAPI: $messageContent")
        val chatRequest = ChatRequest(
            messages = listOf(Message(role = "user", content = messageContent)),
            max_tokens = 2000, // 필요에 따라 이 값을 조정하세요.
            temperature = 0.7
        )

        RetrofitClient.instance.getChatResponse(chatRequest)
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(
                    call: Call<ChatResponse>,
                    response: Response<ChatResponse>
                ) {
                    if (response.isSuccessful) {
                        val chatResponse = response.body()
                        val botReply = chatResponse?.choices?.get(0)?.message?.content?.trim()
                        Log.d(TAG, "onResponse: $botReply")
                        val botMessage = ChatContent(sender = Sender.GPT, content = botReply)
                        chatViewModel.saveUserMessage(botMessage)
                    } else {
                        Log.d(TAG, "onResponse: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
    }

    private fun observeViewModel() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            gptAdapter.submitList(messages.toList())
            binding.rvText.post {
                scrollToBottom()
            }
        }
    }

    // RecyclerView를 맨 밑으로 스크롤하는 함수 정의
    private fun scrollToBottom() {
        binding.rvText.scrollToPosition(gptAdapter.itemCount - 1)
    }
}
