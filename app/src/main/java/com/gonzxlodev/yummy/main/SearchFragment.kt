package com.gonzxlodev.yummy.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.ListAdapter
import com.gonzxlodev.yummy.databinding.FragmentSearchBinding
import com.gonzxlodev.yummy.model.Recipe
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_home.*

import android.widget.TextView
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    /** VARIABLES */
    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var tempArray: ArrayList<Recipe>
    private lateinit var listAdapter: ListAdapter

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater)
        var view = binding.root
        return view
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        /** INICIALIZAMOS EL ARRAY Y EL ADAPTADOR */
        recipesArrayList = arrayListOf()
        tempArray = arrayListOf()

        listAdapter = ListAdapter(tempArray, activity as Context)

        homeRecyclerView.apply {
            layoutManager = GridLayoutManager(activity, 2)
            setHasFixedSize(false)
            adapter = listAdapter
        }
        eventChangeListener()
//        binding.homeSearchView.setlis {
//            if (tempArray.size == 0) {
//                binding.searchNoRecipesBox.visibility = View.VISIBLE
//            } else {
//                binding.searchNoRecipesBox.visibility = View.GONE
//            }
//        }
        binding.homeSearchView.setOnCloseListener {
            Log.i("cerrar", "hola")
            true
        }
        binding.homeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String): Boolean {
                tempArray.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if(searchText.isNotEmpty()){
                    recipesArrayList.forEach{
                        if (it.name!!.lowercase(Locale.getDefault()).contains(searchText)){
                            tempArray.add(it)
                        }
                    }
                    if (tempArray.size == 0) binding.searchNoRecipesBox.visibility = View.VISIBLE
                    else binding.searchNoRecipesBox.visibility = View.GONE
                    homeRecyclerView.adapter?.notifyDataSetChanged()
                    
                } else {
                    tempArray.clear()
                    tempArray.addAll(recipesArrayList)
                    binding.searchNoRecipesBox.visibility = View.GONE
                    homeRecyclerView.adapter?.notifyDataSetChanged()
                }

                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                recipesArrayList.filter { recipe ->
                    return true
                }

                return false
            }

        })
    }

    /** LLAMAMOS A TODAS LAS RECETAS DE LOS USUARIOS ORDENADAS POR LA FECHA DE CREACIÓN */
    private fun eventChangeListener() {
        (activity as MainActivity).db.collection("recipes").orderBy("created_at", Query.Direction.ASCENDING)
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }
                    for (dc : DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            var recipe = dc.document.toObject(Recipe::class.java)
                            recipe.id = dc.document.id

                            if (recipesArrayList.indexOf(recipe) == -1) {
                                recipesArrayList.add(0, recipe)
                                tempArray.add(0, recipe)
                                listAdapter.notifyItemInserted(0)
                            }

                        }
                        if (dc.type == DocumentChange.Type.MODIFIED) {
                            var newRecipe = dc.document.toObject(Recipe::class.java)
                            newRecipe.id = dc.document.id

                            /** COMPRUEBA SI LA ID DEL DOCUMENTO ACTUALIZADO ESTÁ EN EL ARRAY */
                            var position = 0
                            for (recipe in recipesArrayList) {
                                if (recipe.id == dc.document.id) {
                                    position = recipesArrayList.indexOf(recipe)
                                    recipesArrayList.set(position, newRecipe)
                                    tempArray.set(position, newRecipe)
                                    listAdapter.notifyItemChanged(position)
                                }
                            }
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            var deletedRecipe = dc.document.toObject(Recipe::class.java)
                            deletedRecipe.id = dc.document.id

                            if (deletedRecipe.id != null) {
                                if (recipesArrayList.indexOf(deletedRecipe) != -1) {
                                    listAdapter.notifyItemRemoved(recipesArrayList.indexOf(deletedRecipe))
                                    recipesArrayList.removeAt(recipesArrayList.indexOf(deletedRecipe))
                                    tempArray.removeAt(tempArray.indexOf(deletedRecipe))
                                }
                            }
                        }
                    }

                }

            })
    }
}