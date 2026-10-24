package com.alpha.balanceup.ui.dashboard

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityAddExpenseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddGroupActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private val memberAdapter = MemberAdapter { member ->
        viewModel.deleteMember(member)
    }

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

        // Automatically scroll to top when a new member is inserted at position 0
        memberAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.rvMembers.scrollToPosition(0)
                }
            }
        })
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Step 1: Create Group Name
        binding.tilGroupName.setEndIconOnClickListener {
            handleCreateGroupStep()
        }

        binding.etGroupName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleCreateGroupStep()
                true
            } else {
                false
            }
        }

        // Step 2: Add Members
        binding.tilMemberName.setEndIconOnClickListener {
            handleAddMemberAction()
        }

        binding.etMemberName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = binding.etMemberName.text.toString().trim()
                if (name.isEmpty()) {
                    // User hit done on an empty field, assume they are finished adding members
                    handleFinalCreateGroup()
                } else {
                    handleAddMemberAction()
                }
                true
            } else {
                false
            }
        }

        // Finish Group Creation Flow
        binding.btnCreateGroup.setOnClickListener {
            handleFinalCreateGroup()
        }
    }

    private fun handleCreateGroupStep() {
        val groupName = binding.etGroupName.text.toString().trim()
        val includeSelf = binding.switchIncludeSelf.isChecked
        if (groupName.isNotBlank()) {
            viewModel.createGroup(groupName, includeSelf)
        } else {
            binding.tilGroupName.error = "Enter group name"
        }
    }

    private fun handleAddMemberAction() {
        val name = binding.etMemberName.text.toString().trim()
        if (name.isNotBlank()) {
            viewModel.addMember(name)
            binding.etMemberName.text?.clear()
        } else {
            binding.tilMemberName.error = "Enter name"
        }
    }

    private fun handleFinalCreateGroup() {
        if (memberAdapter.itemCount < 1) {
            Toast.makeText(this, "Add at least 1 person", Toast.LENGTH_SHORT).show()
            binding.etMemberName.requestFocus()
            return
        }
        Toast.makeText(this, "Group Created successfully!", Toast.LENGTH_SHORT).show()
        finish()
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
                            binding.switchIncludeSelf.visibility = View.GONE

                            // Auto focus the next field
                            binding.etMemberName.requestFocus()
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
