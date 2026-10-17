package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.databinding.ActivityAddExpenseItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddExpenseItemActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseItemBinding
    private val viewModel: AddExpenseItemViewModel by viewModels()
    private val manualShareAdapter = ManualShareAdapter()
    private var groupMembersList: List<GroupMemberEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        val expenseId = intent.getLongExtra("EXPENSE_ID", -1L)

        if (expenseId != -1L) {
            viewModel.setExpenseId(expenseId)
            binding.toolbar.title = "Update Expense"
            binding.btnSaveExpense.text = "Update Expense"
        } else if (groupId != -1L) {
            viewModel.setGroupId(groupId)
            binding.toolbar.title = "Add Expense"
            binding.btnSaveExpense.text = "Save Expense"
        } else {
            finish()
            return
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvManualShares.apply {
            layoutManager = LinearLayoutManager(this@AddExpenseItemActivity)
            adapter = manualShareAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.rbSplitEqually.isChecked) {
                    calculateEqualSplit()
                }
            }
        })

        binding.rgSplitType.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutManualSplit.visibility = View.VISIBLE
            if (checkedId == binding.rbSplitEqually.id) {
                manualShareAdapter.setEditable(false)
                calculateEqualSplit()
            } else {
                manualShareAdapter.setEditable(true)
            }
        }

        binding.btnSaveExpense.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val priceStr = binding.etPrice.text.toString().trim()
            val displayedPaidBy = binding.etPaidBy.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty() || displayedPaidBy.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Map the displayed alias back to the full unique name for calculation
            val fullPaidBy = groupMembersList.find { 
                (if (it.name.contains("_")) it.name.substringBeforeLast("_") else it.name) == displayedPaidBy 
            }?.name ?: displayedPaidBy

            val totalPrice = priceStr.toDoubleOrNull() ?: 0.0

            if (binding.rbSplitManually.isChecked) {
                val shares = manualShareAdapter.getManualShares()
                val totalManualPrice = shares.values.sum()
                
                if (Math.abs(totalManualPrice - totalPrice) > 0.01) {
                    val formattedManual = String.format("%.2f", totalManualPrice)
                    val formattedTotal = String.format("%.2f", totalPrice)
                    
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Amount Mismatch")
                        .setMessage("The sum of individual shares (₹$formattedManual) does not match the total amount (₹$formattedTotal). Please correct the shares before saving.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setOnClickListener
                }
            }

            viewModel.saveExpense(name, priceStr, fullPaidBy)
        }
    }

    private fun calculateEqualSplit() {
        val priceStr = binding.etPrice.text.toString().trim()
        val totalPrice = priceStr.toDoubleOrNull() ?: 0.0
        if (groupMembersList.isNotEmpty()) {
            val equalShare = totalPrice / groupMembersList.size
            val sharesMap = groupMembersList.associate { it.id to equalShare }
            manualShareAdapter.setShares(sharesMap)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupMembers.collect { members ->
                        groupMembersList = members
                        // Display only the alias part in the dropdown
                        val displayNames = members.map { 
                            if (it.name.contains("_")) it.name.substringBeforeLast("_") else it.name 
                        }.toTypedArray()
                        
                        binding.etPaidBy.setSimpleItems(displayNames)
                        manualShareAdapter.submitList(members)
                        
                        binding.layoutManualSplit.visibility = View.VISIBLE
                        manualShareAdapter.setEditable(!binding.rbSplitEqually.isChecked)
                        if (binding.rbSplitEqually.isChecked) {
                            calculateEqualSplit()
                        }
                    }
                }
                launch {
                    viewModel.existingExpense.collect { expense ->
                        expense?.let {
                            binding.etProductName.setText(it.productName)
                            binding.etPrice.setText(it.price.toString())
                            // Convert full name to alias for display when editing
                            val displayAlias = if (it.paidBy.contains("_")) it.paidBy.substringBeforeLast("_") else it.paidBy
                            binding.etPaidBy.setText(displayAlias, false)
                        }
                    }
                }
                launch {
                    viewModel.saveSuccess.collect { success ->
                        if (success) {
                            val msg = if (intent.hasExtra("EXPENSE_ID")) "Expense updated" else "Expense saved"
                            Toast.makeText(this@AddExpenseItemActivity, msg, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}
