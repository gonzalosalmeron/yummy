package com.gonzxlodev.yummy.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityLoginBinding
import com.gonzxlodev.yummy.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** INICIALIZA FIREBASE AUTH */
        auth = Firebase.auth

        /** ESTABLECE LOS LISTENERS EN LOS BOTONES */
        binding.regBackBtn.setOnClickListener { finish() }
        binding.regSubmitBtn.setOnClickListener{ register() }
    }

    /** REGISTRA AL USUARIO POR EMAIL Y CONTRASEÑA, COMPROBANDO QUE LA CONTRASEÑA TENGA UN MÍNIMO
     * DE SEIS CARÁCTERES DE LONGITUD, QUE TODOS LOS CAMPOS ESTÉN COMPLETOS Y QUE LA CONTRASEÑA
     * Y EL REPETIR CONTRASEÑA COINCIDAN */
    private fun register() {
        val email = binding.registerEmailInputEd.text.toString()
        val password = binding.registerPasswordInputEd.text.toString()
        val repeatPassword = binding.registerPasswordRepeatInputEd.text.toString()
        if (password.length >= 6) {
            Toast.makeText(this, R.string.password_min_six, Toast.LENGTH_LONG).show()
        } else {
            if (email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                if(password == repeatPassword) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Register succesful", Toast.LENGTH_LONG).show()
                            var intent = Intent(this,LoginActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(this, "Ooops, something failed, try again later", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please, fill all the camps", Toast.LENGTH_LONG).show()
            }
        }

    }

}