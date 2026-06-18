package edu.cnm.deepdive.codebreaker.cli.controller;

import edu.cnm.deepdive.codebreaker.cli.view.SessionView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

public class SessionController {

  private static final String STOP_PLAYING_RESPONSE_KEY = "stop_playing_response";

  private final GameController gameController;
  private final SessionView sessionView;
  private final BufferedReader buffer;
  private final char stopPlayingResponse;

  public SessionController(InputStream input, GameController gameController,
      SessionView sessionView, ResourceBundle bundle) {
    this.gameController = gameController;
    this.sessionView = sessionView;
    buffer = new BufferedReader(new InputStreamReader(input));
    stopPlayingResponse = bundle.getString(STOP_PLAYING_RESPONSE_KEY).charAt(0);
  }

  public void run() throws IOException {
    do {
      gameController.play();
    } while (playAgain());
  }

  private boolean playAgain() throws IOException {
    sessionView.emitPlayAgainPrompt();
    String userInput = buffer.readLine().strip().toUpperCase();
    return userInput.isEmpty() || userInput.charAt(0) != stopPlayingResponse;
  }

}
