package com.lmar.tictactoe.ui.screen.profile

import android.util.Log
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
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.ROOMS_REFERENCE
import com.lmar.tictactoe.core.Constants.USERS_REFERENCE
import com.lmar.tictactoe.core.entity.User
import com.lmar.tictactoe.core.state.RoomState
import com.lmar.tictactoe.ui.screen.game.GameViewModel
import com.lmar.tictactoe.ui.screen.game.GameViewModel.Companion

class ProfileViewModel(): ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private var database: DatabaseReference = Firebase.database
        .getReference("$DATABASE_REFERENCE/$USERS_REFERENCE")


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user


    init {
        auth.currentUser?.let {
            getUserById(it.uid)
        }
    }

    private fun getUserById(userId: String) {
        database.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.getValue(User::class.java)?.let {
                            _user.value = it
                            Log.d(TAG,"Usuario con ID ${it.id} encontrada")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al obtener usuario: ${error.message}")
                }
            })
    }
}