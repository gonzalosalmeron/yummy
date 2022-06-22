package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.RecipeCardBinding
import com.gonzxlodev.yummy.main.RecipeFragment
import com.gonzxlodev.yummy.model.Recipe
import java.time.ZonedDateTime

class ListAdapter(private val recipeList: ArrayList<Recipe>, private val context: Context):
    RecyclerView.Adapter<ListAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_card, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val recipe = recipeList[position]
//        viewHolder.setIsRecyclable(false);

        /** CARGAMOS TODOS LOS DATOS DE LA RECETA EN SU CARD */
        viewHolder.name.text = recipe.name
        Glide.with(context)
            .load(recipe.imgUrl)
            .skipMemoryCache(true)
            .into(viewHolder.imageView)

        /** SI HACEMOS CLICK EN LA CARD INICIA UNA ACTIVIDAD PARA VER LOS DATOS DE LA
         * RECETA DETALLADAMENTE */
        viewHolder.itemView.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString("imgUrl", recipe.imgUrl)
            bundle.putString("name", recipe.name)
            bundle.putString("time", recipe.preparation_time)
            bundle.putString("diners", recipe.diners)
            bundle.putString("ingredients", recipe.ingredients)
            bundle.putString("description", recipe.description)
            bundle.putString("tag", recipe.tag)
            bundle.putString("user_name", recipe.user_name)
            bundle.putString("user_imgUrl", recipe.user_imgUrl)
            bundle.putString("created_at", recipe.created_at.toString())

            val activity = view!!.context as AppCompatActivity
            val fragment = RecipeFragment()
            fragment.arguments = bundle
            activity.supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                replace(R.id.nav_container, fragment as Fragment)
                addToBackStack(null)
                setReorderingAllowed(true)
            }
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