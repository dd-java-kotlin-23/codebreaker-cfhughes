package edu.cnm.deepdive.codebreaker.client.service

import edu.cnm.deepdive.codebreaker.client.dto.GameRequest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CodebreakerServiceTest {

    private val service: CodebreakerService = CodebreakerService.instance

    @Test
    fun startGame() {
        val response = service
            .startGame(GameRequest("ABCDEF", 3))
            .get() // Wait for future to complete.
        assertAll(
            { assertEquals(3, response.length) },
            { assertEquals("ABCDEF", response.pool) },
            { assertFalse(response.solved) },
            { assertTrue(response.guesses.isEmpty()) },
        )
    }

    @Test
    fun getGame() {
    }

    @Test
    fun deleteGame() {
    }

    @Test
    fun submitGuess() {
    }

    @Test
    fun getGuess() {
    }

    @Test
    fun getInstance() {
    }

}