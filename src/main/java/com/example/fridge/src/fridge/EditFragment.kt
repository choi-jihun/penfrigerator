package com.example.fridge.src.fridge

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.ashraf007.expandableselectionview.adapter.BasicStringAdapter
import com.example.fridge.MainActivity
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.R
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentEditBinding
import com.example.fridge.src.FirebaseStorageService
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "EditFragment_싸피"

class EditFragment :
    BaseFragment<FragmentEditBinding>(FragmentEditBinding::bind, R.layout.fragment_edit) {
    private lateinit var loadingDialog: Dialog
    private lateinit var currentPhotoPath: String
    private var photoUri: Uri? = null
    private val firebaseStorageService = FirebaseStorageService()
    private val foodViewModel: FoodViewModel by activityViewModels()
    private lateinit var selectedExpirationDate: Date
    private var selectedCategory: String = "기타"
    private val categories = listOf(
        "\uD83C\uDF4E 과일",
        "\uD83E\uDD6C 채소",
        "\uD83C\uDF3E 쌀/잡곡/견과",
        "\uD83E\uDD69 정육/계란",
        "\uD83D\uDC1F 수산/건어물",
        "\uD83E\uDD5B 우유/유제품",
        "\uD83C\uDF5C 간편식",
        "\uD83C\uDF36\uFE0F 김치/반찬",
        "\uD83E\uDDC2 장/조미료",
        "\uD83C\uDF6A 과자/빙과",
        "\uD83E\uDD61 기타"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        foodViewModel.selectedFood.observe(viewLifecycleOwner) { food ->
            food?.let {
                binding.etFoodName.setText(it.proName)
                binding.etFoodExpirationDate.setText(
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(it.expirationDate)
                )
                binding.etFoodRemainingAmount.setText(it.remainAmount.toString())
                binding.ivFoodImage.load(it.img)
                selectedExpirationDate = it.expirationDate
                selectedCategory = it.category
                currentPhotoPath = it.img

                val selectedIndex = categories.indexOf(selectedCategory)
                if (selectedIndex != -1) {
                    binding.singleSelectionView.selectIndex(selectedIndex, notifyListener = false)
                }
            }
        }

        val expandableAdapter = BasicStringAdapter(categories, "카테고리를 선택하세요")
        binding.singleSelectionView.setAdapter(expandableAdapter)

        binding.singleSelectionView.selectionListener = { index: Int? ->
            selectedCategory = if (index != null) categories[index] else ""
        }

        binding.ivFoodImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.etFoodExpirationDate.setOnClickListener {
            showDatePickerDialog(binding.etFoodExpirationDate)
        }

        binding.floatingButton.setOnClickListener {
            if (::currentPhotoPath.isInitialized || photoUri != null) {
                binding.floatingButton.showProgress()
                saveDataToFirebase()
            } else {
                showToast("Please select an image before saving.")
            }
        }
    }




    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    startBarcodeScanner()
                }

                options[item] == "Choose from Gallery" -> {
                    dispatchGalleryIntent()
                }

                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    @SuppressLint("IntentReset")
    private fun dispatchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                    binding.ivFoodImage.setImageBitmap(bitmap)
                }

                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            uri
                        )
                        binding.ivFoodImage.setImageBitmap(bitmap)
                        photoUri = uri
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startBarcodeScanner()
            } else {
                showToast("Camera permission is required to take a photo")
            }
        }
    }

    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CalenderViewCustom,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                selectedExpirationDate = selectedDate.time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                textView.text = dateFormat.format(selectedExpirationDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveDataToFirebase() {
        val foodName = binding.etFoodName.text.toString()
        val foodExpirationDate = selectedExpirationDate
        val foodRemainingAmount = binding.etFoodRemainingAmount.text.toString()
        val currentDate = Date()

        lifecycleScope.launch {
            try {
                (requireActivity() as MainActivity).showLoadingDialog()
                val imageUrl: String = if (photoUri != null) {
                    if (photoUri.toString().startsWith("http")) {
                        photoUri.toString()
                    } else {
                        firebaseStorageService.uploadImage(
                            photoUri!!,
                            "images/${System.currentTimeMillis()}.jpg"
                        )
                    }
                } else {
                    currentPhotoPath
                }

                Log.d(TAG, "Image URL: $imageUrl")

                val food = Food(
                    proName = foodName,
                    img = imageUrl,
                    category = selectedCategory,
                    expirationDate = foodExpirationDate,
                    remainAmount = foodRemainingAmount.toInt(),
                    date = currentDate
                )

                val uid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
                val foodId = foodViewModel.getSelectedFoodId()

                Log.d(TAG, "User ID: $uid, Food ID: $foodId")

                if (uid != null) {
                    if (foodId != null) {
                        foodViewModel.saveUserFood(uid, food.copy(id = foodId))
                    } else {
                        foodViewModel.saveUserFood(uid, food)
                    }
                } else {
                    showToast("User ID not found. Please log in again.")
                    Log.d(TAG, "User ID not found. Please log in again.")
                }
                binding.floatingButton.showCheckAnimation()
                delay(1000) // Allow check animation to play
                binding.floatingButton.resetIcon()
                findNavController().navigate(R.id.action_nav_edit_to_nav_fridge)
                (requireActivity() as MainActivity).hideLoadingDialog()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload image: ${e.message}", e)
            }
        }
    }


    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("상품의 바코드를 스캔해주세요.")
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false);
            captureActivity = CaptureActivity::class.java
        }
        barcodeScannerLauncher.launch(integrator.createScanIntent())
    }

    private val barcodeScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                handleScanFailure()
            } else {
                handleScanSuccess(intentResult.contents)
            }
        }
    }

    private fun handleScanFailure() {
        showToast("Failed to scan barcode")
    }

    private fun handleScanSuccess(scannedData: String) {
        binding.apply {
            etFoodName.setText(scannedData)
            // 바코드를 사용하여 크롤링
            CrawlingTask().execute(scannedData)
        }
    }


    private inner class CrawlingTask : AsyncTask<String, Void, Triple<String, String, Int>>() {

        override fun doInBackground(vararg params: String?): Triple<String, String, Int> {
            val barcode = params[0] ?: ""
            Log.d(TAG, "doInBackground: $barcode")
            val url = BASE_URL + barcode
            var imageUrl = ""
            var title = ""
            var type = ""
            var amount = 0

            try {
                val document = Jsoup.connect(url).get()

                // 이미지 가져오기
                val imageElement = document.selectFirst("div.pv_img img")
                if (imageElement != null) {
                    imageUrl = imageElement.attr("src")
                }

                // 제목 가져오기
                val titleElement = document.selectFirst("div.pv_title h3")
                title = titleElement?.text().toString()

                // 중량 가져오기
                val weightElement = document.selectFirst("table.detail_info")
                val firstData = weightElement?.selectFirst("tr")
                val weightCells = firstData?.select("td")
                if (weightCells != null) {
                    val lastCellData = weightCells.last()?.text()
                    amount = lastCellData?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return Triple(imageUrl, title, amount)
        }

        override fun onPostExecute(result: Triple<String, String, Int>?) {
            super.onPostExecute(result)
            result?.let { (imageUrl, title, amount) ->
                val imagePath = imageUrl.substringAfter("/pr/")
                binding.apply {
                    val fullImageUrl = BASE_URL + imagePath
                    ivFoodImage.load(BASE_URL + imagePath)
                    etFoodName.setText(title)
                    etFoodRemainingAmount.setText(amount.toString())
                    photoUri = Uri.parse(fullImageUrl)
                }
            }
        }
    }

    companion object {
        const val BASE_URL = "https://gs1.koreannet.or.kr/pr/"
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val CH = "\uD83C\uDF6B"
    }


}