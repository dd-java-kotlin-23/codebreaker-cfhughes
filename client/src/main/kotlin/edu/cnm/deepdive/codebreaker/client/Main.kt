package edu.cnm.deepdive.codebreaker.client

import edu.cnm.deepdive.codebreaker.client.dto.GameRequest
import edu.cnm.deepdive.codebreaker.client.service.CodebreakerService

fun main() {
    val response = CodebreakerService
        .instance
        .startGame(GameRequest("ABCDEF", 3))
        .get()
    println(response)
}