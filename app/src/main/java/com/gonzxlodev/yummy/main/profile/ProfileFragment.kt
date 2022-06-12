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
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.adapter.ProfileViewPagerAdapter
import com.gonzxlodev.yummy.auth.AuthActivity
import com.gonzxlodev.yummy.auth.LoginActivity
import com.gonzxlodev.yummy.databinding.FragmentProfileBinding
import com.gonzxlodev.yummy.databinding.ProfileBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.*
import java.lang.reflect.Array.newInstance
import javax.xml.validation.SchemaFactory.newInstance

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    /** variables */
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

        auth = Firebase.auth

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

        setUserProfile()

        binding.profileLogoutBtn.setOnClickListener {
            val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)?.edit()
            prefs?.clear()
            prefs?.apply()

            goAuth()

            auth.signOut()
            activity?.finish()
        }

//        binding.profileOptionsBtn.setOnClickListener {
//            val bottomSheetFragment: BottomSheetDialogFragment = ProfileBottomSheetDialogBinding
//            bottomSheetFragment.show(requireFragmentManager(), bottomSheetFragment.tag)
//        }

    }

    private fun setUserProfile(){
        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)
        val name = prefs?.getString("name", null)
        val imgUrl = prefs?.getString("imgUrl", null)
        Log.i("imgUril", "${imgUrl}, ${email}, ${name}")
        binding.profileUserName.text = name
        Glide.with(context!!)
            .load(imgUrl)
            .skipMemoryCache(true)
            .into(binding.profileUserImage)
    }

    private fun goAuth() {
        val intent = Intent(context, AuthActivity::class.java)
        startActivity(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}