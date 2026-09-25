package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseBinding
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddExpenseActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private val adapter = ExpenseAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(this@AddExpenseActivity)
            adapter = this@AddExpenseActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tilGroupName.setEndIconOnClickListener {
            val groupName = binding.etGroupName.text.toString()
            if (groupName.isNotBlank()) {
                viewModel.createGroup(groupName)
            } else {
                binding.tilGroupName.error = "Enter group name"
            }
        }

        binding.btnAddProduct.setOnClickListener {
            val productName = binding.etProductName.text.toString()
            val quantity = binding.etQuantity.text.toString()
            val price = binding.etPrice.text.toString()
            val paidBy = binding.etPaidBy.text.toString()

            if (productName.isBlank() || price.isBlank() || paidBy.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addExpenseItem(productName, quantity, price, paidBy)
            clearItemFields()
        }

        binding.btnBalanceUp.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this, R.anim.btn_click)
            it.startAnimation(anim)
            handleBalanceUp()
        }
    }

    private fun clearItemFields() {
        binding.etProductName.text?.clear()
        binding.etQuantity.text?.clear()
        binding.etPrice.text?.clear()
        binding.etPaidBy.text?.clear()
        binding.etProductName.requestFocus()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupId.collect { id ->
                        if (id != null) {
                            binding.layoutExpenseContent.visibility = View.VISIBLE
                            binding.layoutBottom.visibility = View.VISIBLE
                            binding.tilGroupName.isEnabled = false
                        }
                    }
                }

                launch {
                    viewModel.expenseItems.collect { items ->
                        adapter.submitList(items)
                    }
                }

                launch {
                    viewModel.totalExpense.collect { total ->
                        binding.tvTotalValue.text = "₹ ${String.format("%.2f", total)}"
                    }
                }
            }
        }
    }

    private fun handleBalanceUp() {
        Toast.makeText(this, "Calculating Balances...", Toast.LENGTH_SHORT).show()
        // Logic for final settlements can be added here
    }
}
