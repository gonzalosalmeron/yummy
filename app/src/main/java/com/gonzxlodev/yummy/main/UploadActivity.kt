package com.gonzxlodev.yummy.main

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.ListAdapter
import com.gonzxlodev.yummy.adapter.MyRecipesAdapter
import com.gonzxlodev.yummy.databinding.ActivityUploadBinding
import com.gonzxlodev.yummy.databinding.FragmentUploadBinding
import com.gonzxlodev.yummy.model.Category
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.robertlevonyan.components.picker.*
import io.grpc.InternalChannelz.id
import java.util.*

class UploadActivity : AppCompatActivity() {

    /** variables */
    private lateinit var binding: ActivityUploadBinding

    private var selectedPhotoUri: Uri? = null
    private var user: FirebaseUser? = null
    private lateinit var categoriesArrayList: ArrayList<Category>
    private var category: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uploadCloseBtn.setOnClickListener { finish() }

        /** GETS THE CURRENT USER */
        user = FirebaseAuth.getInstance().currentUser
        user.let {
            val email = user?.email
        }

        /** GET AND SET CATEGORIES */
        categoriesArrayList = arrayListOf()
        getAndSetCategories()

        binding.chooseImageView.setOnClickListener {
            startImagePicker()
        }

        binding.uploadUploadBtn.setOnClickListener {
            var name = binding.uploadNameEd.text.toString().trim()
            var description = binding.uploadDescriptionEd.text.toString().trim()

            if (name.isNotEmpty() && description.isNotEmpty()) {
                this.uploadImageToFirebaseStorage()
            } else {
                Toast.makeText(this, "Please fill all the camps", Toast.LENGTH_LONG).show()
            }
        }


    }

    /** starts the process of selecting the img */
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

    /** sets the choosen img */
    private fun setChoosenImg(uri: Uri){
        binding.choosenImgImg.visibility = View.INVISIBLE
        binding.choosenImgText.visibility = View.INVISIBLE
        selectedPhotoUri = uri
        binding.chooseImageView.setImageURI(selectedPhotoUri)
    }

    /** uploads the image and returns the url to continue the process */
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
                        saveRecipeToFireStoreDatabase(it.toString())
                    }
                }
        }
    }

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
                "user_email" to user!!.email,
                "created_at" to FieldValue.serverTimestamp()
            )

        ).addOnSuccessListener { taskSnapshot ->
            binding.uploadProgressBar.visibility = View.INVISIBLE
            binding.uploadProgressBar.isIndeterminate = false
            Snackbar.make(binding.root, "Recipe uploaded!", Snackbar.LENGTH_LONG).show()
            finish()
        }
    }

    /** GET AND SET CATEGORIES MATERIAL UI CHIPS */
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

    /** CUSTOM METHOD FOR CREATE CHIPS DINAMICALLY */
    private fun createTagChip(chipName: String): Chip {
        return Chip(this).apply {
            text = chipName
            setChipBackgroundColorResource(R.color.yummy_green)
            isCloseIconVisible = false
            isCheckable = true
            setTextColor(ContextCompat.getColor(context, R.color.black))
//            setTextAppearance(R.style.ChipTextAppearance)
            setOnClickListener {
                category = chipName
            }
        }

    }
}