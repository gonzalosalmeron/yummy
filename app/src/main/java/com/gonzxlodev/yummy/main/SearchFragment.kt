package com.gonzxlodev.yummy.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SearchFragment : Fragment() {

    /** VARIABLES */
    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var listAdapter: ListAdapter
    private lateinit var db: FirebaseFirestore

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

        listAdapter = ListAdapter(recipesArrayList, activity as Context)

        homeRecyclerView.apply {
            layoutManager = GridLayoutManager(activity, 2)
            setHasFixedSize(false)
            adapter = listAdapter
        }

        eventChangeListener()

    }

    /** LLAMAMOS A TODAS LAS RECETAS DE LOS USUARIOS ORDENADAS POR LA FECHA DE CREACIÓN */
    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("recipes").orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }
                    for (dc : DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            recipesArrayList.add(dc.document.toObject(Recipe::class.java))
                        }
                    }

                    if (homeRecyclerView != null) {
//                        homeRecyclerView.scrollToPosition(0)
                    }

                    listAdapter.notifyDataSetChanged()
//                    listAdapter.notifyDataSetChanged()
                }

            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}