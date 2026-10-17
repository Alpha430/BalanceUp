package com.alpha.balanceup.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivitySummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SummaryActivity : BaseActivity() {

    private lateinit var binding: ActivitySummaryBinding
    private val viewModel: ExpenseDetailsViewModel by viewModels()
    private val balanceAdapter = MemberBalanceAdapter()
    private val settlementAdapter = SettlementAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        if (groupId == -1L) {
            finish()
            return
        }

        viewModel.setGroupId(groupId)
        setupToolbar()
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
        
        // Mark as calculated initially when entering summary
        viewModel.markAsCalculated()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        binding.rvBalances.apply {
            layoutManager = LinearLayoutManager(this@SummaryActivity)
            adapter = balanceAdapter
        }

        binding.rvSettlements.apply {
            layoutManager = LinearLayoutManager(this@SummaryActivity)
            adapter = settlementAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseItemActivity::class.java).apply {
                putExtra("GROUP_ID", viewModel.groupId.value)
            }
            startActivity(intent)
            // We finish this activity so that when the user comes back from adding an expense,
            // they don't land on an outdated summary. The Dashboard or redirect will 
            // handle taking them to the Expense List.
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.expenseItems, viewModel.groupMembers) { expenses, members ->
                    viewModel.getSettlementSummary(members, expenses)
                }.collect { summary ->
                    updateUI(summary)
                }
            }
        }
    }

    private fun updateUI(summary: ExpenseDetailsViewModel.SettlementSummary) {
        binding.tvTotalSpending.text = "₹ ${String.format("%.2f", summary.totalGroupExpense)}"
        binding.tvIndividualShare.text = "₹ ${String.format("%.2f", summary.sharePerPerson)}"
        
        balanceAdapter.submitList(summary.memberBalances)
        settlementAdapter.submitList(summary.settlements)
    }
}
