package com.gonzxlodev.yummy.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentRecipeBinding
import com.bumptech.glide.Glide


class RecipeFragment : Fragment() {

    /** variables */
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeBinding.inflate(inflater)
        var view = binding.root

        Glide.with(activity as Context)
            .load(arguments?.getString("imgUrl"))
            .skipMemoryCache(true)
            .into(binding.recipeImg)
        binding.recipeName.text = arguments?.getString("name")
        binding.recipeTime.text = arguments?.getString("time")
        binding.recipeDiners.text = arguments?.getString("diners")
        binding.recipeIngredients.text = arguments?.getString("ingredients")
        binding.recipeDescription.text = arguments?.getString("description")

        return view
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}