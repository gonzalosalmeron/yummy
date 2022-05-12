package com.gonzxlodev.yummy.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentUploadBinding
import com.gonzxlodev.yummy.shared.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class UploadFragment : Fragment() {

    /** variables */
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private var selectedPhotoUri: Uri? = null
    private var user: FirebaseUser? = null

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

        /** gets the current logged user */
        user = FirebaseAuth.getInstance().currentUser
        user.let {
            val email = user?.email
        }
        
        binding.uploadImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding.uploadUploadBtn.setOnClickListener {
            this.uploadImageToFirebaseStorage()
        }
    }

    /** starts the process of selecting the img */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
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

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveRecipeToFireStoreDatabase(it.toString())
                    }
                }
        }


    }

    /** saves recipe in firestore */
    private fun saveRecipeToFireStoreDatabase(imgUrl: String) {
        var name = binding.uploadNameEd.text.toString().trim()
        var description = binding.uploadDescriptionEd.text.toString().trim()

        if (name.length > 0 && description.length > 0) {
            binding.uploadProgressBar.visibility = View.VISIBLE
            binding.uploadProgressBar.isIndeterminate = true
            db.collection("recipes").add(
                hashMapOf(
                    "user_email" to user!!.email,
                    "name" to name,
                    "description" to description,
                    "imgUrl" to imgUrl
                )
            ).addOnSuccessListener { taskSnapshot ->
                val handler = Handler()
                handler.postDelayed({
                    binding.uploadProgressBar.visibility = View.VISIBLE
                    binding.uploadProgressBar.isIndeterminate = false
                    binding.uploadProgressBar.progress = 0
                }, 500)
                Toast.makeText(activity, "Recipe uploaded!", Toast.LENGTH_LONG).show()
                name = ""
                description = ""
                binding.chooseImageView.setImageURI(null)
            }
        } else {
            Toast.makeText(activity, "Please fill all the camps", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}