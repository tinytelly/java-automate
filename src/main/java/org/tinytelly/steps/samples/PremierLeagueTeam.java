package org.tinytelly.steps.samples;

import com.google.gson.annotations.Expose;

/**
 * Sample pojo that represents a Team in the Premier League
 */
public class PremierLeagueTeam {
    @Expose//<-@Expose is used when generating json to be used in PayloadStep
    private String name;
    @Expose
    private Integer points;

    public PremierLeagueTeam(String name, Integer points) {
        this.name = name;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public Integer getPoints() {
        return points;
    }
}
