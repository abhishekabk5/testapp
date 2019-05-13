//package com.arlak.testapp
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import androidx.navigation.fragment.findNavController
//import com.arlak.testapp.databinding.FragmentEnterDetailsBinding
//import kotlinx.android.synthetic.main.fragment_enter_details.*
//
//
//class EnterDetailsFragment : Fragment() {
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//
//        val binding: FragmentEnterDetailsBinding = DataBindingUtil
//            .inflate(inflater, R.layout.fragment_enter_details, container, false)
//
//        binding.buttonNext.setOnClickListener {
//            val phoneNumber =  binding.editPhoneNumber.text.toString()
//            findNavController().navigate(EnterDetailsFragmentDirections.actionEnterDetailsFragmentToVisitorFragment(
//                EnterDetailsFragmentArgs.fromBundle(arguments!!).imageFile, phoneNumber))
//        }
//
//        return binding.root
//    }
//}
