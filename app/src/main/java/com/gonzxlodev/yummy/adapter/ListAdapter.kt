package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.RecipeCardBinding
import com.gonzxlodev.yummy.main.DetailActivity
import com.gonzxlodev.yummy.main.RecipeFragment
import com.gonzxlodev.yummy.main.SearchFragment
import com.gonzxlodev.yummy.model.Recipe
import com.gonzxlodev.yummy.uitel.loadImage

class ListAdapter(private val recipeList: ArrayList<Recipe>, private val context: Context):
    RecyclerView.Adapter<ListAdapter.ViewHolder>()
{

    var onItemClick: ((Recipe) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_card, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val recipe = recipeList[position]

        viewHolder.setIsRecyclable(false);

        viewHolder.name.text = recipe.name
        Glide.with(context)
            .load(recipe.imgUrl)
            .skipMemoryCache(true)
            .into(viewHolder.imageView)

        viewHolder.itemView.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString("imgUrl", recipe.imgUrl)
            bundle.putString("name", recipe.name)
            bundle.putString("time", recipe.preparation_time)
            bundle.putString("diners", recipe.diners)
            bundle.putString("ingredients", recipe.ingredients)
            bundle.putString("description", recipe.description)

            val activity = view!!.context as AppCompatActivity
            val fragment = RecipeFragment()
            fragment.arguments = bundle
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_container, fragment)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }
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