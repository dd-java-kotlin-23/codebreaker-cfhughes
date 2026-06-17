package edu.cnm.deepdive.codebreaker.cli.controller;

import edu.cnm.deepdive.codebreaker.cli.view.GameView;
import edu.cnm.deepdive.codebreaker.cli.viewmodel.CodebreakerViewModel;
import edu.cnm.deepdive.codebreaker.exception.InvalidGuessException;
import edu.cnm.deepdive.codebreaker.model.Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class GameController {

  private static final String POOL_KEY = "pool";
  private static final String LENGTH_KEY = "length";

  private final GameView gameView;
  private final CodebreakerViewModel viewModel;
  private final String pool;
  private final int length;
  private final BufferedReader buffer;
  private CompletableFuture<Boolean> run;

  public GameController(InputStream input, GameView gameView, CodebreakerViewModel viewModel,
      Properties gameProperties) {
    this.gameView = gameView;
    this.viewModel = viewModel;
    pool = gameProperties.getProperty(POOL_KEY);
    length = Integer.parseInt(gameProperties.getProperty(LENGTH_KEY));
    buffer = new BufferedReader(new InputStreamReader(input));
  }

  public boolean play() {
    viewModel.observeGame(this::handleGame);
    viewModel.observeError(this::handleError);
    viewModel.startGame(pool, length);
    run = new CompletableFuture<>();
    return run.join();
  }

  private void handleError(Throwable throwable) {
    if (throwable instanceof InvalidGuessException badGuessException) {
      gameView.emitInvalidGuessMessage(badGuessException.getGame(), badGuessException.getGuess());
    }
  }

  private void handleGame(Game game) {
    if (game != null) {
      if (!game.guesses().isEmpty()) {
        gameView.emitGuessTable(game);
      }
      gameView.emitGameConfiguration(game);
      if (game.isSolved()) {
        gameView.emitSuccessMessage(game);
        viewModel.shutdown();
        run.complete(true);
      } else {
        handleUserInput();
      }
    }
  }

  private void handleUserInput() {
    try {
      gameView.emitGuessPrompt();
      String userInput = buffer.readLine().strip().toUpperCase();
      if (userInput.charAt(0) == 'X') { // FIXME: 6/17/26 Take this eXit character from the bundle.
        viewModel.shutdown();
        run.complete(false);
      } else {
        viewModel.submitGuess(userInput.strip().toUpperCase());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
