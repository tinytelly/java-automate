package org.tinytelly.steps.samples;

import org.springframework.stereotype.Component;
import org.tinytelly.steps.Step;

/**
 * Sample step that would get the premiership table from a website somewhere, but is faked here for simplicity.
 */
@Component
public class PremierLeagueTableFinderStep extends Step {
    @Override
    public void doStep() throws Exception {
        String tableName = getProperty("premier.league.table.name");//<--table.name is a property found in sample.properties

        PremierLeagueTable premierLeagueTable = new PremierLeagueTable(tableName);
        premierLeagueTable.add(new PremierLeagueTeam("Liverpool", 80));
        premierLeagueTable.add(new PremierLeagueTeam("Chelsea", 75));
        premierLeagueTable.add(new PremierLeagueTeam("Manchester City", 74));
        premierLeagueTable.add(new PremierLeagueTeam("Arsenal", 70));
        premierLeagueTable.add(new PremierLeagueTeam("Manchester United", 57));

        payLoad.append("footballResults", premierLeagueTable);//<--Here we are populating the payload that can be used in future steps
    }
}
