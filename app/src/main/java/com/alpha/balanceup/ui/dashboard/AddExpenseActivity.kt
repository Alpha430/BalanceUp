package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBalanceUp.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this, R.anim.btn_click)
            it.startAnimation(anim)
            
            // Logic to handle Balance Up can be added here
            handleBalanceUp()
        }
    }

    private fun handleBalanceUp() {
        // TODO: Save data and navigate or show result
    }
}
