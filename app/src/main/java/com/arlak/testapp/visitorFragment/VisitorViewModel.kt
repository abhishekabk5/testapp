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

    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)

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
    private lateinit var photoDownloadUrl: Uri

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
        visitorsRef.child("Vis-$phoneNumber").setValue(visitor)
    }

    fun onNext(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        checkPhoneNumber()
    }

    private fun checkPhoneNumber() {
        val phoneQuery = visitorsRef.orderByChild("phoneNumber").equalTo(phoneNumber)
        phoneQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    if(singleSnapshot.exists()) {
                        val visitor = singleSnapshot.getValue(Visitor::class.java)
                        visitor?.let {
                            visitorsRef.child("Vis-$phoneNumber").child("visitCount")
                                .setValue(visitor.visitCount + 1)
                        }
                    }
                    else {
                        authenticateNewVisitor()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun authenticateNewVisitor() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 30, TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD, callbacks)
        _authenticationStarted.value = true

        uploadCompressedPhoto()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(TaskExecutors.MAIN_THREAD, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    addVisitor()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            })
    }

    private fun uploadCompressedPhoto() {
        var file = Uri.fromFile(File(compressedImageFilePath))

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
                    // Handle Errors.
                }
            }
    }
}