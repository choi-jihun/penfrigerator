package com.example.fridge.src

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(uri: Uri, path: String): String {
        val storageRef = storage.reference.child(path)
        val uploadTask = storageRef.putFile(uri).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        return downloadUrl.toString()
    }
}