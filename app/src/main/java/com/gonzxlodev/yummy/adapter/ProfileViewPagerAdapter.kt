package com.gonzxlodev.yummy.adapter

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gonzxlodev.yummy.main.profile.MyRecipesFragment
import com.gonzxlodev.yummy.main.profile.SavedRecipesFragment

class ProfileViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> { MyRecipesFragment() }
            1 -> { SavedRecipesFragment() }
            else -> { throw Resources.NotFoundException("Position not found") }
        }
    }

}