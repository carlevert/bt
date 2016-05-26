package net.carlevert.Scoreboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

/**
 * Created by carlevert on 4/14/16.
 */

@Cache
@Entity
public class Highscore implements Comparable<Highscore> {

    @JsonProperty("username")
    @Id
    private String username;

    @Index
    private int score;

    @Parent
    private Key<String> parent;

    public int newIndex;

    public Highscore() {
        score = 0;
        parent = Key.create(String.class, "Highscores");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("score")
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Highscore anotherHighscore) {
        if (this.getScore() == anotherHighscore.getScore())
            return 0;
        else if (this.getScore() < anotherHighscore.getScore())
            return 1;
        else
            return -1;
    }

    @Override
    public String toString() {
        return username + ": " + score;
    }

}
