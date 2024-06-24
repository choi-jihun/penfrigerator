package com.example.fridge.src

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.MainActivity
import com.example.fridge.R
import com.example.fridge.config.BaseFragment
import com.example.fridge.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment :
    BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            auth = FirebaseAuth.getInstance()

            val id = binding.etIdLogin.text.toString()
            val pw = binding.etPwLogin.text.toString()

            if (id.isNotEmpty() && pw.isNotEmpty()) {
                loginUser(id, pw)
            } else {
                showToast("로그인에 실패했습니다.")
            }
        }

        binding.registButton.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.frameLayout, JoinFragment())
                .commit()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("로그인에 성공했습니다")
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserInfo(email, user.uid)  // Firebase에서 UID 가져오기
                        navigateToMainActivity()
                    } else {
                        showToast("로그인 실패: 사용자 정보를 가져올 수 없습니다.")
                    }
                } else {
                    showToast("로그인 실패: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserInfo(email: String, userId: String) {
        ApplicationClass.sharedPreferences.setString(Constants.USER_EMAIL, email)
        ApplicationClass.sharedPreferences.setString(Constants.USER_UID, userId)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
