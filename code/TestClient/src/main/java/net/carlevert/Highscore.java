package net.carlevert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by carlevert on 4/19/16.
 */
public class Highscore {

    @JsonProperty("username")
    String username;

    @JsonProperty("score")
    int score;

}
