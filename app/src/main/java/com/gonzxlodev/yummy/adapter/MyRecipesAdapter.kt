package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.RecipeCard2Binding
import com.gonzxlodev.yummy.main.RecipeFragment
import com.gonzxlodev.yummy.main.UploadActivity
import com.gonzxlodev.yummy.model.Recipe
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.Serializable


class MyRecipesAdapter(private val recipeList: ArrayList<Recipe>, private val context: Context, private val fragment: Fragment):
    RecyclerView.Adapter<MyRecipesAdapter.ViewHolder>()
{
    private lateinit var db: FirebaseFirestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_card_2, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val recipe = recipeList[position]

        /** CARGAMOS TODOS LOS DATOS DE LA RECETA EN SU CARD */
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

            val activity = view!!.context as AppCompatActivity
            val fragment = RecipeFragment()
            fragment.arguments = bundle
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_container, fragment)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }

        /** SI MANTENEMOS LA PULSACIÓN SOBRE UNA RECETA SE INICIA EL MENÚ CONTEXTUAL */
        viewHolder.itemView.setOnLongClickListener(object: View.OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                /** LLAMA A FIREBASE */
                db = FirebaseFirestore.getInstance()

                /** LLAMA AL MENÚ CONTEXTUAL */
                val showPopUp = PopupMenu(context, viewHolder.binding.recipe2CardImage)
                showPopUp.inflate(R.menu.recipe_popup)
                showPopUp.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        /** SI DAMOS CLICK EN LA OPCIÓN DE EDITAR, LANZA LA ACTIVIDAD DONDE
                         * PODREMOS EDITAR LA RECETA */
                        R.id.recipe_popup_edit -> {
                            val intent = Intent(view!!.context, UploadActivity::class.java)
                            intent.putExtra("id", recipe.id)
                            intent.putExtra("imgUrl", recipe.imgUrl)
                            intent.putExtra("name", recipe.name)
                            intent.putExtra("time", recipe.preparation_time)
                            intent.putExtra("diners", recipe.diners)
                            intent.putExtra("ingredients", recipe.ingredients)
                            intent.putExtra("description", recipe.description)
                            intent.putExtra("created_at", recipe.created_at.toString())
                            context.startActivity(intent)
                        }
                        /** SI DAMOS CLICK EN LA OPCIÓN DE ELIMINAR, ELIMINA LA RECETA */
                        R.id.recipe_popup_delete -> {
                            FirebaseStorage.getInstance().getReferenceFromUrl(recipe.imgUrl!!).delete()
                            db.collection("recipes").document(recipe.id!!).delete()
                                .addOnSuccessListener {
                                    recipeList.remove(recipe)
                                    notifyItemRemoved(position)
                                    notifyDataSetChanged()
                                }


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