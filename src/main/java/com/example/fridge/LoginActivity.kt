package com.example.fridge

import android.content.Intent
import android.os.Bundle
import com.example.fridge.config.ApplicationClass
import com.example.fridge.config.BaseActivity
import com.example.fridge.databinding.ActivityLoginBinding
import com.example.fridge.src.LoginFragment
import com.example.fridge.util.Constants

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        if (userId.isNullOrEmpty()) {
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, LoginFragment())
                .commit()
        } else {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
