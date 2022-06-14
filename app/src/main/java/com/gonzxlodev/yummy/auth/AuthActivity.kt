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

        /** ESTABLECE LOS LISTENERS EN LOS BOTONES */
        binding.regEmailBtn.setOnClickListener{ this.regWithEmail() }
        binding.regLoginBtn.setOnClickListener { this.goLogin() }

        /** VER MÉTODO */
        session()
    }

    /** INICIA LA ACTIVIDAD PARA REGISTRARNOS CON EMAIL */
    private fun regWithEmail() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    /** INICIA LA ACTIVIDAD PARA PODER INICIAR SESIÓN */
    private fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /** INICIA LA MAIN ACTIVITY */
    private fun goMain(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    /** COMPRUEBA SI HAY ALGÚN EMAIL GUARDADO EN LAS SHARED PREFERENCES POR SI YA HAY UNO
     * NOS LLEVA A LA MAIN ACTIVITY YA QUE ESTO INDICARÍA QUE HAY UN USUARIO QUE YA HA INICIADO
     * SESIÓN Y POR LO TANTO NO DEBERÍA DE TENER QUE VOLVER A INICIAR SESIÓN */
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        if (email != null) {
            goMain(email)
        }
    }
}