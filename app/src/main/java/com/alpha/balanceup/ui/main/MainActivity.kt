package com.alpha.balanceup.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.data.local.database.AppDatabase
import com.alpha.balanceup.databinding.ActivityMainBinding
import com.alpha.balanceup.ui.auth.SignInActivity
import com.alpha.balanceup.ui.dashboard.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen on-screen until we decide where to navigate
        splashScreen.setKeepOnScreenCondition { true }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        lifecycleScope.launch {
            val localUser = AppDatabase.getDatabase(this@MainActivity).userDao().getUser()
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            val nextScreen = if (localUser != null || firebaseUser != null) {
                DashboardActivity::class.java
            } else {
                SignInActivity::class.java
            }

            startActivity(Intent(this@MainActivity, nextScreen))
            finish()
        }
    }
}