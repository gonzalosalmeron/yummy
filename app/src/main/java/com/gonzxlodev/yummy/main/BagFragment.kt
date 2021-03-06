package com.gonzxlodev.yummy.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
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

        /** ACT??A CUANDO DAMOS CLICK SOBRE EL BOT??N FLOTANTE DE A??ADIR */
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
        /** ACT??A CUANDO DAMOS CLICK SOBRE EL BOT??N FLOTANTE DE ELIMINAR */
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

        checkEmptyArray(bagsArrayList.size)

    }

    // ESTE M??TODO ELIMINA LA VISTA HIJO (LA VISTA INFLADA EN EL DIALOG)
    // DEL HIJO PARA QUE NO PETE AL INFLARLA DE NUEVO
    private fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    /** A??ADE UN ALIMENTO A LA BOLSA Y LO SUBE A FIREBASE */
    private fun createBag() {
        (activity as MainActivity).db.collection("bags").add(
            hashMapOf(
                "name" to dataBag,
                "completed" to false,
                "user_email" to (activity as MainActivity).getEmail(),
                "created_at" to FieldValue.serverTimestamp()
            )

        ).addOnSuccessListener { taskSnapshot ->
            Snackbar.make(view as View, R.string.item_uploaded, Snackbar.LENGTH_LONG)
                .setAction(R.string.close) { /** NOTHING */ }
                .setActionTextColor(resources.getColor(R.color.yummy_purple, null))
                .show()
        }

    }

    /** COMPRUEBA LA LONGITUD DEL ARRAY Y SI EST?? VAC??O MUESTRA UNA MENSAJE */
    fun checkEmptyArray(size: Int){
        Log.i("hola2", "${bagsArrayList.size}")
        if(size < 1) {
            bag_animation.setAnimation(R.raw.animation_empty_bag)
            bag_animation.repeatCount = Animation.INFINITE
            bag_animation.playAnimation()
            bag_no_items_box.visibility = View.VISIBLE
        } else {
            bag_no_items_box.visibility = View.GONE
        }
    }

    /** ELIMINA LOS ALIMENTOS MARCADOS COMO COMPLETADOS */
    private fun deleteBags() {
        /** UTILIZAMOS UN BATCH PARA MANDAR UNA ??NICA PETICI??N
         * A FIREBASE Y NO CARGAR LAS PETICIONES */
        var batch = FirebaseFirestore.getInstance().batch()
        deleteBags.forEach {
            var docRef = (activity as MainActivity).db.collection("bags").document(it.id!!)
            batch.delete(docRef)
            bagsArrayList.remove(it)
        }
        bagAdapter.notifyDataSetChanged()
        checkEmptyArray(bagsArrayList.size)
        Snackbar.make(view as View, R.string.items_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.close) { /** NOTHING */ }
            .setActionTextColor(resources.getColor(R.color.yummy_purple, null))
            .show()
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

                            if (bagsArrayList.indexOf(bag) == -1){
                                bagsArrayList.add(bag)
                                bagAdapter.notifyItemInserted(bagsArrayList.size)
                                checkEmptyArray(bagsArrayList.size)
                            }

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