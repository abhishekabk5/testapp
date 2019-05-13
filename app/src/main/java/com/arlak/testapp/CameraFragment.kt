package com.arlak.testapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.arlak.testapp.databinding.FragmentCameraBinding
import com.camerakit.CameraKitView
import id.zelory.compressor.Compressor
import java.io.File


class CameraFragment : Fragment() {

    private lateinit var cameraKit: CameraKitView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentCameraBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_camera, container, false)

        cameraKit = binding.camera
        binding.buttonCapture.setOnClickListener {
            Log.i("buttonCapture", "buttonClicked")
            cameraKit.captureImage{_, capturedImage ->
                Log.i("buttonCapture", "callback")
                val imageFile = File.createTempFile("temp", "jpg")
                imageFile.writeBytes(capturedImage)
                Log.i("buttonCapture", "fileCreated")
                val compressedFile = Compressor(context).compressToFile(imageFile)
                imageFile.writeBytes(compressedFile.readBytes())
                Log.i("buttonCapture", "fileCompressed")
                findNavController().navigate(CameraFragmentDirections
                    .actionCameraFragmentToVisitorFragment(imageFile.absolutePath))
            }
        }

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
