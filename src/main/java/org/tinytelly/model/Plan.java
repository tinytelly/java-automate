package org.tinytelly.model;

import org.tinytelly.util.PropertyConstants;

import java.util.List;

public class Plan {
    private String name;
    private List<String> steps;

    public Plan(String name, List<String> steps) {
        this.name = name;
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "name='" + name + '\'' +
                ", steps=" + steps +
                '}';
    }

    public boolean isValid() {
        if (this.getSteps() == null) {
            return false;
        } else {
            return true;
        }
    }

    public void displayHeader() {
        System.out.println("Running Plan : " + name);
        System.out.println("Which contains the following Steps");
        for (String step : this.getSteps()) {
            System.out.println("Step : " + step);
        }
        System.out.println(PropertyConstants.NEW_LINE);
    }
}
