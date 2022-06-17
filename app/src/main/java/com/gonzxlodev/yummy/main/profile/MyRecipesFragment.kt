package com.gonzxlodev.yummy.main.profile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout.HORIZONTAL
import android.widget.GridLayout.VERTICAL
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.ListAdapter
import com.gonzxlodev.yummy.adapter.MyRecipesAdapter
import com.gonzxlodev.yummy.databinding.FragmentMyRecipesBinding
import com.gonzxlodev.yummy.model.Recipe
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_my_recipes.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.gonzxlodev.yummy.databinding.FragmentProfileBinding
import com.gonzxlodev.yummy.main.MainActivity
import org.w3c.dom.Text
import androidx.fragment.app.FragmentManager


class MyRecipesFragment : Fragment() {

    /** VARIABLES */
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var listAdapter: MyRecipesAdapter
    private lateinit var db: FirebaseFirestore

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

        eventChangeListener()
    }

    /** LLAMADA A FIREBASE PARA CARGAR LAS RECETAS DEL USUARIO ACTUALMENTE LOGUEADO */
    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("recipes").orderBy("created_at", Query.Direction.DESCENDING)
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
                            recipesArrayList.add(recipe)
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                    saveRecipes(recipesArrayList.size)


                    /** SI NO HAY RECETAS SE MUESTRA UNA IMÁGEN Y TEXTO INDICÁNDOLO */
                    if(recipesArrayList.size == 0) {
                        binding.myRecipesNoRecipesBox.visibility = View.VISIBLE
                    } else {
                        binding.myRecipesNoRecipesBox.visibility = View.GONE
                    }
                }
            })

    }

    /** ALMACENA LOS DATOS DEL USUARIO QUE HA INICIADO SESIÓN EN LAS SHARED PREFERENCES */
    private fun saveRecipes(recipes: Int) {
        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)!!.edit()
        prefs.putString("recipes", recipes.toString())
        prefs.apply()
    }

}