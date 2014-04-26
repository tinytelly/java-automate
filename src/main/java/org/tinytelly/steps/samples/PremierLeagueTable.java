package org.tinytelly.steps.samples;

import com.google.gson.annotations.Expose;

import java.util.*;

/**
 * Sample Pojo to hold current table of a Football League
 */
public class PremierLeagueTable {
    @Expose//<-@Expose is used when generating json to be used in PayloadStep
    private List<PremierLeagueTeam> currentTable = new ArrayList<PremierLeagueTeam>();
    @Expose
    private String name;

    public PremierLeagueTable(String name) {
        this.name = name;
    }

    public void add(PremierLeagueTeam premierLeagueTeam) {
        currentTable.add(premierLeagueTeam);
    }

    public List<PremierLeagueTeam> getCurrentTable() {
        return currentTable;
    }

    public String getName() {
        return name;
    }
}
