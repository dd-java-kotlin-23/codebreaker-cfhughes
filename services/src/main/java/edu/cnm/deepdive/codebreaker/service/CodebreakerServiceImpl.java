package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.client.dto.GameRequest;
import edu.cnm.deepdive.codebreaker.client.dto.GameResponse;
import edu.cnm.deepdive.codebreaker.client.dto.GuessRequest;
import edu.cnm.deepdive.codebreaker.client.dto.GuessResponse;
import edu.cnm.deepdive.codebreaker.client.service.CodebreakerProxy;
import edu.cnm.deepdive.codebreaker.exception.InvalidGameConfigurationException;
import edu.cnm.deepdive.codebreaker.exception.InvalidGuessException;
import edu.cnm.deepdive.codebreaker.model.Game;
import edu.cnm.deepdive.codebreaker.model.Guess;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class CodebreakerServiceImpl implements CodebreakerService {

  private static final int MIN_LENGTH = 1;
  private static final int MAX_LENGTH = 20;

  private final CodebreakerProxy proxy; // FIXME: 6/15/26 Use dependency injection.

  CodebreakerServiceImpl() {
    proxy = CodebreakerProxy.getInstance();
  }

  @Override
  public CompletableFuture<Game> startGame(String pool, int length) {
    return isGameConfigurationValid(pool, length)
        ? proxy.startGame(new GameRequest(pool, length))
        .thenApply(CodebreakerServiceImpl::buildGame)
        : CompletableFuture.failedFuture(new InvalidGameConfigurationException());
  }

  @Override
  public CompletableFuture<Game> getGame(String id) {
    return proxy.getGame(id).thenApply(CodebreakerServiceImpl::buildGame);
  }

  @Override
  public CompletableFuture<Void> deleteGame(String id) {
    return proxy.deleteGame(id);
  }

  @Override
  public CompletableFuture<Game> submitGuess(Game game, String text) {
    return isGuessValid(game, text)
        ? proxy.submitGuess(game.id(), new GuessRequest(text))
        .thenApply((response) -> {
          game.guesses().add(buildGuess(response));
          return game;
        })
        : CompletableFuture.failedFuture(new InvalidGuessException());
  }

  @Override
  public void shutdown() {
    proxy.shutdown();
  }

  private static boolean isGameConfigurationValid(String pool, int length) {
    return length >= MIN_LENGTH
        && length <= MAX_LENGTH
        && pool
        .codePoints()
        .distinct()
        .noneMatch(codePoint ->
            Character.isWhitespace(codePoint)
                || Character.isISOControl(codePoint)
                || !Character.isDefined(codePoint));
  }

  private static Game buildGame(GameResponse response) {
    List<Guess> guesses = response
        .getGuesses()
        .stream()
        .map(CodebreakerServiceImpl::buildGuess)
        .collect(Collectors.toCollection(LinkedList::new));
    return new Game(response.getId(), response.getPool(), response.getLength(), guesses,
        response.getCreated());
  }

  private static Guess buildGuess(GuessResponse response) {
    return new Guess(response.getText(), response.getExactMatches(),
        response.getNearMatches(), response.getSolution(), response.getCreated());
  }

  private static boolean isGuessValid(Game game, String text) {
    boolean valid = (text.codePoints().count() == game.length());
    if (valid) {
      Set<Integer> poolSet = game
          .pool()
          .codePoints()
          .boxed()
          .collect(Collectors.toSet());
      valid = text
          .codePoints()
          .allMatch(poolSet::contains);
    }
    return valid;
  }

}
