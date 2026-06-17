package edu.cnm.deepdive.codebreaker.cli.view

import edu.cnm.deepdive.codebreaker.model.Guess
import java.io.PrintStream
import java.util.ResourceBundle

private const val GUESS_ROW_KEY = "guess_row"

class GuessView(private val output: PrintStream, bundle: ResourceBundle) {

    private val guessRow = bundle.getString(GUESS_ROW_KEY)

    fun emitGuessRow(guess: Guess, position: Int) {
        output.println(
            guessRow.format(
                position + 1,
                guess.text,
                guess.exactMatches,
                guess.nearMatches
            )
        )
    }

}