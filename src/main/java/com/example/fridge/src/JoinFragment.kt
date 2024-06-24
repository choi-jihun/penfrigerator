package com.example.fridge.src

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants.USERS_COLLECTION
import com.example.fridge.util.Constants.USER_EMAIL
import com.example.fridge.util.Constants.USER_UID
import com.example.fridge.MainActivity
import com.example.fridge.R
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "JoinFragment"

class JoinFragment :
    BaseFragment<FragmentJoinBinding>(FragmentJoinBinding::bind, R.layout.fragment_join) {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference(USERS_COLLECTION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joinButton.setOnClickListener {
            val id = binding.etIdLogin.text.toString()
            val pw = binding.etPwLogin.text.toString()
            val checkPW = binding.etCheckPwLogin.text.toString()

            if (id.isNotEmpty() && pw.isNotEmpty()) {
                if (pw == checkPW) {
                    registerUser(id, pw)
                } else {
                    showToast("비밀번호가 일치하지 않습니다.")
                }
            } else {
                showToast("ID 혹은 PW를 입력해주세요")
            }
        }

        binding.visiblePW.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.invisiblePW.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun registerUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "registerUser: herehere")
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d(TAG, "registerUser: $result")
                val user = result.user
                user?.let {
                    val userId = it.uid
                    val userMap = mapOf(
                        "email" to email,
                        "uid" to userId
                    )

                    database.child(userId).setValue(userMap).await()
                    saveUserInfo(email, userId)

                    withContext(Dispatchers.Main) {
                        showToast("회원가입 성공!")
                        Log.d(TAG, "User registration successful: $email")
                        navigateToMainActivity()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("회원가입 실패: ${e.message}")
                    Log.e(TAG, "User registration failed: ${e.message}")
                }
            }
        }
    }

    private fun saveUserInfo(email: String, userId: String) {
        ApplicationClass.sharedPreferences.setString(USER_EMAIL, email)
        ApplicationClass.sharedPreferences.setString(USER_UID, userId)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        updatePasswordVisibility()
    }

    private fun updatePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPwLogin.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.etCheckPwLogin.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.visiblePW.isVisible = false
            binding.invisiblePW.isVisible = true
        } else {
            binding.etPwLogin.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.etCheckPwLogin.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.visiblePW.isVisible = true
            binding.invisiblePW.isVisible = false
        }
    }
}
