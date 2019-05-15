package com.arlak.testapp.visitorFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VisitorViewModelFactory(private val imageFilePath: String) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitorViewModel::class.java)) {
            return VisitorViewModel(imageFilePath) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}