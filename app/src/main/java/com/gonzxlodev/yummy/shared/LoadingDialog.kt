package com.gonzxlodev.yummy.shared

import android.app.Activity
import android.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.gonzxlodev.yummy.R

class LoadingDialog(val mActivity:FragmentActivity) {
    private lateinit var isDialog:AlertDialog

    fun startLoading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }

    fun isDimiss(){
        isDialog.dismiss()
    }
}