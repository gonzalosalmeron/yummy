package com.gonzxlodev.yummy.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentRecipeBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*


class RecipeFragment : Fragment() {

    /** VARIABLES */
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater)
        var view = binding.root

        /** CARGA LOS DATOS DE LA RECETA SELECCIONADA */
        Glide.with(activity as Context)
            .load(arguments?.getString("imgUrl"))
            .skipMemoryCache(true)
            .into(binding.recipeImg)
        binding.recipeName.text = arguments?.getString("name")
        if(arguments?.getString("tag") != null){
            binding.recipeTag.text = "#${arguments?.getString("tag")}"
        }
        binding.recipeTime.text = arguments?.getString("time")
        binding.recipeDiners.text = arguments?.getString("diners")
        binding.recipeIngredients.text = arguments?.getString("ingredients")
        binding.recipeDescription.text = arguments?.getString("description")
        binding.recipeUserName.text = arguments?.getString("user_name")

        val stringToDate = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        var createdAt = stringToDate.parse(arguments?.getString("created_at")!!)

        val DateToString = SimpleDateFormat("d-MM-yyyy", Locale.ROOT)
        var date = DateToString.format(createdAt!!)
        binding.recipeCreatedAt.text = date

        if (arguments?.getString("user_imgUrl") != null) {
            Glide.with(activity as Context)
                .load(arguments?.getString("user_imgUrl"))
                .skipMemoryCache(true)
                .into(binding.recipeUserImage)
        }

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}