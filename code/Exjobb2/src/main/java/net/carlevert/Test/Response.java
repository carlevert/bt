package net.carlevert.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

    public int rankEstimate;

    public int realRank;

    public int diff;

    @JsonProperty("diff")
    public int getDiff() {
        return Math.abs(realRank - rankEstimate);
    }

    public long time;

    @Override
    public String toString() {
        return "rankEstimate: " + rankEstimate + ", realRank: " + realRank + ", diff: " + getDiff() + ", time: " + time;
    }

}