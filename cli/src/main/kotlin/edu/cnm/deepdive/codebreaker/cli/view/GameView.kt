package edu.cnm.deepdive.codebreaker.cli.view

import edu.cnm.deepdive.codebreaker.model.Game
import java.io.PrintStream
import java.util.ResourceBundle

private const val GAME_STATE_LABEL_KEY = "game_state_label"
private const val GUESS_PROMPT_KEY = "guess_prompt"
private const val INVALID_GUESS_MESSAGE_KEY = "invalid_guess_message"

private const val GUESSES_HEADER_KEY = "guesses_header"
private const val GUESS_NUM_HEADER_KEY = "guess_num_header"
private const val GUESS_TEXT_HEADER_KEY = "guess_text_header"
private const val EXACT_MATCHES_HEADER_KEY = "exact_matches_header"
private const val NEAR_MATCHES_HEADER_KEY = "near_matches_header"

private const val SUCCESS_MESSAGE_KEY = "success_message"

class GameView(
    private val output: PrintStream,
    private val bundle: ResourceBundle,
    private val guessView: GuessView,
) {

    private val gameStateLabel = bundle.getString(GAME_STATE_LABEL_KEY)
    private val guessPrompt = bundle.getString(GUESS_PROMPT_KEY)
    private val invalidGuessMessage = bundle.getString(INVALID_GUESS_MESSAGE_KEY)

    private val guessesHeader = bundle.getString(GUESSES_HEADER_KEY)
    private val guessNumHeader = bundle.getString(GUESS_NUM_HEADER_KEY)
    private val guessTextHeader = bundle.getString(GUESS_TEXT_HEADER_KEY)
    private val exactMatchesHeader = bundle.getString(EXACT_MATCHES_HEADER_KEY)
    private val nearMatchesHeader = bundle.getString(NEAR_MATCHES_HEADER_KEY)

    private val successMessage = bundle.getString(SUCCESS_MESSAGE_KEY)

    fun emitGameConfiguration(game: Game) {
        output.println(gameStateLabel.format(game.pool, game.length))
    }

    fun emitGuessPrompt() {
        output.print(guessPrompt)
    }

    fun emitInvalidGuessMessage(game: Game, text: String) {
        output.println(invalidGuessMessage.format(game.length, game.pool, text))
    }

    fun emitGuessTable(game: Game) {
        output.println(
            guessesHeader.format(
                guessNumHeader,
                guessTextHeader,
                exactMatchesHeader,
                nearMatchesHeader
            )
        )
        game
            .guesses
            .forEachIndexed { index, guess ->  guessView.emitGuessRow(guess, index)}
    }

    fun emitSuccessMessage(game: Game) {
        println(successMessage.format(game.code, game.guesses.size))
    }
}