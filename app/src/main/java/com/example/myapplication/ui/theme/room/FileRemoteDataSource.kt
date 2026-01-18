package com.example.myapplication.ui.theme.room

import com.example.myapplication.ui.theme.models.FileStored
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FileRemoteDataSource {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun fileRef() =
        firestore.collection("users").document(auth.currentUser!!.uid).collection("files")


    suspend fun upload(file : FileStored){
        fileRef().document(file.syncId).set(file).await()
    }

    suspend fun fetchAll() : List<FileStored>{
        val snapshot = fileRef().get().await()
        return snapshot.toObjects(FileStored::class.java)
    }

}