package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseItemBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddExpenseItemActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseItemBinding
    private val viewModel: AddExpenseItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        if (groupId == -1L) {
            finish()
            return
        }

        viewModel.setGroupId(groupId)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSaveExpense.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val qty = binding.etQuantity.text.toString().trim()
            val price = binding.etPrice.text.toString().trim()
            val paidBy = binding.etPaidBy.text.toString().trim()

            if (name.isEmpty() || price.isEmpty() || paidBy.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveExpense(name, qty, price, paidBy)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupMembers.collect { members ->
                        val names = members.map { it.name }
                        // Using a custom filterless layout or setting filtering behavior manually can work, 
                        // but setting the adapter text to empty string before setting data or disabling auto-filtering ensures all names show.
                        val adapter = object : ArrayAdapter<String>(
                            this@AddExpenseItemActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                        ) {
                            override fun getFilter(): android.widget.Filter {
                                return object : android.widget.Filter() {
                                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                                        val results = FilterResults()
                                        results.values = names
                                        results.count = names.size
                                        return results
                                    }
                                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                                        notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                        binding.etPaidBy.setAdapter(adapter)
                    }
                }
                launch {
                    viewModel.saveSuccess.collect { success ->
                        if (success) {
                            Toast.makeText(this@AddExpenseItemActivity, "Expense added", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}
