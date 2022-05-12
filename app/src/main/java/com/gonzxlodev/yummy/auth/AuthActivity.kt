package com.gonzxlodev.yummy.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityAuthBinding
import com.gonzxlodev.yummy.main.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SETTING BUTTONS ACTIONS
        binding.regEmailBtn.setOnClickListener{ this.regWithEmail() }
        binding.regLoginBtn.setOnClickListener { this.goLogin() }

        session()
    }

    // FUNCTIONS
    private fun regWithEmail() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun goMain(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        if (email != null) {
            goMain(email)
        }
    }
}