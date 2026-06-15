package edu.cnm.deepdive.codebreaker.model;

import java.time.ZonedDateTime;

public record Guess(Game game, String text, int exactMatches, int nearMatches, boolean solution,
                    ZonedDateTime submitted) {

}
