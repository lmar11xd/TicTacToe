package com.lmar.tictactoe.ui.screen.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.STORAGE_REFERENCE
import com.lmar.tictactoe.core.Constants.USERS_REFERENCE
import com.lmar.tictactoe.core.state.UserState

class ProfileViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private var database: DatabaseReference = Firebase.database
        .getReference("$DATABASE_REFERENCE/$USERS_REFERENCE")

    private val _userState = MutableLiveData<UserState>()
    val userState: MutableLiveData<UserState> = _userState

    private val _profileImageUri = MutableLiveData<Uri?>(null)
    val profileImageUri: MutableLiveData<Uri?> = _profileImageUri

    private val _showForm = MutableLiveData(false)
    val showForm: LiveData<Boolean> = _showForm

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        auth.currentUser?.let {
            getUserById(it.uid)
        }
    }

    private fun getUserById(userId: String) {
        _isLoading.value = true
        database.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.getValue(UserState::class.java)?.let {
                            _isLoading.value = false
                            Log.d(TAG,"Usuario con ID ${it.id} encontrado")
                            _userState.value = it
                            listenForUpdates(it.id)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e(TAG, "Error al obtener usuario: ${error.message}")
                }
            })
    }

    private fun listenForUpdates(userId: String) {
        database.child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserState::class.java)?.let {
                        _userState.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error: ${error.message}")
                }
            })
    }

    fun setProfileImage(uri: Uri?) {
        profileImageUri.value = uri
    }

    private fun uploadProfileImage(userId: String, onSuccess: (Boolean, String?) -> Unit) {
        if (profileImageUri.value == null) onSuccess(true, null)

        val storageRef = FirebaseStorage.getInstance().reference.child("$STORAGE_REFERENCE/$userId.jpg")
        storageRef.putFile(profileImageUri.value!!)
            .addOnSuccessListener {

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    profileImageUri.value = null
                    val downloadUrl = uri.toString()
                    Log.d(TAG, "Imagen de perfil guardada con éxito: $downloadUrl")

                    // Llamar al callback con la URL
                    onSuccess(true, downloadUrl)
                }.addOnFailureListener {
                    Log.e(TAG, "Error al obtener URL de descarga", it)
                    onSuccess(false, null)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al guardar imagen de perfil", it)
                onSuccess(false, null)
            }
    }

    fun changeName(value: String) {
        if(value.isEmpty()) return
        val currentUser = _userState.value ?: return
        _userState.value = currentUser.copy(
            names = value
        )
    }

    fun showForm() {
        _showForm.value = true
    }

    fun dismissForm() {
        _showForm.value = false
    }

    fun saveForm() {
        val updatedUser = _userState.value ?: return
        updatedUser.updatedAt = System.currentTimeMillis()

        _isLoading.value = true
        uploadProfileImage(updatedUser.id) { isSuccess, imageUrl ->
            if(isSuccess) {
                if(imageUrl != null) {
                    updatedUser.imageUrl = imageUrl
                }

                _userState.value?.let {
                    database.child(it.id).setValue(updatedUser)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            dismissForm()
                        }
                        .addOnFailureListener { error ->
                            _isLoading.value = false
                            Log.e(TAG, "Error al actualizar usuario", error)
                            dismissForm()
                        }
                }
            } else {
                _isLoading.value = false
            }
        }

    }
}