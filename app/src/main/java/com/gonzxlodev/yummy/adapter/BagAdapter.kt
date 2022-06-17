package com.gonzxlodev.yummy.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.BagCardBinding
import com.gonzxlodev.yummy.model.Bag
import com.google.firebase.firestore.FirebaseFirestore

class BagAdapter(private val bagList: ArrayList<Bag>, private val context: Context):
    RecyclerView.Adapter<BagAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bag_card, parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val bag = bagList[position]

        viewHolder.checkBox.isChecked = bag.completed!!
        viewHolder.checkBox.text = bag.name

        /** COMPRUEBA SI ESTÁ COMPLETADA O NO EL ALIMENTO DE LA BOLSA */
        if (bag.completed!!) {
            viewHolder.checkBox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            viewHolder.checkBox.paintFlags = 0
        }

        /** ESTÁ AL TANTO DE CUANDO HACEMOS CLICK PARA CAMBIAR SU ESTADO */
        viewHolder.checkBox.setOnClickListener { view ->
            if (bag.completed!!) {
                bag.completed = false
                viewHolder.checkBox.paintFlags = 0

            } else {
                bag.completed = true
                viewHolder.checkBox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }


    override fun getItemCount(): Int {
        return bagList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var binding = BagCardBinding.bind(view)
        val checkBox: CheckBox = binding.bagCardCkeckbox
    }

}