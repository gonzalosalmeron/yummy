package com.gonzxlodev.yummy.main

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.ActivityMainBinding
import com.gonzxlodev.yummy.main.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val HomeFragment = com.gonzxlodev.yummy.main.HomeFragment()
    private val SearchFragment = com.gonzxlodev.yummy.main.SearchFragment()
    private val BagFragment = com.gonzxlodev.yummy.main.BagFragment()
    private val ProfileFragment = ProfileFragment()
    private var lastItem: Int = 0
    lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        db = FirebaseFirestore.getInstance()

        /** RECOGE LA BARRA DE NAVEGACIÓN */
        val myBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        /** ESTABLECE EL FRAGMENTO POR DEFECTO EN LA BARRA DE NAVEGACIÓN */
        replaceFragment(HomeFragment)

        /** DEFINE LAS ACCIONES EN LOS DISTINTOS ELEMENTOS DE LA NAVEGACIÓN */
        myBottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment)
                    lastItem = 0
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment)
                    lastItem = 1
                }
                R.id.nav_upload -> {
                    item.isCheckable = false
                    uploadActivity()
                }
                R.id.nav_bag -> {
                    replaceFragment(BagFragment)
                    lastItem = 3
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment)
                    lastItem = 4
                }
                else -> Log.i("hola", "hola")
            }

            true
        }


    }

    /** MÉTODO PÚBLICO ES ACCESIBLE A TODOS LOS FRAGMENTOS QUE PERTENEZCAN A ESTA ACTIVIDAD
     * EL CUAL PERMITE RECOGER LOS DATOS DEL USUARIO LOGUEADO DE LAS SHARED PREFERENCES OPTIMIZANDO
     * LAS LLAMADAS QUE HACEMOS A FIREBASE*/
    fun getEmail(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)

        return email.toString()
    }

    override fun onResume() {
        super.onResume()

        val myBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        myBottomNavigationView.menu.getItem(lastItem).isChecked = true
    }

    /** ESTE MÉTODO REEMPLAZA EL FRAGMENTO ACTUAL POR EL NUEVO INDICADO */
    internal fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_container, fragment)
        transaction.commit()
    }

    private fun uploadActivity() {
        val intent = Intent(this, UploadActivity::class.java)
        startActivity(intent)
    }

}