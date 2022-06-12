package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.RecipeCard2Binding
import com.gonzxlodev.yummy.databinding.RecipeCardBinding
import com.gonzxlodev.yummy.main.DetailActivity
import com.gonzxlodev.yummy.main.RecipeFragment
import com.gonzxlodev.yummy.main.SearchFragment
import com.gonzxlodev.yummy.model.Recipe
import com.gonzxlodev.yummy.uitel.loadImage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import com.google.firebase.storage.StorageReference




class MyRecipesAdapter(private val recipeList: ArrayList<Recipe>, private val context: Context, private val fragment: Fragment):
    RecyclerView.Adapter<MyRecipesAdapter.ViewHolder>()
{
    private lateinit var db: FirebaseFirestore
    var onItemClick: ((Recipe) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_card_2, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val recipe = recipeList[position]

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

        viewHolder.itemView.setOnLongClickListener(object: View.OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                db = FirebaseFirestore.getInstance()
                val showPopUp = PopupMenu(context, viewHolder.binding.recipe2CardImage)
                showPopUp.inflate(R.menu.recipe_popup)
                showPopUp.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.recipe_popup_edit -> {

                        }
                        R.id.recipe_popup_delete -> {
                            FirebaseStorage.getInstance().getReferenceFromUrl(recipe.imgUrl!!).delete()
                            db.collection("recipes").document(recipe.id!!).delete()
                            recipeList.remove(recipe)
                            notifyItemRemoved(position)

                            Snackbar.make(viewHolder.binding.root, R.string.recipe_deleted, Snackbar.LENGTH_LONG).show()

                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                showPopUp.show()

                return true
            }
        })
    }


    override fun getItemCount(): Int {
        return recipeList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        var binding = RecipeCard2Binding.bind(view)

        val imageView: ImageView = binding.recipe2CardImage

    }
}