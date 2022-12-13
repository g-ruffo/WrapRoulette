package ca.veltus.wraproulette.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

object FirebaseStorageUtil {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserReference: StorageReference
        get() = storageInstance.reference.child(
            FirebaseAuth.getInstance().uid ?: throw NullPointerException("UID is null.")
        )

    fun uploadProfilePhoto(imageBytes: ByteArray, onSuccess: (imagePath: String) -> Unit) {
        val ref =
            currentUserReference.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }.addOnFailureListener {
            Firebase.crashlytics.recordException(it)
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}