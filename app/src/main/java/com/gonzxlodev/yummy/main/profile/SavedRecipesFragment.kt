package com.gonzxlodev.yummy.main.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentProfileBinding
import com.gonzxlodev.yummy.databinding.FragmentSavedRecipesBinding

class SavedRecipesFragment : Fragment() {

    /** variables */
    private var _binding: FragmentSavedRecipesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }
}