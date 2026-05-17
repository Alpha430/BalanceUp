package com.alpha.balanceup.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowInsetsControllerCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.alpha.balanceup.R
import com.alpha.balanceup.core.base.BaseActivity
import com.alpha.balanceup.data.local.database.AppDatabase
import com.alpha.balanceup.data.local.entity.UserEntity
import com.alpha.balanceup.databinding.ActivitySignInBinding
import com.alpha.balanceup.ui.dashboard.DashboardActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.util.UUID

class SignInActivity : BaseActivity() {

    companion object {
        private const val TAG = "SignInActivity"
    }

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true


        auth = FirebaseAuth.getInstance()

        credentialManager = CredentialManager.create(baseContext)

        binding.btnGoogleSignIn.setOnClickListener {
            launchCredentialManager()
        }

        binding.tvSkip.setOnClickListener {
            handleGuestSignIn()
        }

    }

    private fun handleGuestSignIn() {
        lifecycleScope.launch {
            val uniqueId = UUID.randomUUID().toString()
            val guestUser = UserEntity(
                id = uniqueId,
                name = "Guest_${uniqueId.take(8)}",
                email = null,
                googleId = null,
                isGuest = true,
                synced = false
            )
            
            val db = AppDatabase.getDatabase(this@SignInActivity)
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


    private fun launchCredentialManager() {

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                    context = this@SignInActivity,
                    request = request
                )

                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun handleSignIn(credential: Credential) {

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }


    // [END sign_out]

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in
            Log.d(TAG, "User signed in: ${user.displayName}, ${user.email}")
            
            // Optionally store the Firebase user in Room here as well if needed
            lifecycleScope.launch {
                val userEntity = UserEntity(
                    id = user.uid,
                    name = user.displayName ?: "User",
                    email = user.email,
                    googleId = user.uid,
                    isGuest = false,
                    synced = true
                )
                AppDatabase.getDatabase(this@SignInActivity).userDao().insert(userEntity)
                navigateToDashboard()
            }
        }
    }

}
