package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddGroupActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private val memberAdapter = MemberAdapter()

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
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(this@AddGroupActivity)
            adapter = memberAdapter
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Step 1: Create Group Name
        binding.tilGroupName.setEndIconOnClickListener {
            val groupName = binding.etGroupName.text.toString().trim()
            if (groupName.isNotBlank()) {
                viewModel.createGroup(groupName)
            } else {
                binding.tilGroupName.error = "Enter group name"
            }
        }

        // Step 2: Add Members
        binding.tilMemberName.setEndIconOnClickListener {
            val name = binding.etMemberName.text.toString().trim()
            if (name.isNotBlank()) {
                viewModel.addMember(name)
                binding.etMemberName.text?.clear()
            } else {
                binding.tilMemberName.error = "Enter name"
            }
        }

        // Finish Group Creation Flow
        binding.btnCreateGroup.setOnClickListener {
            if (memberAdapter.itemCount < 2) {
                Toast.makeText(this, "Add at least 2 people", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Group Created successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.groupId.collect { id ->
                        if (id != null) {
                            binding.layoutAddMembers.visibility = View.VISIBLE
                            binding.btnCreateGroup.visibility = View.VISIBLE
                            binding.tilGroupName.isEnabled = false
                            binding.tilGroupName.setEndIconDrawable(null)
                        }
                    }
                }

                launch {
                    viewModel.groupMembers.collect { members ->
                        memberAdapter.submitList(members)
                    }
                }
            }
        }
    }
}
