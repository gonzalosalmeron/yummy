package com.gonzxlodev.yummy.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.BagAdapter
import com.gonzxlodev.yummy.databinding.FragmentBagBinding
import com.gonzxlodev.yummy.model.Bag
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.bag_dialog.view.*
import kotlinx.android.synthetic.main.fragment_bag.*
import java.lang.reflect.Array
import java.util.*
import kotlin.collections.ArrayList

class BagFragment : Fragment() {

    /** VARIABLES */
    private var _binding: FragmentBagBinding? = null
    private val binding get() = _binding!!

    private lateinit var bagsArrayList: ArrayList<Bag>
    private lateinit var bagAdapter: BagAdapter
    private lateinit var dataBag: String
    private var deleteBags: ArrayList<Bag> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBagBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_bag, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        /** INICIALIZAMOS EL ARRAY Y LLAMAMOS A FIREBASE */
        bagsArrayList = arrayListOf()
        bagAdapter = BagAdapter(bagsArrayList, activity as Context)
        eventChangeListener()

        /** SETEA LA LAYOUT PARA EL RECYCLER VIEW */
        bagRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(false)
            adapter = bagAdapter
        }

        /** ACTÚA CUANDO DAMOS CLICK SOBRE EL BOTÓN FLOTANTE DE AÑADIR */
        bag_add_btn.setOnClickListener {

            /** INFLA LA VISTA QUE VA A CARGAR EN EL DIALOG */
            val bagDialog: View = layoutInflater.inflate(R.layout.bag_dialog, binding.root, false)
            bagDialog.removeSelf()
            MaterialAlertDialogBuilder(context as Context)
                .setView(bagDialog)
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                    // Respond to negative button press
                }
                .setPositiveButton(resources.getString(R.string.create)) { dialog, which ->
                    dataBag = bagDialog.bag_dialog_input.text.toString().trim()
                    createBag()
                }
                .show()
        }
        /** ACTÚA CUANDO DAMOS CLICK SOBRE EL BOTÓN FLOTANTE DE ELIMINAR */
        bag_delete_btn.setOnClickListener {

            /** CREA UN NUEVO ARRAY CON LOS ALIMENTOS YA MARCADOS COMO COMPLETADOS  */
            bagsArrayList.map { bag ->
                if (bag.completed!!){
                    deleteBags.add(bag)
                }
            }
            if (deleteBags.size > 0){
                deleteBags()
            } else {
                Snackbar.make(view as View, R.string.no_items_in_bag, Snackbar.LENGTH_LONG)
                    .setAction(R.string.close) {
                        /** NO HACE NADA */
                    }
                    .show()
            }
        }

    }

    // ESTE MÉTODO ELIMINA LA VISTA HIJO (LA VISTA INFLADA EN EL DIALOG)
    // DEL HIJO PARA QUE NO PETE AL INFLARLA DE NUEVO
    private fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    /** AÑADE UN ALIMENTO A LA BOLSA Y LO SUBE A FIREBASE */
    private fun createBag() {
//        Log.i("bagItem", "${dataBag}")
        (activity as MainActivity).db.collection("bags").add(
            hashMapOf(
                "name" to dataBag,
                "completed" to false,
                "user_email" to (activity as MainActivity).getEmail(),
                "created_at" to FieldValue.serverTimestamp()
            )

        ).addOnSuccessListener { taskSnapshot ->
            Snackbar.make(view as View, "Bag Item Uploaded!", Snackbar.LENGTH_LONG)
                .setAction(R.string.close) { /** NOTHING */ }
                .setActionTextColor(resources.getColor(R.color.yummy_purple, null))
                .show()
        }

    }

    /** ELIMINA LOS ALIMENTOS MARCADOS COMO COMPLETADOS */
    private fun deleteBags() {
        /** UTILIZAMOS UN BATCH PARA MANDAR UNA ÚNICA PETICIÓN
         * A FIREBASE Y NO CARGAR LAS PETICIONES */
        var batch = FirebaseFirestore.getInstance().batch()
        deleteBags.forEach {
            var docRef = (activity as MainActivity).db.collection("bags").document(it.id!!)
            batch.delete(docRef)
            bagsArrayList.remove(it)
        }
        bagAdapter.notifyDataSetChanged()

        batch.commit()

    }

    /** LLAMADA A FIREBASE PARA RECOGER TODOS LOS ALIMENTOS QUE TENGA EN LA BOLSA
     * EL USUARIO LOGUEADO ACTUALMENTE */
    private fun eventChangeListener() {
        (activity as MainActivity).db.collection("bags").orderBy("created_at", Query.Direction.ASCENDING)
            .whereEqualTo("user_email", (activity as MainActivity).getEmail())
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("firestoreerror", error.message.toString())
                        return
                    }

                    for (dc : DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            var bag = dc.document.toObject(Bag::class.java)
                            bag.id = dc.document.id

                            if (bagsArrayList.indexOf(bag) == -1) bagsArrayList.add(bag)

                            bagAdapter.notifyItemInserted(bagsArrayList.size)
                        }
                    }
//                    if(bagsArrayList.size == 0) {
//                        binding.myRecipesNoRecipesBox.visibility = View.VISIBLE
//                    } else {
//                        binding.myRecipesNoRecipesBox.visibility = View.GONE
//                    }
                }
            })

    }
}