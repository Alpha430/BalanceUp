package com.alpha.balanceup.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.databinding.ActivityGroupDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseListActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private val viewModel: ExpenseDetailsViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private var isSelectionMode = false
    private var groupName: String = ""

    private var currentMembers: List<GroupMemberEntity> = emptyList()
    private var currentExpenses: List<ExpenseItemEntity> = emptyList()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitSelectionMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        groupName = intent.getStringExtra("GROUP_NAME") ?: "Group Details"

        if (groupId == -1L) {
            finish()
            return
        }

        viewModel.setGroupId(groupId)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        setupToolbar(groupName)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar(name: String) {
        binding.toolbar.title = name
        binding.toolbar.setNavigationOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        binding.toolbar.inflateMenu(R.menu.group_menu)
        updateToolbarMenu()
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    if (isSelectionMode) {
                        showDeleteExpensesConfirmation()
                    } else {
                        showDeleteGroupConfirmation()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateToolbarMenu() {
        val deleteItem = binding.toolbar.menu.findItem(R.id.action_delete)
        if (isSelectionMode) {
            deleteItem?.title = "Delete Selected"
            binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        } else {
            deleteItem?.title = "Delete Group"
            binding.toolbar.setNavigationIcon(null) 
        }
    }

    private fun showDeleteGroupConfirmation() {
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

    private fun showDeleteExpensesConfirmation() {
        val selectedIds = expenseAdapter.getSelectedIds()
        val selectedCount = selectedIds.size
        if (selectedCount == 0) return

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Expenses")
            .setMessage("Are you sure you want to delete $selectedCount selected expense(s)?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteExpenses(selectedIds)
                exitSelectionMode()
            }
            .show()
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        backPressedCallback.isEnabled = true
        expenseAdapter.setSelectionMode(true)
        binding.fabAddExpense.hide()
        updateToolbarMenu()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        backPressedCallback.isEnabled = false
        expenseAdapter.setSelectionMode(false)
        binding.toolbar.title = groupName
        binding.fabAddExpense.show()
        updateToolbarMenu()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onExpenseClick = { expense ->
                val intent = Intent(this, AddExpenseItemActivity::class.java).apply {
                    putExtra("EXPENSE_ID", expense.id)
                }
                startActivity(intent)
            },
            onExpenseLongClick = { _ ->
                enterSelectionMode()
            },
            onSelectionChange = { count ->
                if (isSelectionMode) {
                    binding.toolbar.title = if (count > 0) "$count Selected" else "Select Items"
                    if (count == 0) {
                        exitSelectionMode()
                    }
                }
            }
        )
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(this@ExpenseListActivity)
            adapter = expenseAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseItemActivity::class.java).apply {
                putExtra("GROUP_ID", viewModel.groupId.value ?: -1L)
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
                        currentExpenses = items
                        expenseAdapter.submitList(items)
                    }
                }
                launch {
                    viewModel.groupMembers.collect { members ->
                        currentMembers = members
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
        if (currentMembers.isEmpty()) {
            Toast.makeText(this, "Add members to the group first", Toast.LENGTH_SHORT).show()
            return
        }

        val groupId = viewModel.groupId.value ?: return
        val intent = Intent(this, SummaryActivity::class.java).apply {
            putExtra("GROUP_ID", groupId)
        }
        startActivity(intent)
    }
}
