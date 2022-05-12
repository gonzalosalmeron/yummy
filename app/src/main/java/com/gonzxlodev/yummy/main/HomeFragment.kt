package com.gonzxlodev.yummy.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.ListAdapter
import com.gonzxlodev.yummy.databinding.FragmentHomeBinding
import com.gonzxlodev.yummy.model.Recipe
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.log

class HomeFragment : Fragment() {

    private lateinit var dbref: DatabaseReference
    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var listAdapter: ListAdapter
    private lateinit var db: FirebaseFirestore

    private var _binding:FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater)
        var view = binding.root
        return view
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        recipesArrayList = arrayListOf()

        listAdapter = ListAdapter(recipesArrayList)

        homeRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = listAdapter
        }

        EventChangeListener()
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("recipes")
            .addSnapshotListener(object: EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }

                    for (dc :DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            recipesArrayList.add(dc.document.toObject(Recipe::class.java))
                        }
                    }

                    listAdapter.notifyDataSetChanged()
                }

            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        mDatabaseRef!!.removeEventListener(mDBListener!!)
    }
}