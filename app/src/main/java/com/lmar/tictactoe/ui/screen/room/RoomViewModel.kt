package com.lmar.tictactoe.ui.screen.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.ROOMS_REFERENCE
import com.lmar.tictactoe.core.enums.RoomStatusEnum
import com.lmar.tictactoe.core.state.RoomState
import com.lmar.tictactoe.core.util.generateUniqueCode
import java.util.UUID

class RoomViewModel : ViewModel() {

    companion object {
        private const val TAG = "RoomViewModel"
    }

    private var database: DatabaseReference =
        Firebase.database.getReference("$DATABASE_REFERENCE/$ROOMS_REFERENCE")

    private val _roomState = MutableLiveData<RoomState?>()
    val roomState: LiveData<RoomState?> = _roomState

    fun createNewRoom(onResult: (roomId: String?) -> Unit) {
        val roomId = UUID.randomUUID().toString()
        val newRoom = RoomState()
        newRoom.roomId = roomId
        newRoom.roomCode = generateUniqueCode()

        Log.d(TAG, "Creando Sala: $roomId")

        database.child(roomId)
            .setValue(newRoom)
            .addOnSuccessListener {
                Log.d(TAG, "Sala creada con éxito: $roomId")
                _roomState.value = newRoom
                onResult(newRoom.roomId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear sala: $roomId", e)
                onResult(null)
            }
    }

    private fun searchRoom(roomCode: String, onResult: (DataSnapshot?) -> Unit) {
        database.orderByChild("roomCode").equalTo(roomCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Sala con codigo: $roomCode encontrada")
                        val roomSnapshot = snapshot.children.firstOrNull()
                        onResult(roomSnapshot)
                    } else {
                        Log.d(TAG, "Sala con codigo: $roomCode no encontrada")
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error de consulta: $error")
                    onResult(null)
                }

            })
    }

    fun searchRoomByCode(roomCode: String, onResult: (Boolean, String) -> Unit) {
        searchRoom(roomCode) { dataSnapshot ->
            if (dataSnapshot != null) {
                dataSnapshot.getValue(RoomState::class.java)?.let {
                    if(it.roomStatus == RoomStatusEnum.OPENED) {
                        _roomState.value = it

                        //Jugador 2 se ha unido
                        val updatedRoom = it.copy(
                            roomStatus = RoomStatusEnum.COMPLETED,
                            updatedAt = System.currentTimeMillis()
                        )

                        database.child(it.roomId).setValue(updatedRoom)

                        onResult(true, it.roomId)
                    } else {
                        onResult(false, "¡Sala llena!")
                    }
                }
            } else {
                onResult(false, "¡Sala no encontrada, intenta con otro código!")
            }
        }
    }
}