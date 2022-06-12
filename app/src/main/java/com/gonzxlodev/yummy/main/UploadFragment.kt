package com.gonzxlodev.yummy.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentUploadBinding
import com.gonzxlodev.yummy.model.Category
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class UploadFragment : Fragment() {

    /** variables */
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private var selectedPhotoUri: Uri? = null
    private var user: FirebaseUser? = null
    private lateinit var categoriesArrayList: ArrayList<Category>
    private var category: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        /** GETS THE CURRENT USER */
        user = FirebaseAuth.getInstance().currentUser
        user.let {
            val email = user?.email
        }

        /** GET AND SET CATEGORIES */
        categoriesArrayList = arrayListOf()
        getAndSetCategories()

        binding.chooseImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding.uploadUploadBtn.setOnClickListener {
            var name = binding.uploadNameEd.text.toString().trim()
            var description = binding.uploadDescriptionEd.text.toString().trim()

            if (name.isNotEmpty() && description.isNotEmpty()) {
                this.uploadImageToFirebaseStorage()
            } else {
                Toast.makeText(activity, "Please fill all the camps", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** starts the process of selecting the img */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            binding.choosenImgImg.visibility = View.INVISIBLE
            binding.choosenImgText.visibility = View.INVISIBLE
            selectedPhotoUri = data.data
            binding.chooseImageView.setImageURI(selectedPhotoUri)
        }
    }

    /** uploads the image and returns the url to continue the process */
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null){
            Toast.makeText(activity, "Please select an image first", Toast.LENGTH_LONG).show()
        } else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

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
                "name" to binding.uploadNameEd.text.toString().trim().capitalize(),
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
            binding.uploadNameEd.setText("")
            binding.uploadIngredientsEd.setText("")
            binding.uploadDinersEd.setText("")
            binding.uploadTimeEd.setText("")
            binding.uploadDescriptionEd.setText("")
//            chip
            binding.chooseImageView.setImageURI(null)
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
                            .addView(createTagChip(activity as AppCompatActivity, category.name!!))
                    }
                }

            })
    }

    /** CUSTOM METHOD FOR CREATE CHIPS DINAMICALLY */
    private fun createTagChip(context: Context, chipName: String): Chip {
        return Chip(context).apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}