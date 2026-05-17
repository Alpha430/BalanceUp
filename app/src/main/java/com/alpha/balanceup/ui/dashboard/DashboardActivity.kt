package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityDashboardBinding

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)
        
        // Ensure status bar icons are dark if the top is light, or light if the top is dark.
        // Since we have a gradient at the top, let's keep it consistent.
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false // Set to false for white icons on colored background
    }
}
