import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

class Highscore implements Comparable<Highscore> {

    public Highscore() {
    }

    public Highscore(String username, int score, int newIndex) {
        this.username = username;
        this.score = score;
        this.newIndex = newIndex;
    }

    public String getUsername() {
        return username;
    }

    private String username;
    private int score;

    @JsonProperty("newIndex")
    private int newIndex;

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return username + ": " + score;
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

    public static class Comparators {

        public static Comparator<Highscore> ASC = new Comparator<Highscore>() {
            @Override
            public int compare(Highscore o1, Highscore o2) {
                return o1.getScore() - o2.getScore();
            }
        };

        public static Comparator<Highscore> DESC = new Comparator<Highscore>() {
            @Override
            public int compare(Highscore o1, Highscore o2) {
                return o2.getScore() - o1.getScore();
            }
        };

    }

}