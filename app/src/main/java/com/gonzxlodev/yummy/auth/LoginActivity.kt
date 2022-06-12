package com.gonzxlodev.yummy.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityLoginBinding
import com.gonzxlodev.yummy.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN = 100
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lgnBackBtn.setOnClickListener { finish() }
        binding.loginSubmitButton.setOnClickListener{ login() }
        binding.lgnGoogleBtn.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_2))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        auth = Firebase.auth

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            saveUserToFireStore(account.displayName!!, account.email!!, account.photoUrl!!)
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

    private fun login() {
        var email = binding.loginEmailInputEd.text.toString()
        var password = binding.loginPasswordInputEd.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if(it.isSuccessful) {
//                    saveUserInLocale(email)
                    goMain()
                } else {
                    Toast.makeText(this, R.string.ups_something_failed, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, R.string.fill_the_camps, Toast.LENGTH_LONG).show()
        }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun saveUserToFireStore(name:String, email:String ,imgUrl: Uri) {
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
                        goMain()
                    }
                } else {
                    saveUserInLocale(email, name, imgUrl)
                    goMain()
                }
            }
    }

    private fun saveUserInLocale(email: String, name: String?, imgUrl: Uri?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("name", name)
        prefs.putString("imgUrl", imgUrl.toString())
        Log.i("imgUril", "${imgUrl}, ${name}")
        prefs.apply()
    }
}