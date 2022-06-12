package com.gonzxlodev.yummy.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityMainBinding
import com.gonzxlodev.yummy.main.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val HomeFragment = com.gonzxlodev.yummy.main.HomeFragment()
    private val SearchFragment = com.gonzxlodev.yummy.main.SearchFragment()
    private val UploadFragment = com.gonzxlodev.yummy.main.UploadFragment()
    private val ProfileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** GETS THE NAVIGATION MENU */
        val myBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        /** DEFAULT FRAGMENT */
        replaceFragment(HomeFragment)

        myBottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment)
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment)
                }
                R.id.nav_upload -> {
                    item.isCheckable = false
                    uploadActivity()
                }
                R.id.nav_bag -> {
                    replaceFragment(UploadFragment)
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment)
                }
                else -> Log.i("hola", "hola")
            }

            true
        }


    }

//    override fun onBackPressed() {
//        // Simply Do noting!
//    }

    /** THIS METHOD REPLACES THE CURRENT FRAGMENT */
    internal fun replaceFragment(fragment: Fragment) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_container, fragment)
            transaction.commit()
        }
    }

    private fun uploadActivity() {
        val intent = Intent(this, UploadActivity::class.java)
        startActivity(intent)
    }

}