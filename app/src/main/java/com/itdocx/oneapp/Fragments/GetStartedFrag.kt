package com.itdocx.oneapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.itdocx.oneapp.DashboardActivity
import com.itdocx.oneapp.MainActivity
import com.itdocx.oneapp.R
import com.itdocx.oneapp.databinding.FragmentGetStartedBinding


class GetStartedFrag : Fragment() {


    private var _binding: FragmentGetStartedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGetStartedBinding.inflate(inflater, container, false)


        Glide.with(this).asGif().load(R.drawable.ic_getstarted_header)
            .into(binding.ivGetstartedsticker)


        binding.btnGetstarted.setOnClickListener {

            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            isonBoardingFinished()
            activity?.finish()
        }


        return binding.root
    }


    private fun isonBoardingFinished() {

        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("finished", true)
        editor.apply()
    }


}