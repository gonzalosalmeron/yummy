package com.gonzxlodev.yummy.main.profile

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import com.gonzxlodev.yummy.R
import com.gonzxlodev.yummy.databinding.FragmentSavedRecipesBinding
import kotlinx.android.synthetic.main.fragment_my_recipes.*

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

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        binding.savedRecipesAnimation.setAnimation(R.raw.animation_development)
        binding.savedRecipesAnimation.repeatCount = Animation.INFINITE
        binding.savedRecipesAnimation.playAnimation()

        binding.savedRecipesAnimation.setOnClickListener {
            launchSound()
        }
    }

    fun launchSound(){
        val mediaPlayer = MediaPlayer.create(activity as Context, R.raw.shake_sound)
        mediaPlayer.start()
    }
}