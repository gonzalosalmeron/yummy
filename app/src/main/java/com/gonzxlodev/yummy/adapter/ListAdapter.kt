package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.RecipeCardBinding
import com.gonzxlodev.yummy.main.DetailActivity
import com.gonzxlodev.yummy.model.Recipe
import com.gonzxlodev.yummy.uitel.loadImage
import com.squareup.picasso.Picasso

class ListAdapter(private val recipeList: ArrayList<Recipe>):
RecyclerView.Adapter<ListAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_card, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val recipe = recipeList[position]

        viewHolder.name.text = recipe.name
        Picasso.get().load(recipe.imgUrl).into(viewHolder.imageView)
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        var binding = RecipeCardBinding.bind(view)

        val name: TextView = binding.recipeCardName
        val imageView: ImageView = binding.recipeCardImage

    }
}