package com.arlak.testapp.visitorFragment

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import java.io.File
import java.util.concurrent.TimeUnit


class VisitorViewModel(private val compressedImageFilePath: String) : ViewModel() {

    private val _showSnackbar = MutableLiveData<Boolean>()
    val showSnackbar: LiveData<Boolean>
        get() = _showSnackbar

    private var _snackbarMsg: String = ""
    val snackbarMsg: String
        get() = _snackbarMsg

    private var _snackbarActionText = ""
    val snackActionText: String
        get() = _snackbarActionText

    private var _snackbarAction: (() -> Unit)? = null
    val snackbarAction: (() -> Unit)?
        get() = _snackbarAction

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.i("Authentication", "Verification completed.")
            _snackbarMsg = "Verification Completed"
            _showSnackbar.value = true

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.i("Authentication", "Verification failed: $e")
            _snackbarMsg = "Verification Failed: $e"
            _showSnackbar.value = true

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
        }

        override fun onCodeSent(
            verificationId: String?,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.i("Authentication", "Code Sent.")
            Log.d(TAG, "onCodeSent:" + verificationId!!)

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            // ...
        }
    }

    private val _authenticationStarted = MutableLiveData<Boolean>()
    val authenticationStarted: LiveData<Boolean>
        get() = _authenticationStarted

    private var database = FirebaseDatabase.getInstance().reference
    private var visitorsRef: DatabaseReference
    private var suspiciousUsersRef: DatabaseReference

    private lateinit var phoneNumber: String
//    private lateinit var photoDownloadUrl: Uri

    private var auth = FirebaseAuth.getInstance()

    private var storageRef = FirebaseStorage.getInstance().reference.child("images")

    init {
        visitorsRef = database.child("visitors")
        suspiciousUsersRef = database.child("suspicious_users")
    }

    fun onCheck(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun addVisitor() {
        val visitor = Visitor(phoneNumber, photoDownloadUrl)
        visitorsRef.push().setValue(visitor)

        _snackbarMsg = "New Visitor Saved!"
        _showSnackbar.value = true
    }

    private fun addSuspiciousUser() {
        val visitor = Visitor(phoneNumber, photoDownloadUrl)
        suspiciousUsersRef.push().setValue(visitor)
    }

    fun onNext(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        checkPhoneNumber()
    }

    private fun checkPhoneNumber() {
        val phoneQuery = visitorsRef.orderByChild("phoneNumber").equalTo(phoneNumber)
        phoneQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("checkPhoneNumber", "onDataChanged called, dataSnapshot exists: ${dataSnapshot.exists()}")

                if(dataSnapshot.exists()) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val key = singleSnapshot.key
                        val visitor = singleSnapshot.getValue(Visitor::class.java)
                        visitor?.let {
                            incrementVisitCount(key!!, visitor.visitCount)
                        }
                    }
                }
                else {
                    authenticateNewVisitor()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun incrementVisitCount(visitorKey: String, visitCount: Int) {
        val visRef = visitorsRef.child(visitorKey)
        val newCount = visitCount + 1
        visRef.child("visitCount").setValue(newCount)

        _snackbarMsg = "welcome back for $newCount time"
        _showSnackbar.value = true
    }

    private fun authenticateNewVisitor() {
        Log.i("New User", "Authentication started.")
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91$phoneNumber", 30, TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD, callbacks)
        _authenticationStarted.value = true

        uploadCompressedPhoto()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.i("New User", "signIn started.")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(TaskExecutors.MAIN_THREAD, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    addVisitor()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        addSuspiciousUser()
                    }
                }
            })
    }

    private fun uploadCompressedPhoto() {
        val file = Uri.fromFile(File(compressedImageFilePath))

        val uploadFileRef = storageRef.child(file.lastPathSegment!!)
        val metadata = StorageMetadata.Builder().setContentType("image/jpg").build()

        val urlTask = uploadFileRef.putFile(file, metadata)
            .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation uploadFileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    photoDownloadUrl = task.result!!
                } else {
                    _snackbarMsg = "Upload failed."
                    _snackbarActionText = "Try Again?"
                    _snackbarAction = ::uploadCompressedPhoto
                    _showSnackbar.value = true
                }
            }
    }

    fun doneShowingSnackbar() {
        _showSnackbar.value = false
        _snackbarMsg = ""
        _snackbarActionText = ""
        _snackbarAction = null
    }
}