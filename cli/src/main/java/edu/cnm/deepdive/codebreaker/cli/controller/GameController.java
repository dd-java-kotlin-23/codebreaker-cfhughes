package edu.cnm.deepdive.codebreaker.cli.controller;

import edu.cnm.deepdive.codebreaker.cli.view.GameView;
import edu.cnm.deepdive.codebreaker.cli.viewmodel.CodebreakerViewModel;
import edu.cnm.deepdive.codebreaker.exception.InvalidGuessException;
import edu.cnm.deepdive.codebreaker.model.Game;
import java.io.InputStream;
import java.util.Properties;

public class GameController {

  private static final String POOL_KEY = "pool";
  private static final String LENGTH_KEY = "length";

  private final InputStream input;
  private final GameView gameView;
  private final CodebreakerViewModel viewModel;
  private final String pool;
  private final int length;

  public GameController(InputStream input, GameView gameView, CodebreakerViewModel viewModel,
      Properties gameProperties) {
    this.input = input;
    this.gameView = gameView;
    this.viewModel = viewModel;
    pool = gameProperties.getProperty(POOL_KEY);
    length = Integer.parseInt(gameProperties.getProperty(LENGTH_KEY));
  }

  public boolean play() {
    viewModel.observeGame(this::handleGame);
    viewModel.observeError(this::handleError);
    viewModel.startGame(pool, length);
  }

  private void handleError(Throwable throwable) {
    if (throwable instanceof InvalidGuessException badGuessException) {
      gameView.emitInvalidGuessMessage(badGuessException.getGame(), badGuessException.getGuess());
    }
  }

  private void handleGame(Game game) {
    if (game != null) {
      if (game.guesses().isEmpty()) {
        gameView.emitGameConfiguration(game);
      } else {
        gameView.emitGuessTable(game);
        gameView.emitGameConfiguration(game);
        if (game.isSolved()) {
          gameView.emitSuccessMessage(game);
        } else {
          // TODO: 6/17/26 Get the next guess from the user.
        }
      }
    }
  }

}
