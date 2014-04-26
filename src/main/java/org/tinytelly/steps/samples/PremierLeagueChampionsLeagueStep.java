package org.tinytelly.steps.samples;

import org.springframework.stereotype.Component;
import org.tinytelly.steps.Step;

/**
 * Sample step that just prints out the teams that are going to the play in the champions league next season based on current standing.
 */
@Component
public class PremierLeagueChampionsLeagueStep extends Step {
    private static final int NUMBER_OF_TEAMS_THAT_MAKE_IT = 4;

    @Override
    public void doStep() throws Exception {
        PremierLeagueTable premierLeagueTable = (PremierLeagueTable) payLoad.getMostRecentLoad();//<-The payload comes from a previous step or is provided as json using PayloadStep

        System.out.println("Champions League teams from the " + premierLeagueTable.getName() + " are currently:");
        int count = 0;
        for (PremierLeagueTeam premierLeagueTeam : premierLeagueTable.getCurrentTable()) {
            System.out.println(premierLeagueTeam.getName() + " on " + premierLeagueTeam.getPoints() + " points");
            count++;
            if (count >= NUMBER_OF_TEAMS_THAT_MAKE_IT) {
                break;
            }
        }
    }
}
