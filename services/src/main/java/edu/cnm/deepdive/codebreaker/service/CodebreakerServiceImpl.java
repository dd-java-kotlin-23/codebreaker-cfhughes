package edu.cnm.deepdive.codebreaker.service;

import edu.cnm.deepdive.codebreaker.client.dto.GameRequest;
import edu.cnm.deepdive.codebreaker.client.dto.GameResponse;
import edu.cnm.deepdive.codebreaker.client.dto.GuessResponse;
import edu.cnm.deepdive.codebreaker.client.service.CodebreakerProxy;
import edu.cnm.deepdive.codebreaker.model.Game;
import edu.cnm.deepdive.codebreaker.model.Guess;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

class CodebreakerServiceImpl implements CodebreakerService {

  private static final int MIN_LENGTH = 1;
  private static final int MAX_LENGTH = 20;

  private final CodebreakerProxy proxy = CodebreakerProxy.getInstance(); // FIXME: 6/15/26 Use dependency injection.

  @Override
  public CompletableFuture<Game> startGame(String pool, int length) {
    validateGameConfiguration(pool, length);
    GameRequest request = new GameRequest(pool, length);
    return proxy.startGame(request)
        .thenApply(new Function<GameResponse, Game>() {
          @Override
          public Game apply(GameResponse response) {
            List<Guess> guesses = response
                .getGuesses()
                .stream()
                .map(new Function<GuessResponse, Guess>() {
                  @Override
                  public Guess apply(GuessResponse response) {
                    return new Guess(response.getText(), response.getExactMatches(),
                        response.getNearMatches(), response.getSolution(), response.getCreated());
                  }
                })
                .collect(Collectors.toList());
            return new Game(response.getId(), response.getPool(), response.getLength(), guesses, response.getCreated());
          }
        });
  }


  @Override
  public CompletableFuture<Game> getGame(String id) {
    return null;
  }

  @Override
  public CompletableFuture<Void> deleteGame(String id) {
    return null;
  }

  @Override
  public CompletableFuture<Game> submitGuess(Game game, String text) {
    return null;
  }

  @Override
  public void shutdown() {
    // TODO: 6/15/26 Invoke as-yet-unimoplemented shutdown method in proxy.
  }

  private static void validateGameConfiguration(String pool, int length) {
    boolean isValidPool = pool
        .codePoints()
        .distinct()
        .noneMatch(new IntPredicate() {
          @Override
          public boolean test(int codePoint) {
            return Character.isWhitespace(codePoint)
                || Character.isISOControl(codePoint)
                || !Character.isDefined(codePoint);
          }
        });
    boolean isValidLength = (length >= MIN_LENGTH && length <= MAX_LENGTH);
    if (!isValidPool || !isValidLength) {
      throw new IllegalArgumentException(); // TODO: 6/15/26 Throw InvalidGameConfigurationException
    }
  }

}
