package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
