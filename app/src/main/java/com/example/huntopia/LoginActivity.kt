package com.example.huntopia

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<MaterialButton>(R.id.btnStart).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
