package com.example.huntopia

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val userRepository = UserRepository()

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var btnStart: MaterialButton
    private lateinit var createAccountLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        btnStart = findViewById(R.id.btnStart)
        createAccountLink = findViewById(R.id.createAccountLink)

        btnStart.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error = getString(R.string.error_email_required)
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = getString(R.string.error_invalid_email)
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = getString(R.string.error_password_required)
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        createAccountLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val existingUser = auth.currentUser
        if (existingUser != null) {
            provisionProfileAndNavigate(existingUser, showSuccessToast = false)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user ?: auth.currentUser
                    if (firebaseUser == null) {
                        Toast.makeText(this, getString(R.string.error_login_generic), Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }
                    provisionProfileAndNavigate(firebaseUser, showSuccessToast = true)
                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> getString(R.string.error_wrong_password)
                        is FirebaseAuthInvalidUserException -> getString(R.string.error_user_not_found)
                        else -> getString(R.string.error_login_generic)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun provisionProfileAndNavigate(user: FirebaseUser, showSuccessToast: Boolean) {
        lifecycleScope.launch {
            val profileSynced = runCatching {
                userRepository.getOrProvisionProfile(user.uid, user.email.orEmpty())
            }.isSuccess

            if (!profileSynced) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.warning_profile_sync_failed),
                    Toast.LENGTH_LONG
                ).show()
            }

            if (showSuccessToast) {
                Toast.makeText(this@LoginActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
            }

            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
