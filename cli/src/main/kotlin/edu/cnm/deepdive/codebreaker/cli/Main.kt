package edu.cnm.deepdive.codebreaker.cli

import java.util.Properties

private const val GAME_PROPERTIES_FILE = "game.properties"

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        this.javaClass
            .classLoader
            .getResourceAsStream(GAME_PROPERTIES_FILE)
            .use {
                val props = Properties()
                props.load(it)
                println(props)
                // TODO: Create a session controller, passing it the game properties.
            }
    }

}