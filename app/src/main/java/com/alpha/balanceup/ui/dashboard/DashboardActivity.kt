package com.alpha.balanceup.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var groupAdapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)
        
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter { group ->
            val intent = Intent(this, GroupDetailsActivity::class.java).apply {
                putExtra("GROUP_ID", group.id)
                putExtra("GROUP_NAME", group.name)
            }
            startActivity(intent)
        }
        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = groupAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddExpense.setOnClickListener {
            // This button now specifically creates a new group
            val intent = Intent(this, AddGroupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.allGroups.collect { groups ->
                if (groups.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvGroups.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvGroups.visibility = View.VISIBLE
                    groupAdapter.submitList(groups)
                }
            }
        }
    }
}
