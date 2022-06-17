package com.gonzxlodev.yummy.auth

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityLoginBinding
import com.gonzxlodev.yummy.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** INICIALIZA FIREBASE AUTH */
        auth = Firebase.auth

        /** ESTABLECE LOS LISTENERS EN LOS BOTONES */
        binding.regBackBtn.setOnClickListener { finish() }
        binding.regSubmitBtn.setOnClickListener{ register() }
        binding.registerImage.setOnClickListener { startImagePicker() }
    }

    /** REGISTRA AL USUARIO POR EMAIL Y CONTRASEÑA, COMPROBANDO QUE LA CONTRASEÑA TENGA UN MÍNIMO
     * DE SEIS CARÁCTERES DE LONGITUD, QUE TODOS LOS CAMPOS ESTÉN COMPLETOS Y QUE LA CONTRASEÑA
     * Y EL REPETIR CONTRASEÑA COINCIDAN */
    private fun register() {
        val name = binding.registerNameInputEd.text.toString().trim()
        val email = binding.registerEmailInputEd.text.toString().trim()
        val password = binding.registerPasswordInputEd.text.toString().trim()
        val repeatPassword = binding.registerPasswordRepeatInputEd.text.toString().trim()

        if (password.length < 6) {
            Toast.makeText(this, R.string.password_min_six, Toast.LENGTH_LONG).show()
        } else {
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                if(password == repeatPassword) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Register succesful", Toast.LENGTH_LONG).show()
                            if (selectedPhotoUri != null){
                                uploadImageToFirebaseStorage(name, email)
                            } else {
                                saveUserToFireStore(name, email, null)
                            }

                        } else {
                            Log.i("holatest", "${it.exception}")
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

    /** COMPRUEBA QUE EL USUARIO LOGUEADO NO TENGA UNA CUENTA YA CREADA DENTRO DE LA COLECIÓN
     * DE USUARIO EN FIREBASE. SI EL USUARIO NO EXISTIESE EN LA COLECIÓN LO CREARÍA, ALMACENANDO
     * SU NOMBRE, SU CORREO Y SU IMAGEN DE PERFIL DE GOOGLE */
    private fun saveUserToFireStore(name:String, email:String ,imgUrl: Uri?) {
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
                        goLogin()
                    }
                } else {
                    goLogin()
                }
            }
    }

    /** NOS LLEVA AL LOGIN */
    private fun goLogin() {
        var intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    /** EMPIEZA EL PROCESO DE SELECCIÓN DE LA IMAGEN */
    private fun startImagePicker(){
        pickerDialog {
            setTitle(R.string.select_from)
            setTitleTextBold(true)
            setTitleTextSize(22f)
            setTitleGravity(Gravity.START)
            setItems(
                setOf(
                    ItemModel(ItemType.Camera, backgroundType = ShapeType.TYPE_CIRCLE, itemBackgroundColor = Color.rgb(182, 184, 214)),
                    ItemModel(ItemType.ImageGallery(MimeType.Image.All), itemBackgroundColor = Color.rgb(182, 184, 214)),
                )
            )
            setListType(PickerDialog.ListType.TYPE_GRID)
        }.setPickerCloseListener { type, uris ->
            when (type) {
                ItemType.Camera -> setChoosenImg(uris.first())
                is ItemType.ImageGallery -> {
                    setChoosenImg(uris.first())
                }
            }
        }.show()
    }

    /** ESTABLECE LA IMAGEN SELECCIONADA EN UNA VARIABLE PARA SU POSTERIOR USO */
    private fun setChoosenImg(uri: Uri){
        selectedPhotoUri = uri
        binding.registerImage.setImageURI(selectedPhotoUri)
    }

    /** SUBE LA IMAGEN A FIREBASE FIRESTORAGE Y DEVUELVE LA URI DE LA IMAGEN
     * PARA PODER CONTINUAR CON EL PROCESO */
    private fun uploadImageToFirebaseStorage(name: String, email: String) {
        if (selectedPhotoUri == null){
            Toast.makeText(this, R.string.select_image, Toast.LENGTH_LONG).show()
        } else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveUserToFireStore(name, email, it)
                    }
                }
        }
    }

}