package com.alpha.balanceup.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityGroupDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private val viewModel: GroupDetailsViewModel by viewModels()
    private val expenseAdapter = ExpenseAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Group Details"

        if (groupId == -1L) {
            finish()
            return
        }

        viewModel.setGroupId(groupId)
        setupToolbar(groupName)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar(name: String) {
        binding.toolbar.title = name
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        binding.toolbar.inflateMenu(R.menu.group_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete this group? All associated members and expenses will be permanently removed.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGroup()
                finish()
            }
            .show()
    }

    private fun setupRecyclerView() {
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailsActivity)
            adapter = expenseAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseItemActivity::class.java).apply {
                putExtra("GROUP_ID", viewModel.groupId.value)
            }
            startActivity(intent)
        }

        binding.btnBalanceUp.setOnClickListener {
            handleBalanceUp()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.expenseItems.collect { items ->
                        expenseAdapter.submitList(items)
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
        Toast.makeText(this, "Calculating Settlements...", Toast.LENGTH_SHORT).show()
    }
}
