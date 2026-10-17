package com.alpha.balanceup.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.data.local.database.AppDatabase
import com.alpha.balanceup.data.local.entity.UserEntity
import com.alpha.balanceup.databinding.ActivityAliasBinding
import com.alpha.balanceup.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch
import java.util.UUID

class AliasActivity : BaseActivity() {

    private lateinit var binding: ActivityAliasBinding
    private var existingUser: UserEntity? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAliasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        checkExistingUser()

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            val alias = binding.etAlias.text.toString().trim()
            if (alias.isNotEmpty()) {
                if (isEditMode) {
                    updateAlias(alias)
                } else {
                    handleGuestSignIn(alias)
                }
            } else {
                binding.tilAlias.error = "Alias cannot be empty"
            }
        }
    }

    private fun checkExistingUser() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AliasActivity)
            existingUser = db.userDao().getUser()
            existingUser?.let { user ->
                isEditMode = true
                binding.tvAliasTitle.text = "Update Your Alias"
                binding.btnContinue.text = "Update"
                val alias =
                    if (user.name.contains("_")) user.name.substringBeforeLast("_") else user.name
                binding.etAlias.setText(alias)
            }
        }
    }

    private fun updateAlias(newAlias: String) {
        lifecycleScope.launch {
            val user = existingUser ?: return@launch
            val uniqueId =
                if (user.name.contains("_")) user.name.substringAfterLast("_") else UUID.randomUUID()
                    .toString().take(8)
            val newProfileName = "${newAlias}_$uniqueId"
            val oldProfileName = user.name

            val updatedUser = user.copy(name = newProfileName)
            val db = AppDatabase.getDatabase(this@AliasActivity)

            // 1. Update user table
            db.userDao().update(updatedUser)

            // 2. Update all occurrences in groups and expenses to maintain stats consistency
            db.expenseDao().updateMemberName(oldProfileName, newProfileName)
            db.expenseDao().updateExpensePaidBy(oldProfileName, newProfileName)

            Toast.makeText(this@AliasActivity, "Profile updated successfully", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    private fun handleGuestSignIn(alias: String) {
        lifecycleScope.launch {
            val uniqueId = UUID.randomUUID().toString().take(8)
            val profileName = "${alias}_$uniqueId"
            val userId = UUID.randomUUID().toString()
            
            val guestUser = UserEntity(
                id = userId,
                name = profileName,
                email = null,
                googleId = null,
                isGuest = true,
                synced = false
            )
            
            val db = AppDatabase.getDatabase(this@AliasActivity)
            db.userDao().insert(guestUser)
            
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
