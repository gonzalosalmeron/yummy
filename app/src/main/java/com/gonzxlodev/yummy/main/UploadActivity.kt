package com.gonzxlodev.yummy.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityUploadBinding
import com.gonzxlodev.yummy.model.Category
import com.gonzxlodev.yummy.model.Recipe
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UploadActivity : AppCompatActivity() {

    /** VARIABLES */
    private lateinit var binding: ActivityUploadBinding

    private var selectedPhotoUri: Uri? = null
    private lateinit var categoriesArrayList: ArrayList<Category>
    private var category: String? = null
    var createdAt: Date? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uploadCloseBtn.setOnClickListener { finish() }

        /** RECOGE Y ESTABLECE TODAS LAS CATEGORÍAS EXISTENTES */
        categoriesArrayList = arrayListOf()
        getAndSetCategories()

        binding.chooseImageView.setOnClickListener {
            startImagePicker()
        }

        binding.uploadUploadBtn.setOnClickListener {
            var name = binding.uploadNameEd.text.toString().trim()
            var description = binding.uploadDescriptionEd.text.toString().trim()
            var diners = binding.uploadDinersEd.text.toString().trim()
            var time = binding.uploadTimeEd.text.toString().trim()

            if (name.isNotEmpty() && description.isNotEmpty() && diners.isNotEmpty() && time.isNotEmpty()) {
                if( getIntent().getExtras() == null) {
                    Log.i("hola", "por aqui no")
                    uploadImageToFirebaseStorage()
                } else if (selectedPhotoUri == null) {
                    Log.i("hola", "por aqui si")
                    updateRecipe(intent.getStringExtra("imgUrl").toString())
                } else {
                    Log.i("hola", "por aqui no, ${selectedPhotoUri.toString()}")
                    uploadImageToFirebaseStorage()
                }
            } else {
                Toast.makeText(this, "Please fill all the camps", Toast.LENGTH_LONG).show()
            }
        }

        /** EN CASO DE QUE VAYAMOS A EDITAR LA RECETA, RECOGE LOS DATOS DE LOS PUTEXTRA */
        if( getIntent().getExtras() != null) {
            getParamsRecipe()
            binding.choosenImgImg.visibility = View.INVISIBLE
            binding.choosenImgText.visibility = View.INVISIBLE
            binding.uploadUploadBtn.text = getString(R.string.edit)
        }
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
        binding.choosenImgImg.visibility = View.INVISIBLE
        binding.choosenImgText.visibility = View.INVISIBLE
        selectedPhotoUri = uri
        binding.chooseImageView.setImageURI(selectedPhotoUri)
    }

    /** SUBE LA IMAGEN A FIREBASE FIRESTORAGE Y DEVUELVE LA URI DE LA IMAGEN
     * PARA PODER CONTINUAR CON EL PROCESO */
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null){
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show()
        } else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            binding.uploadCloseBtn.isClickable = false
            binding.uploadUploadBtn.visibility = View.GONE
            binding.uploadProgressBar.visibility = View.VISIBLE
            binding.uploadProgressBar.isIndeterminate = true

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        if( getIntent().getExtras() != null && selectedPhotoUri != null) {
                            updateRecipe(it.toString())
                        } else {
                            saveRecipeToFireStoreDatabase(it.toString())
                        }
                    }
                }
        }
    }

    /** GUARDA TODOS LOS DATOS DE LA RECETA EN FIRESTORE */
    private fun saveRecipeToFireStoreDatabase(imgUrl: String) {
        db.collection("recipes").add(
            hashMapOf(
                "name" to binding.uploadNameEd.text.toString().trim().replaceFirstChar(Char::titlecase),
                "ingredients" to binding.uploadIngredientsEd.text.toString().trim(),
                "diners" to binding.uploadDinersEd.text.toString().trim(),
                "preparation_time" to binding.uploadTimeEd.text.toString().trim(),
                "description" to binding.uploadDescriptionEd.text.toString().trim(),
                "tag" to category,
                "imgUrl" to imgUrl,
                "user_email" to getEmail(),
                "user_name" to getUserName(),
                "user_imgUrl" to getUserImgUrl(),
                "created_at" to FieldValue.serverTimestamp()
            )

        ).addOnSuccessListener { taskSnapshot ->
            binding.uploadProgressBar.visibility = View.INVISIBLE
            binding.uploadProgressBar.isIndeterminate = false
            Snackbar.make(binding.root, "Recipe uploaded!", Snackbar.LENGTH_LONG).show()
            finish()
        }
    }

    /** ACTUALIZA TODOS LOS DATOS DE LA RECETA EN FIRESTORE */
    private fun updateRecipe(imgUrl: String) {
        db.collection("recipes").document(intent.getStringExtra("id").toString()).set(
            hashMapOf(
                "name" to binding.uploadNameEd.text.toString().trim().replaceFirstChar(Char::titlecase),
                "ingredients" to binding.uploadIngredientsEd.text.toString().trim(),
                "diners" to binding.uploadDinersEd.text.toString().trim(),
                "preparation_time" to binding.uploadTimeEd.text.toString().trim(),
                "description" to binding.uploadDescriptionEd.text.toString().trim(),
                "tag" to category,
                "imgUrl" to imgUrl,
                "user_email" to getEmail(),
                "user_name" to getUserName(),
                "user_imgUrl" to getUserImgUrl(),
                "created_at" to createdAt
            )

        ).addOnSuccessListener { taskSnapshot ->
            binding.uploadProgressBar.visibility = View.INVISIBLE
            binding.uploadProgressBar.isIndeterminate = false
            Snackbar.make(binding.root, R.string.recipe_updated, Snackbar.LENGTH_LONG).show()
            finish()
        }
    }

    /** RECOGE TODAS LAS CATEGORÍAS Y LAS ESTABLECE EN CHIPS DE MATERIAL DESIGN */
    private fun getAndSetCategories() {
        db.collection("categories").orderBy("name", Query.Direction.DESCENDING)
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }
                    for (dc : DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            categoriesArrayList.add(dc.document.toObject(Category::class.java))
                        }
                    }
                    for (category in categoriesArrayList) {
                        binding.uploadChipgroup
                            .addView(createTagChip(category.name!!))
                    }
                }

            })
    }

    /** ESTE MÉTODO CREA UN CHIP */
    private fun createTagChip(chipName: String): Chip {
        return Chip(this).apply {
            text = chipName
            setChipBackgroundColorResource(R.color.yummy_green)
            isCloseIconVisible = false
            isCheckable = true
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setOnClickListener {
                category = chipName
            }
        }

    }

    /** RECOGE EL EMAIL DE LAS SHARED PREFERENCES */
    fun getEmail(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)

        return email.toString()
    }

    /** RECOGE LA IMAGEN DEL USUARIO DE LAS SHARED PREFERENCES */
    fun getUserImgUrl(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val imgUrl = prefs?.getString("imgUrl", null)

        return imgUrl.toString()
    }

    /** RECOGE EL NOMBRE DE LAS SHARED PREFERENCES */
    fun getUserName(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val name = prefs?.getString("name", null)

        return name.toString()
    }

    /** EN CASO DE QUE VAYAMOS A EDITAR LA RECETA, RECOGE LOS DATOS DE LOS PUTEXTRA */
    private fun getParamsRecipe() {
        var name = intent.getStringExtra("name").toString()
        var ingredients = intent.getStringExtra("ingredients").toString()
        var diners = intent.getStringExtra("diners").toString()
        var time = intent.getStringExtra("time").toString()
        var description = intent.getStringExtra("description").toString()
        var imgUrl = intent.getStringExtra("imgUrl").toString()

        val formatData = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        createdAt = formatData.parse(intent.getStringExtra("created_at"))

        binding.uploadNameEd.setText(name)
        binding.uploadIngredientsEd.setText(ingredients)
        binding.uploadDinersEd.setText(diners)
        binding.uploadTimeEd.setText(time)
        binding.uploadDescriptionEd.setText(description)
        Glide.with(this)
            .load(Uri.parse(imgUrl))
            .skipMemoryCache(true)
            .into(binding.chooseImageView)
    }


}