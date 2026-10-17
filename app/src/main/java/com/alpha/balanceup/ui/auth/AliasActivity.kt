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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAliasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        binding.btnContinue.setOnClickListener {
            val alias = binding.etAlias.text.toString().trim()
            if (alias.isNotEmpty()) {
                handleGuestSignIn(alias)
            } else {
                binding.tilAlias.error = "Alias cannot be empty"
            }
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
