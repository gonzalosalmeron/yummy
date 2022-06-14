package com.gonzxlodev.yummy.main

import android.content.Context
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
    private val BagFragment = com.gonzxlodev.yummy.main.BagFragment()
    private val ProfileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** RECOGE LA BARRA DE NAVEGACIÓN */
        val myBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        /** ESTABLECE EL FRAGMENTO POR DEFECTO EN LA BARRA DE NAVEGACIÓN */
        replaceFragment(HomeFragment)

        /** DEFINE LAS ACCIONES EN LOS DISTINTOS ELEMENTOS DE LA NAVEGACIÓN */
        myBottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment)
                    item.title
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment)
                }
                R.id.nav_upload -> {
                    item.isCheckable = false
                    uploadActivity()
                }
                R.id.nav_bag -> {
                    replaceFragment(BagFragment)
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

    /** MÉTODO PÚBLICO ES ACCESIBLE A TODOS LOS FRAGMENTOS QUE PERTENEZCAN A ESTA ACTIVIDAD
     * EL CUAL PERMITE RECOGER LOS DATOS DEL USUARIO LOGUEADO DE LAS SHARED PREFERENCES OPTIMIZANDO
     * LAS LLAMADAS QUE HACEMOS A FIREBASE*/
    fun getEmail(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)

        return email.toString()
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