package org.tinytelly.model;

import org.tinytelly.util.PropertyConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayLoad<T extends Object> {
    private Map<String, T> Load = new HashMap<String, T>();
    private T mostRecentLoad;

    private List<StepOutcome> stepOutcomes = new ArrayList<StepOutcome>();

    public PayLoad() {
    }

    public PayLoad(String key, T load) {
        Load.put(key, load);
    }

    public Map<String, T> getLoad() {
        return Load;
    }

    public T getLoad(String key) {
        if (!Load.containsKey(key)) {
            return null;
        }
        return Load.get(key);
    }

    public boolean isLoaded() {
        return this.Load == null ? false : true;
    }

    public void append(String key, T load) {
        Load.put(key, load);
        mostRecentLoad = load;
    }

    public void addError(String key, String error) {
        StepOutcome stepOutcome = new StepOutcome(key);
        stepOutcome.setError(error);
        this.stepOutcomes.add(stepOutcome);
    }

    public void addResult(String key, String result) {
        StepOutcome stepOutcome = new StepOutcome(key);
        stepOutcome.setResult(result);
        this.stepOutcomes.add(stepOutcome);
    }

    public boolean hasErrors() {
        for (StepOutcome stepOutcome : this.stepOutcomes) {
            if (stepOutcome.getError() != null) {
                return true;
            }
        }
        return false;
    }

    public String getErrors() {
        StringBuilder errors = new StringBuilder();
        for (StepOutcome stepOutcome : this.stepOutcomes) {
            if (stepOutcome.getError() != null) {
                errors.append(getStepDisplayText(stepOutcome.getName()) + "\n" + stepOutcome.getError());
            }
        }
        return errors.toString();
    }

    public String getResults() {
        StringBuilder results = new StringBuilder();
        for (StepOutcome stepOutcome : this.stepOutcomes) {
            if (stepOutcome.getResult() != null) {
                results.append(getStepDisplayText(stepOutcome.getName()) + stepOutcome.getResult());
            }
        }
        return results.toString();
    }

    public T getMostRecentLoad() {
        return mostRecentLoad;
    }

    private String getStepDisplayText(String stepName) {
        return "\n" +
                PropertyConstants.SPACER +
                "\nSTEP: " + stepName + "\n" +
                PropertyConstants.SPACER;
    }

    public void reset() {
        stepOutcomes = new ArrayList<StepOutcome>();
    }
}
