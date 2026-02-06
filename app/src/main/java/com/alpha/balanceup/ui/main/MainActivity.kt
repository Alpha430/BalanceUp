package com.alpha.balanceup.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.databinding.ActivityMainBinding
import com.alpha.balanceup.ui.auth.SignInActivity
import com.alpha.balanceup.ui.dashboard.DashboardActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        val nextScreen = if (isUserSignedIn()) {
            DashboardActivity::class.java
        } else {
            SignInActivity::class.java
        }

        startActivity(Intent(this, nextScreen))
        finish()
    }

    private fun isUserSignedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}