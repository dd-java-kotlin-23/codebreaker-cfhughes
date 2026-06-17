package edu.cnm.deepdive.codebreaker.cli

import edu.cnm.deepdive.codebreaker.cli.view.GameView
import edu.cnm.deepdive.codebreaker.cli.view.GuessView
import edu.cnm.deepdive.codebreaker.cli.view.SessionView
import java.util.Properties
import java.util.ResourceBundle

private const val GAME_PROPERTIES_FILE = "game.properties"
private const val BASE_BUNDLE_NAME = "ui-string"

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        this.javaClass
            .classLoader
            .getResourceAsStream(GAME_PROPERTIES_FILE)
            .use {
                val props = Properties()
                props.load(it)
                val bundle = ResourceBundle.getBundle(BASE_BUNDLE_NAME)
                val sessionView = SessionView(System.out, bundle)
                val guessView = GuessView(System.out, bundle)
                val gameView = GameView(System.out, bundle, guessView)
                // TODO: Create a session controller, passing it the game properties.
            }
    }

}