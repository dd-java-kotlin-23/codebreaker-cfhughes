package edu.cnm.deepdive.codebreaker.model;

import java.time.OffsetDateTime;
import java.util.List;

// TODO: 6/15/26 Add guesses parameter after defining the Guess record.
public record Game(String id, String pool, int length, List<Guess> guesses,
                   OffsetDateTime created) {

  public boolean solved() {
    //noinspection Convert2MethodRef // TODO Explore use of method reference.
    return guesses
        .stream()
        .anyMatch((guess) -> guess.solution());
  }

}
