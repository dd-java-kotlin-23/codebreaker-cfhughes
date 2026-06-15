package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.model.Game;
import java.util.concurrent.CompletableFuture;

public interface CodebreakerService {

  // TODO: 6/15/26 Declare abstract methods to start game, get game, delete game, and submit guess.

  CompletableFuture<Game> startGame(String pool, int length); // TODO: 6/15/26 Define exception for invalid game configuration.

  CompletableFuture<Game> getGame(String id); // TODO: 6/15/26 Define exception for game not found.

  CompletableFuture<Void> deleteGame(String id);

  CompletableFuture<Game> submitGuess(Game game, String text); // TODO: 6/15/26 Define exception for invalid guess.

  void shutdown();

}
