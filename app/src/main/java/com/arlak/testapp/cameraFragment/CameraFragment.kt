package com.arlak.testapp.cameraFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.arlak.testapp.R
import com.arlak.testapp.databinding.FragmentCameraBinding
import com.camerakit.CameraKitView


class CameraFragment : Fragment() {

    private lateinit var cameraKit: CameraKitView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentCameraBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_camera, container, false)

        val application = requireNotNull(activity).application
        val viewModelFactory = CameraViewModelFactory(application)
        val cameraViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(CameraViewModel::class.java)

        cameraKit = binding.camera
        binding.buttonCapture.setOnClickListener {
            Log.i("buttonCapture", "buttonClicked")
            cameraKit.captureImage{_, capturedImage ->
                cameraViewModel.saveAndCompressImage(capturedImage)
            }
        }

        cameraViewModel.navigateToNextFragment.observe(this, Observer { navigate ->
            if(navigate) {
                findNavController().navigate(CameraFragmentDirections
                    .actionCameraFragmentToVisitorFragment(cameraViewModel.filePath))
                cameraViewModel.doneNavigating()
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        cameraKit.onStart()
    }

    override fun onStop() {
        super.onStop()
        cameraKit.onStop()
    }

    override fun onPause() {
        super.onPause()
        cameraKit.onPause()
    }

    override fun onResume() {
        super.onResume()
        cameraKit.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKit.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
