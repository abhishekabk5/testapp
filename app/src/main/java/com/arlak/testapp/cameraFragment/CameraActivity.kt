package com.arlak.testapp.cameraFragment

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.arlak.testapp.R
import com.arlak.testapp.databinding.ActivityCameraBinding
import com.wonderkiln.camerakit.*

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraKit: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityCameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        val viewModelFactory = CameraViewModelFactory(application)
        val cameraViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(CameraViewModel::class.java)

        cameraKit = binding.camera

        binding.buttonFacing.setOnClickListener {
            cameraKit.toggleFacing()
//            Log.i("buttonFacing", "clicked. now: ${cameraKit.facing}")
        }

        binding.buttonFlash.setOnClickListener {
//            Log.i("buttonFlash", "clicked.")
            when (cameraKit.flash) {
                CameraKit.Constants.FLASH_AUTO -> {
                    cameraKit.flash = CameraKit.Constants.FLASH_ON
                    binding.buttonFlash.setImageResource(R.drawable.ic_flash_on)
//                    Log.i("buttonFlash", "On")
                }
                CameraKit.Constants.FLASH_ON -> {
                    cameraKit.flash = CameraKit.Constants.FLASH_OFF
                    binding.buttonFlash.setImageResource(R.drawable.ic_flash_off)
//                    Log.i("buttonFlash", "Off")
                }
                else -> {
                    cameraKit.flash = CameraKit.Constants.FLASH_AUTO
                    binding.buttonFlash.setImageResource(R.drawable.ic_flash_auto)
//                    Log.i("buttonFlash", "Auto")
                }
            }
        }

        binding.buttonCapture.setOnClickListener {
//            Log.i("buttonCapture", "buttonClicked")
            cameraViewModel.onCaptureClicked()
        }

        cameraViewModel.captureClicked.observe(this, Observer { clicked ->
            if(clicked) {
//                Log.i("camera", "preview: ${cameraKit.previewSize}, capture: ${cameraKit.captureSize}")
                cameraKit.captureImage()
            }
        })

        cameraKit.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(video: CameraKitVideo?) {
                Log.i("camera", "onVideo")
            }

            override fun onEvent(event: CameraKitEvent?) {
                Log.i("camera", "onEvent: $event")
            }

            override fun onImage(capturedImage: CameraKitImage?) {
                Log.i("camera", "onImage")
                capturedImage?.let { image ->
                    cameraViewModel.saveAndCompressImage(image.jpeg)
                }
            }

            override fun onError(e: CameraKitError?) {
                Log.i("camera", "onError: ${e?.exception}")
            }
        })

        cameraViewModel.navigateToNextFragment.observe(this, Observer { navigate ->
            if(navigate) {
//                Log.i("CameraFragment", "Navigate to next.")
                val resultIntent = Intent()
                resultIntent.putExtra("imagePath", cameraViewModel.filePath)
                setResult(Activity.RESULT_OK, resultIntent)

                cameraViewModel.doneNavigating()
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cameraKit.start()
//        Log.i("Fragment", "onResume.")
    }

    override fun onPause() {
        cameraKit.stop()
        super.onPause()
//        Log.i("Fragment", "onPause.")
    }
}
