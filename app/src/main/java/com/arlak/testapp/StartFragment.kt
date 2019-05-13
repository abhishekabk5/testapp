package com.arlak.testapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.arlak.testapp.databinding.FragmentStartBinding


class StartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentStartBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_start, container, false)

        binding.buttonStart.setOnClickListener {
//            findNavController().navigate(StartFragmentDirections.actionStartFragmentToCameraFragment())
            findNavController().navigate(StartFragmentDirections.actionStartFragmentToVisitorFragment(
                "https://firebasestorage.googleapis.com/v0/b/testapp-rc.appspot.com/o/738278.jpg?alt=media&token=96b663ae-be32-4a17-ad1c-dd3c0d33df7c"))
        }

        return binding.root
    }
}
