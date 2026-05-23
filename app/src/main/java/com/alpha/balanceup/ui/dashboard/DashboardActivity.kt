package com.alpha.balanceup.ui.dashboard

import android.content.Intent
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
        
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false // Set to false for white icons on colored background

        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }
}
