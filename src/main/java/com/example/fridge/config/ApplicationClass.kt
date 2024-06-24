package com.example.fridge.config

import android.app.Application
import com.example.fridge.util.SharedPreferencesUtil
import com.google.firebase.FirebaseApp

class ApplicationClass: Application() {

    companion object {
        lateinit var sharedPreferences: SharedPreferencesUtil

        const val SHARED_PREFERENCES_NAME = "SSAFY_TEMPLATE_APP"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        sharedPreferences = SharedPreferencesUtil(applicationContext)

    }

}