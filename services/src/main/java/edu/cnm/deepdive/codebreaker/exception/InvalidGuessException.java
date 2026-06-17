package edu.cnm.deepdive.codebreaker.exception;

import edu.cnm.deepdive.codebreaker.model.Game;

public class InvalidGuessException extends IllegalArgumentException {

  private final Game game;
  private final String guess;

  public InvalidGuessException(Game game, String guess) {
    this.game = game;
    this.guess = guess;
  }

  public InvalidGuessException(String message, Game game, String guess) {
    super(message);
    this.game = game;
    this.guess = guess;
  }

  public InvalidGuessException(String message, Throwable cause, Game game, String guess) {
    super(message, cause);
    this.game = game;
    this.guess = guess;
  }

  public InvalidGuessException(Throwable cause, Game game, String guess) {
    super(cause);
    this.game = game;
    this.guess = guess;
  }

  public Game getGame() {
    return game;
  }

  public String getGuess() {
    return guess;
  }

}
