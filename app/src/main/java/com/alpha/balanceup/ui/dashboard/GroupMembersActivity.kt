package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityGroupMembersBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupMembersActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupMembersBinding
    private val viewModel: GroupMembersViewModel by viewModels()
    private val adapter = MemberAdapter { member ->
        viewModel.deleteMember(member)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val groupId = intent.getLongExtra("GROUP_ID", -1L)
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Members"

        if (groupId == -1L) {
            finish()
            return
        }

        viewModel.setGroupId(groupId)
        setupToolbar(groupName)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar(name: String) {
        binding.toolbar.title = "$name - Members"
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
            .setMessage("Are you sure you want to delete this group? All associated expenses will be permanently removed.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGroup()
                finish() // Go back to Dashboard
            }
            .show()
    }

    private fun setupRecyclerView() {
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(this@GroupMembersActivity)
            adapter = this@GroupMembersActivity.adapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.members.collect { members ->
                    adapter.submitList(members)
                }
            }
        }
    }
}
