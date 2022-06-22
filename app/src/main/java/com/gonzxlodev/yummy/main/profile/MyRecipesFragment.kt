package com.gonzxlodev.yummy.main.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.MyRecipesAdapter
import com.gonzxlodev.yummy.databinding.FragmentMyRecipesBinding
import com.gonzxlodev.yummy.model.Recipe
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_my_recipes.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.gonzxlodev.yummy.main.MainActivity


class MyRecipesFragment : Fragment() {

    /** VARIABLES */
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var listAdapter: MyRecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        /** INICIALIZAMOS EL ARRAY Y EL ADAPTADOR */
        recipesArrayList = arrayListOf()
        val myRecipesFragment = com.gonzxlodev.yummy.main.profile.MyRecipesFragment()
        listAdapter = MyRecipesAdapter(recipesArrayList, activity as Context, myRecipesFragment)

        my_recipes_recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            setHasFixedSize(false)
            adapter = listAdapter

            /** SEPARA LAS COLUMNAS Y FILAS CON UNA LÍNEA DE COLOR BLANCO */
            val dividerItemDecorationVertical = DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )

            dividerItemDecorationVertical
                .setDrawable(context.resources.getDrawable(R.drawable.divider_item_decoration))

            val dividerItemDecorationHorizontal = DividerItemDecoration(
                context,
                LinearLayoutManager.HORIZONTAL
            )
            dividerItemDecorationHorizontal
                .setDrawable(context.resources.getDrawable(R.drawable.divider_item_decoration))

            addItemDecoration(dividerItemDecorationHorizontal)
            addItemDecoration(dividerItemDecorationVertical)
        }
        checkEmptyArray(recipesArrayList.size)
        eventChangeListener()
    }


    /** COMPRUEBA LA LONGITUD DEL ARRAY Y SI ESTÁ VACÍO MUESTRA UNA MENSAJE */
    fun checkEmptyArray(size: Int){
        if(size < 1) {
            binding.myRecipesNoRecipes.setAnimation(R.raw.animation_no_recipes)
            binding.myRecipesNoRecipes.repeatCount = Animation.INFINITE
            binding.myRecipesNoRecipes.playAnimation()
            binding.myRecipesNoRecipesBox.visibility = View.VISIBLE
        } else {
            binding.myRecipesNoRecipesBox.visibility = View.GONE
        }
    }

    /** LLAMADA A FIREBASE PARA CARGAR LAS RECETAS DEL USUARIO ACTUALMENTE LOGUEADO */
    private fun eventChangeListener() {
        (activity as MainActivity).db.collection("recipes").orderBy("created_at", Query.Direction.ASCENDING)
            .whereEqualTo("user_email", (activity as MainActivity).getEmail())
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }
                    for (dc : DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED){
                            var recipe = dc.document.toObject(Recipe::class.java)
                            recipe.id = dc.document.id

                            if (recipesArrayList.indexOf(recipe) == -1) {
                                recipesArrayList.add(0, recipe)
                                listAdapter.notifyItemInserted(0)
                            }

                            checkEmptyArray(recipesArrayList.size)

                        }
                        if (dc.type == DocumentChange.Type.MODIFIED) {
                            var newRecipe = dc.document.toObject(Recipe::class.java)
                            newRecipe.id = dc.document.id

                            /** COMPRUEBA SI LA ID DEL DOCUMENTO ACTUALIZADO ESTÁ EN EL ARRA */
                            var position = 0
                            for (recipe in recipesArrayList) {
                                if (recipe.id == dc.document.id) {
                                    position = recipesArrayList.indexOf(recipe)
                                    recipesArrayList.set(position, newRecipe)
                                    listAdapter.notifyItemChanged(position)
                                }
                            }
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            var deletedRecipe = dc.document.toObject(Recipe::class.java)
                            deletedRecipe.id = dc.document.id

                            if (deletedRecipe.id != null) {
                                if (recipesArrayList.indexOf(deletedRecipe) != -1) {
                                    Log.i("eliminartest", "${recipesArrayList.indexOf(deletedRecipe)}")
                                    listAdapter.notifyItemRemoved(recipesArrayList.indexOf(deletedRecipe))
                                    recipesArrayList.removeAt(recipesArrayList.indexOf(deletedRecipe))
                                    checkEmptyArray(recipesArrayList.size)
                                }
                            }
                        }
                    }
                }
            })

    }

}