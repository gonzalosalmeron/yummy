package com.gonzxlodev.yummy.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityAuthBinding
import com.gonzxlodev.yummy.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val GOOGLE_SIGN_IN = 100
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        /** ESTABLECE LOS LISTENERS EN LOS BOTONES */
        binding.regEmailBtn.setOnClickListener{ this.regWithEmail() }
        binding.regLoginBtn.setOnClickListener { this.goLogin() }
        binding.regGoogleBtn.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_2))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
        binding.regFacebookBtn.setOnClickListener {
            Snackbar.make(binding.root, R.string.under_development, Snackbar.LENGTH_LONG).show()
        }

        /** VER MÉTODO */
        session()

        /** INICIALIZA FIREBASE AUTH */
        auth = Firebase.auth
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /** SI HEMOS MARCADO INICIAR SESIÓN CON GOOGLE, SE INICIA EL PROCESO DE SELECCIÓN
         * DE CUENTA DE GOOGLE Y POSTERIORMENTE INICIA SESIÓN */
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            checkAndSaveUserToFireStore(account.displayName!!, account.email!!, account.photoUrl!!)
                        } else {
                            Toast.makeText(this, R.string.ups_something_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, R.string.ups_something_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** COMPRUEBA QUE EL USUARIO LOGUEADO NO TENGA UNA CUENTA YA CREADA DENTRO DE LA COLECIÓN
     * DE USUARIO EN FIREBASE. SI EL USUARIO NO EXISTIESE EN LA COLECIÓN LO CREARÍA, ALMACENANDO
     * SU NOMBRE, SU CORREO Y SU IMAGEN DE PERFIL DE GOOGLE */
    private fun checkAndSaveUserToFireStore(name:String?, email:String ,imgUrl: Uri?) {
        db.collection("users").document(email).get()
            .addOnSuccessListener {
                var dbEmail = it.get("email") as String?
                if (dbEmail == null){
                    db.collection("users").document(email).set(
                        hashMapOf(
                            "name" to name,
                            "email" to email,
                            "imgUrl" to imgUrl,
                        )
                    ).addOnSuccessListener { taskSnapshot ->
                        saveUserInLocale(email, name, imgUrl)
                        session()
                    }
                } else {
                    var img: String = it.get("imgUrl") as String
                    saveUserInLocale(email, it.get("name") as String, Uri.parse(img))
                    session()
                }
            }
    }

    /** ALMACENA LOS DATOS DEL USUARIO QUE HA INICIADO SESIÓN EN LAS SHARED PREFERENCES */
    private fun saveUserInLocale(email: String, name: String?, imgUrl: Uri?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("name", name)
        prefs.putString("imgUrl", imgUrl.toString())
        prefs.apply()
    }
}