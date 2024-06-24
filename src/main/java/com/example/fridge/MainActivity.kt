package com.example.fridge

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.fridge.config.BaseActivity
import com.example.fridge.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private lateinit var navController: NavController
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initLoadingDialog()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNav, navController)
    }

    private fun initLoadingDialog() {
        loadingDialog = Dialog(this).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            setContentView(view)
            setCancelable(false)

            // Set dialog attributes to fill the screen
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            // ProgressBar 색상 설정 (필요시)
            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            progressBar.indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(context, R.color.main_blue_1),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun showLoadingDialog() {
        if (!loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }

    fun hideLoadingDialog() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

}