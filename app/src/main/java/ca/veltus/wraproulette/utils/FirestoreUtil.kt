package ca.veltus.wraproulette.utils

import ca.veltus.wraproulette.data.objects.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.qualifier.named

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocReference: DocumentReference
    get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().uid ?: throw NullPointerException("UID is null.")}")

    fun initCurrentUserIfFirstTime(department: String, onComplete: () -> Unit) {
        currentUserDocReference.get().addOnSuccessListener {
            if (!it.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                     department,
                null,
                null)
                currentUserDocReference.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else {
                onComplete()
            }
        }

    }
    fun updateCurrentUser(uid: String = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                          displayName: String = "",
                          email: String = FirebaseAuth.getInstance().currentUser?.email ?: "",
                          department: String = "",
                          profilePicturePath: String? = null,
                          pools: MutableMap<String, Any>? = null) {

        val userFieldMap = mutableMapOf<String, Any>()

        if (displayName.isNotBlank()) userFieldMap["name"] = displayName
        if (department.isNotBlank()) userFieldMap["department"] = department
        if (profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath
        if (pools != null) userFieldMap["pools"] = pools
        currentUserDocReference.update(userFieldMap)


    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocReference.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

}