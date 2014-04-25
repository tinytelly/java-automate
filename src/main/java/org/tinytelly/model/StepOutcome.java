package org.tinytelly.model;

public class StepOutcome {
    private String name;
    private String result;
    private String error;

    public StepOutcome(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getName() {
        return name;
    }
}
