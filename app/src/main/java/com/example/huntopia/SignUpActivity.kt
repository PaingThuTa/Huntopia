package com.example.huntopia

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var createAccountButton: MaterialButton
    private lateinit var backToLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        createAccountButton = findViewById(R.id.btnCreateAccount)
        backToLoginLink = findViewById(R.id.backToLoginLink)

        createAccountButton.setOnClickListener {
            attemptSignUp()
        }

        backToLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun attemptSignUp() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (email.isEmpty()) {
            emailInput.error = getString(R.string.error_email_required)
            emailInput.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = getString(R.string.error_invalid_email)
            emailInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = getString(R.string.error_password_required)
            passwordInput.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordInput.error = getString(R.string.error_password_too_short)
            passwordInput.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = getString(R.string.error_confirm_password_required)
            confirmPasswordInput.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = getString(R.string.error_passwords_do_not_match)
            confirmPasswordInput.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> getString(R.string.error_password_too_short)
                        is FirebaseAuthUserCollisionException -> getString(R.string.error_email_already_in_use)
                        else -> getString(R.string.error_signup_generic)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}
