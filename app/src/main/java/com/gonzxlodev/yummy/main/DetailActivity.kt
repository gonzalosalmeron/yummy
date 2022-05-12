package com.gonzxlodev.yummy.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gonzxlodev.yummy.databinding.ActivityDetailBinding
import com.gonzxlodev.yummy.uitel.loadImage

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intss = intent
        var nameT = intss.getStringExtra("NAMET")
        var imgT = intss.getStringExtra("IMGURI")

        binding.detailName.text = nameT
        binding.recipeDetailImageView.loadImage(imgT)
    }
}