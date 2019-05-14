package com.arlak.testapp.cameraFragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import java.io.File

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private var _filePath = ""
    val filePath: String
        get() = _filePath

    private val _navigateToNextFragment = MutableLiveData<Boolean>()
    val navigateToNextFragment: LiveData<Boolean>
        get() = _navigateToNextFragment

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun saveAndCompressImage(image: ByteArray) {
        Log.i("buttonCapture", "callback")
        uiScope.launch {
            val imageFile = saveImageToFile(image)
            Log.i("buttonCapture", "fileCreated: ${imageFile.absolutePath}")
            val compressedFile = compressToFile(imageFile)
            Log.i("buttonCapture", "fileCompressed: ${compressedFile.absolutePath}")
            imageFile.writeBytes(compressedFile.readBytes())
            compressedFile.deleteOnExit()

            _filePath = imageFile.absolutePath
            _navigateToNextFragment.value = true
        }
    }

    private suspend fun saveImageToFile(image: ByteArray) : File {
        return withContext(Dispatchers.IO) {
            val imageFile = File.createTempFile("temp", ".jpg")
            imageFile.writeBytes(image)
            return@withContext imageFile
        }
    }

    private suspend fun compressToFile(file: File) : File {
        return withContext(Dispatchers.IO) {
            val compressedFile = Compressor(getApplication()).compressToFile(file)
            compressedFile
        }
    }

    fun doneNavigating() {
        _navigateToNextFragment.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}