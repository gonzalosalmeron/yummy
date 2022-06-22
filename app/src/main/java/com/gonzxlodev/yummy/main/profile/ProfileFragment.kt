package com.gonzxlodev.yummy.main.profile

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.MyRecipesAdapter
import com.gonzxlodev.yummy.adapter.ProfileViewPagerAdapter
import com.gonzxlodev.yummy.auth.AuthActivity
import com.gonzxlodev.yummy.auth.LoginActivity
import com.gonzxlodev.yummy.databinding.FragmentProfileBinding
import com.gonzxlodev.yummy.databinding.ProfileBottomSheetDialogBinding
import com.gonzxlodev.yummy.main.MainActivity
import com.gonzxlodev.yummy.main.UploadActivity
import com.gonzxlodev.yummy.model.Recipe
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.*
import java.lang.reflect.Array.newInstance
import javax.xml.validation.SchemaFactory.newInstance

class ProfileFragment : Fragment() {

    /** VARIABLES */
    private lateinit var auth: FirebaseAuth
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var recipesArrayList: ArrayList<Recipe>
    private lateinit var listAdapter: MyRecipesAdapter

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        /** INICIALIZAMOS FIREBASE AUTH */
        auth = Firebase.auth

        /** INICIALIZAMOS EL ARRAY Y EL ADAPTADOR */
        recipesArrayList = arrayListOf()
        val profileFragment = com.gonzxlodev.yummy.main.profile.ProfileFragment()
        listAdapter = MyRecipesAdapter(recipesArrayList, activity as Context, profileFragment)

        /** INICIALIZA EL TABLAYOUT */
        tabLayout = binding.profileTabLayout
        viewPager = binding.profileViewPager
        viewPager.adapter = ProfileViewPagerAdapter(activity as FragmentActivity)
        TabLayoutMediator(tabLayout, viewPager) { tab, index ->
            tab.text = when(index) {
                0 -> { getString(R.string.my_recipes) }
                1 -> { getString(R.string.saved) }
                else -> { throw Resources.NotFoundException("Position not found") }
            }
        }.attach()

        /** LLAMADA DEL MÉTODO SETUSERPROFILE */
        setUserProfile()

        binding.profileOptionsBtn.setOnClickListener {
            /** LLAMA AL MENÚ CONTEXTUAL */
            val showPopUp = PopupMenu(context, it)
            showPopUp.inflate(R.menu.profile_menu)
            showPopUp.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    /** SI DAMOS CLICK EN LA OPCIÓN DE CERRAR CIERRA LA SESIÓN
                     * Y ELIMINA LOS DATOS DEL USUARIO DE SHARED PREFERENCES */
                    R.id.profile_popup_logout -> {
                        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)?.edit()
                        prefs?.clear()
                        prefs?.apply()

                        goAuth()

                        auth.signOut()
                        activity?.finish()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            showPopUp.show()
        }

        eventChangeListener()
    }



    /** PONE LA IMAGEN Y EL NOMBRE DE PERFIL DEL USUARIO ACTUALMENTE LOGUEADO */
    private fun setUserProfile(){
        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)
        val name = prefs?.getString("name", null)
        val imgUrl = prefs?.getString("imgUrl", null)

        binding.profileUserName.text = name
        Glide.with(context!!)
            .load(imgUrl)
            .skipMemoryCache(true)
            .into(binding.profileUserImage)
    }

    /** SI HACEMOS LOGOUT NOS LLEVA DE VUELTA A LA ACTIVIDAD DE AUTH */
    private fun goAuth() {
        val intent = Intent(context, AuthActivity::class.java)
        startActivity(intent)
    }

    /** LLAMADA A FIREBASE PARA CARGAR LAS RECETAS DEL USUARIO ACTUALMENTE LOGUEADO */
    private fun eventChangeListener() {
        (activity as MainActivity).db.collection("recipes").orderBy("created_at", Query.Direction.DESCENDING)
            .whereEqualTo("user_email", (activity as MainActivity).getEmail())
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.i("Firestore error", error.message.toString())
                        return
                    }
                    for (dc : DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED){
                            var recipe = dc.document.toObject(Recipe::class.java)
                            recipe.id = dc.document.id

                            /** COMPRUEBA SI LA LA RECETA EXISTE EN EL ARRAY */
                            if (recipesArrayList.indexOf(recipe) == -1) {
                                recipesArrayList.add(0, recipe)
                                binding.profileUserRecipes.text = recipesArrayList.size.toString()
                            }
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            var deletedRecipe = dc.document.toObject(Recipe::class.java)
                            deletedRecipe.id = dc.document.id

                            if (deletedRecipe.id != null) {
                                if (recipesArrayList.indexOf(deletedRecipe) != -1) {
                                    recipesArrayList.removeAt(recipesArrayList.indexOf(deletedRecipe))
                                    checkEmptyArray(recipesArrayList.size)
                                }
                            }
                        }
                    }

                }
            })

    }

    fun checkEmptyArray(size: Int){
        if (size >= 0) {
            binding.profileUserRecipes.text = recipesArrayList.size.toString()
        } else {
            binding.profileUserRecipes.text = "0"
        }
    }

}