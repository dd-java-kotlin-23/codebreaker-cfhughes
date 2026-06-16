package edu.cnm.deepdive.codebreaker.cli.viewmodel;

import edu.cnm.deepdive.codebreaker.model.Game;
import edu.cnm.deepdive.codebreaker.service.CodebreakerService;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class CodebreakerViewModel {

  private final CodebreakerService service;
  private final List<Consumer<Game>> gameObservers;
  private final List<Consumer<Throwable>> errorObservers;

  private Game game;
  private Throwable error;

  public CodebreakerViewModel() {
    service = CodebreakerService.getInstance();
    gameObservers = new LinkedList<>();
    errorObservers = new LinkedList<>();
  }

  public void observeGame(Consumer<Game> observer) {
    gameObservers.add(observer);
    if (game != null) {
      observer.accept(game);
    }
  }

  public void observeError(Consumer<Throwable> observer) {
    errorObservers.add(observer);
    if (error != null) {
      observer.accept(error);
    }
  }

  public void startGame(String pool, int length) {
    handleError(null);
    service
        .startGame(pool, length)
        .thenAccept(this::handleGame)
        .exceptionally(this::handleError);
  }

  public void submitGuess(String text) {
    handleError(null);
    service
        .submitGuess(game, text)
        .thenAccept(this::handleGame)
        .exceptionally(this::handleError);
  }

  @Nullable
  private Void handleError(Throwable throwable) {
    error = throwable;
    notifyErrorObservers();
    return null;
  }

  private void handleGame(Game game) {
    this.game = game;
    notifyGameObservers();
  }

  private void notifyGameObservers() {
    gameObservers.forEach((observer) -> observer.accept(game));
  }

  private void notifyErrorObservers() {
    errorObservers.forEach((observer) -> observer.accept(error));
  }

}
