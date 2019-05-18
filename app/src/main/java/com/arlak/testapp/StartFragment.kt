package com.arlak.testapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.arlak.testapp.databinding.FragmentStartBinding
import com.google.android.material.snackbar.Snackbar


class StartFragment : Fragment() {

    private val cameraIntentCode = 1
    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_start, container, false)

        binding.buttonStart.setOnClickListener {
            val cameraIntent = Intent()
            cameraIntent.action = "com.arlak.testapp.action.cameraAction"
            startActivityForResult(cameraIntent, cameraIntentCode)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == cameraIntentCode) {
            if(resultCode == Activity.RESULT_OK) {
                data?.extras?.getString("imagePath")?.let { imagePath ->
                    findNavController().navigate(StartFragmentDirections
                        .actionStartFragmentToVisitorFragment(imagePath))
                }
            } else {
                Snackbar.make(binding.root.rootView, "Failed to get image. Start Again.", Snackbar.LENGTH_LONG)
                    .show()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }
}
