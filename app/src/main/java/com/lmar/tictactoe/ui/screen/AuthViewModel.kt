package com.lmar.tictactoe.ui.screen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.USERS_REFERENCE
import com.lmar.tictactoe.core.state.UserState

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private var database: DatabaseReference = Firebase.database
        .getReference("$DATABASE_REFERENCE/$USERS_REFERENCE")

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if(auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("¡Correo y/o contraseña no pueden ser vacías!")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error al logearse")
                }
            }
            .addOnFailureListener { error ->
                _authState.value = error.message?.let { AuthState.Error(it) }
            }
    }

    fun signup(names: String, email: String, password: String, passwordRepeat: String) {
        if(names.isEmpty()) {
            _authState.value = AuthState.Error("¡Nombres no puede ser vacío!")
            return
        }

        if(email.isEmpty()) {
            _authState.value = AuthState.Error("¡Correo no puede ser vacío!")
            return
        }

        if(password.isEmpty()) {
            _authState.value = AuthState.Error("¡Contraseña no puede ser vacía!")
            return
        }

        if(password.length < 6) {
            _authState.value = AuthState.Error("¡Contraseña debe tener al menos 6 caracteres!")
            return
        }

        if(passwordRepeat.isEmpty() || password != passwordRepeat) {
            _authState.value = AuthState.Error("¡Las contraseñas no coinciden!")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    _authState.value = AuthState.Authenticated

                    //Registrar Usuario
                    val newUser = UserState()
                    newUser.id = task.result.user?.uid.toString()
                    newUser.names = names
                    newUser.email = email
                    createUser(newUser)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error al registrarse")
                }
            }
            .addOnFailureListener { error ->
                _authState.value = error.message?.let { AuthState.Error(it) }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    private fun createUser(user: UserState) {
        database.child(user.id)
            .setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Usuario registrado con éxito: ${user.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al registrar usuario ${user.id}", e)
            }
    }

}

sealed class AuthState {
    data object Authenticated: AuthState()
    data object Unauthenticated: AuthState()
    data object Loading: AuthState()
    data class Error(val message: String): AuthState()
}
