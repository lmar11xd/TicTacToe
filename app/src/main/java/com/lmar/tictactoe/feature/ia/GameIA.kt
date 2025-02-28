package com.lmar.tictactoe.feature.ia

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.MEMORY_REFERENCE
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

class GameIA(
    userId: String,
    private val difficulty: Difficulty
) {
    enum class Difficulty { EASY, MEDIUM, HARD }

    companion object {
        private const val TAG = "GameIA"

        private val WINPATTERNS = listOf(
            // Filas
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),

            // Columnas
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),

            //Diagonales
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0)),
        )

        private fun checkWin(board: MutableList<MutableList<String>>, player: String): Boolean {
            return WINPATTERNS.any { pattern -> pattern.all { board[it.first][it.second] == player } }
        }

        fun checkWinner(board: MutableList<MutableList<String>>): Int? {
            if(checkWin(board, PlayerTypeEnum.O.name)) return 1 //IA gana
            if(checkWin(board, PlayerTypeEnum.X.name)) return -1 //Jugador gana
            if(board.all { row -> row.all { it != "" } }) return 0 //Empate
            return null //juego en progreso
        }

        fun getWinCells(board: MutableList<MutableList<String>>, player: String): List<Pair<Int, Int>> {
            for (line in WINPATTERNS) {
                if (line.all { (row, col) -> board[row][col] == player }) {
                    return line
                }
            }
            return emptyList() // No hay línea ganadora
        }

    }

    private var database: DatabaseReference = Firebase.database
        .getReference("$DATABASE_REFERENCE/$MEMORY_REFERENCE")
        .child(userId)

    private val playerMoves = mutableMapOf<Pair<Int, Int>, Int>()

    init {
        loadPlayerMoves() // Cargar datos desde Firebase
    }

    fun getNextMove(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        return when (difficulty) {
            Difficulty.EASY -> getRandomMove(board)
            Difficulty.MEDIUM -> getSmartMove(board)
            Difficulty.HARD -> getAdaptiveMove(board)
        }
    }

    private fun getRandomMove(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        val availableMoves = mutableListOf<Pair<Int, Int>>()
        for (i in board.indices) {
            for (j in board[i].indices) {
                if(board[i][j] == "") availableMoves.add(Pair(i, j))
            }
        }
        return availableMoves.random()
    }

    private fun getSmartMove(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        // Verificar si la IA puede ganar en el siguiente movimiento
        findWinningMove(board, PlayerTypeEnum.O.name)?.let { return it }

        // Bloquear al jugador si está a punto de ganar
        findWinningMove(board, PlayerTypeEnum.X.name)?.let { return it }

        // Si no hay jugadas críticas, hacer un movimiento aleatorio
        return getRandomMove(board)
    }

    private fun findWinningMove(board: MutableList<MutableList<String>>, player: String): Pair<Int, Int>? {
        for(i in board.indices) {
            for(j in board.indices) {
                if (board[i][j] == "") {
                    // Simular la jugada
                    board[i][j] = player
                    if (checkWin(board, player)) {
                        // Deshacer la simulacion
                        board[i][j] = ""
                        return Pair(i, j)
                    }
                    board[i][j] = "" // Deshacer si no gana
                }
            }
        }
        return null
    }

    /*Sin mejora en apertura*/
    private fun getBestMoveNoOptimized(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int> = Pair(-1, -1)

        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] == "") {
                    board[i][j] = PlayerTypeEnum.O.name // Simula movimiento de la IA
                    //val score = minimax(board, false) // Evalúa jugada
                    val score = minimax(board, false, Int.MIN_VALUE, Int.MAX_VALUE) //Evalúa jugada con poda alfa-beta
                    board[i][j] = "" // Deshace simulación

                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(i, j)
                    }
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: MutableList<MutableList<String>>, isMaximizing: Boolean): Int {
        val winner = checkWinner(board)
        if(winner != null) return winner

        if(isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if(board[i][j] == "") {
                        board[i][j] = PlayerTypeEnum.O.name
                        bestScore = maxOf(bestScore, minimax(board, false))
                        board[i][j] = ""
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if(board[i][j] == "") {
                        board[i][j] = PlayerTypeEnum.X.name
                        bestScore = minOf(bestScore, minimax(board, true))
                        board[i][j] = ""
                    }
                }
            }
            return bestScore
        }
    }

    //Minimax con poda alfa-beta
    private fun minimax(board: MutableList<MutableList<String>>, isMaximizing: Boolean, alpha: Int, beta: Int): Int {
        val winner = checkWinner(board)
        if (winner != null) return winner

        var localAlpha = alpha
        var localBeta = beta

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if (board[i][j] == "") {
                        board[i][j] = PlayerTypeEnum.O.name
                        bestScore = maxOf(bestScore, minimax(board, false, localAlpha, localBeta))
                        board[i][j] = ""

                        localAlpha = maxOf(localAlpha, bestScore)
                        if (localBeta <= localAlpha) break // Poda
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if (board[i][j] == "") {
                        board[i][j] = PlayerTypeEnum.X.name
                        bestScore = minOf(bestScore, minimax(board, true, localAlpha, localBeta))
                        board[i][j] = ""

                        localBeta = minOf(localBeta, bestScore)
                        if (localBeta <= localAlpha) break // Poda
                    }
                }
            }
            return bestScore
        }
    }

    /*
    * Para hacer que la IA difícil tenga aperturas más naturales y variadas,
    * podemos mejorar su primera jugada para que no sea siempre la misma.
    * */
    private fun getBestMoveOptimized(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        val emptyCells = board.flatMapIndexed  { i, row ->
            row.mapIndexedNotNull { j, cell -> if (cell == "") Pair(i, j) else null }
        }

        // Si es la primera jugada de la IA, hacer una apertura variada
        if (emptyCells.size == 9) {
            val openings = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2), Pair(1, 1)) // Esquinas o centro
            return openings.random()
        }

        if (emptyCells.size == 8 && board[1][1] == "") {
            return Pair(1, 1) // Si el centro está libre, tomarlo
        }

        // Aplicar Minimax si no es la primera jugada
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int> = Pair(-1, -1)

        for ((i, j) in emptyCells) {
            board[i][j] = PlayerTypeEnum.O.name
            val score = minimax(board, false, Int.MIN_VALUE, Int.MAX_VALUE)
            board[i][j] = ""

            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(i, j)
            }
        }
        return bestMove
    }

    private fun getAdaptiveMove(board: MutableList<MutableList<String>>): Pair<Int, Int> {
        // Si hay suficiente información, ajusta el juego en base a movimientos previos del jugador
        if (playerMoves.isNotEmpty()) {
            val commonMove = playerMoves.maxByOrNull { it.value }?.key
            if (commonMove != null && board[commonMove.first][commonMove.second] == "") {
                return commonMove // Intentar jugar en el lugar más usado por el jugador
            }
        }
        // Si no hay datos suficientes, usar Minimax con poda alfa-beta
        return getBestMoveOptimized(board)
    }

    fun recordPlayerMove(row: Int, col: Int) {
        val key = "$row$col"
        val count = playerMoves.getOrDefault(Pair(row, col), 0) + 1
        playerMoves[Pair(row, col)] = count

        database.child(key).setValue(count) // Guardar en Firebase
    }

    private fun loadPlayerMoves() {
        Log.e(TAG, "Cargar Movimientos")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val key = child.key ?: return
                    val count = child.getValue(Int::class.java) ?: 0
                    if (key.length == 2) {
                        val row = key[0].digitToInt()
                        val col = key[1].digitToInt()
                        playerMoves[Pair(row, col)] = count
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar datos: ${error.message}")
            }
        })
    }

    fun resetMemory() {
        database.removeValue() // Borra los datos de Firebase
        playerMoves.clear()
    }

    fun getMemoryStats(): Map<Pair<Int, Int>, Int> {
        return playerMoves.toMap()
    }
}