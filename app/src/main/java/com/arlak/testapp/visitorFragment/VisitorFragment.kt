package com.arlak.testapp.visitorFragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.arlak.testapp.R
import com.arlak.testapp.databinding.FragmentVisitorBinding
import com.google.android.material.snackbar.Snackbar


class VisitorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentVisitorBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_visitor, container, false)

        val visitorImagePath = VisitorFragmentArgs.fromBundle(arguments!!).imageFilePath

        val viewModelFactory = VisitorViewModelFactory(visitorImagePath)
        val visitorViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(VisitorViewModel::class.java)

        binding.imageVisitor.setImageDrawable(Drawable.createFromPath(visitorImagePath))

        visitorViewModel.showSnackbar.observe(this, Observer {show ->
            visitorViewModel.run {
                if(show) {
                    val snackbar = Snackbar.make(binding.rootScrollView, snackbarMsg
                        , Snackbar.LENGTH_LONG)
                    if(snackActionText != "") {
                        snackbar.setAction(snackActionText, View.OnClickListener { snackbarAction })
                    }
                    snackbar.show()
                    doneShowingSnackbar()
                }
            }
        })

        binding.buttonNext.setOnClickListener {
            val phoneNumber = binding.editPhoneNumber.text.toString().trim()
            visitorViewModel.onNext(phoneNumber)
        }

        visitorViewModel.authenticationStarted.observe(this, Observer {started ->
            if(started) {
                binding.editOTP.visibility = View.VISIBLE
                binding.buttonCheck.visibility = View.VISIBLE
            }
        })

        binding.buttonCheck.setOnClickListener {
            val otp = binding.editOTP.text.toString().trim()
            visitorViewModel.onCheck(otp)
            binding.editOTP.visibility = View.GONE
            binding.buttonCheck.visibility = View.GONE
        }

        visitorViewModel.navigateToStart.observe(this, Observer { navigate ->
            if(navigate) {
                findNavController().navigate(VisitorFragmentDirections.actionVisitorFragmentToStartFragment())
                visitorViewModel.doneNavigating()
            }
        })

        return binding.root
    }

}