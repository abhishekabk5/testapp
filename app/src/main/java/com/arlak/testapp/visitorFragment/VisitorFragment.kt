package com.arlak.testapp.visitorFragment

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.arlak.testapp.R
import com.arlak.testapp.databinding.FragmentVisitorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ValueEventListener
import java.io.File


class VisitorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentVisitorBinding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_visitor, container, false)

        val viewModelFactory = VisitorViewModelFactory(VisitorFragmentArgs.fromBundle(arguments!!).imageFilePath)
        val visitorViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(VisitorViewModel::class.java)
//        val visitorViewModel = ViewModelProviders.of(this).get(VisitorViewModel::class.java)

        binding.buttonNext.setOnClickListener {
            val phoneNumber = binding.editPhoneNumber.text.toString()
            visitorViewModel.onNext(phoneNumber)
        }

        visitorViewModel.authenticationStarted.observe(this, Observer {started ->
            if(started) {
                binding.editOTP.visibility = View.VISIBLE
                binding.buttonCheck.visibility = View.VISIBLE
            }
        })

        binding.buttonCheck.setOnClickListener {
            val otp = binding.editOTP.text.toString()
            visitorViewModel.onCheck(otp)
        }

        return binding.root
    }

}