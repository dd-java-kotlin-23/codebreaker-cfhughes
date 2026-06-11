package edu.cnm.deepdive.codebreaker.client.service

import edu.cnm.deepdive.codebreaker.client.dto.GameRequest
import edu.cnm.deepdive.codebreaker.client.dto.GameResponse
import edu.cnm.deepdive.codebreaker.client.dto.GuessRequest
import edu.cnm.deepdive.codebreaker.client.dto.GuessResponse
import java.util.concurrent.CompletableFuture

interface CodebreakerService {

    fun startGame(game: GameRequest): CompletableFuture<GameResponse>

    fun getGame(gameId: String): CompletableFuture<GameResponse>

    fun deleteGame(gameId: String): CompletableFuture<Void?>

    fun submitGuess(gameId: String, guess: GuessRequest): CompletableFuture<GuessResponse>

    fun getGuess(gameId: String, guessId: String): CompletableFuture<GuessResponse>

    // TODO: Add shutdown function.

    companion object {

        @JvmStatic
        val instance: CodebreakerService
            get() = CodebreakerServiceImpl

    }
}