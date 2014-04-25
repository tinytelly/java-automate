package org.tinytelly.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tinytelly.model.PayLoad;
import org.tinytelly.model.PropertyPair;
import org.tinytelly.service.LogService;
import org.tinytelly.service.PropertiesService;
import org.tinytelly.util.PropertyConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class Step {
    @Autowired
    public PropertiesService propertiesService;

    @Autowired
    public LogService logService;

    private PayloadStep payloadStep;
    private String callingStepName;

    /*
    The identifier is a way to pair a step in a plan with a property.
    In the Plan : runCommandLine trigproc where trigproc is the identifier
    In the Properties : runCommandLine.trigproc
     */
    public String identifier;

    List<String> payloadRunSteps = new ArrayList<String>();

    public PayLoad payLoad;

    public String EMPTY = PropertyConstants.EMPTY + PropertyConstants.NEW_LINE;

    protected String result = EMPTY;
    public String error = EMPTY;

    private boolean ignoreErrorsExplicitlySetInStep;//This allows us to not fail on errors...e.g. delete directory may fail but the usage in the plan may not care.

    public Step doNotFailOnNonFatalErrors() {
        ignoreErrorsExplicitlySetInStep = true;
        return this;
    }

    public Step addProperty(String propertyToOverride, String override) {
        propertiesService.setProperty(propertyToOverride, override);
        return this;
    }

    public Step addProperties(String propertyToOverride, List<String> overrides) {
        for (String override : overrides) {
            propertiesService.setProperty(propertyToOverride, override);
        }
        return this;
    }

    public Step addPayload(PayLoad payLoad) {
        this.payLoad = payLoad;
        return this;
    }

    /*
    This is just used for logging reasons.  It will show the calling Step name in the logs like this:
    *****************************************************************
    STEP: DeleteDirectoriesStep [called from JbossServerSetUpStep ]
    *****************************************************************
     */
    @Deprecated //User addCallingStep
    public Step addCallingStepName(Step step) {
        this.callingStepName = step.getClass().getSimpleName();
        return this;
    }

    public Step addCallingStep(Step step) {
        this.callingStepName = step.getClass().getSimpleName();
        addPayload(step.payLoad);
        return this;
    }


    public final String getResult() {
        return result;
    }

    public final String getError() {
        return error;
    }

    public boolean hasPayLoad() {
        return this.payLoad == null ? false : true;
    }

    @Deprecated //Use log instead
    public void addResult(String result) {
        if (this.result == null) {
            this.result = result;
        } else {
            this.result += result;
        }
        this.result += PropertyConstants.NEW_LINE;
    }

    public void log(String result) {
        if (this.result == null) {
            this.result = result;
        } else {
            this.result += result;
        }
        this.result += PropertyConstants.NEW_LINE;
    }

    public void doWork() throws Exception {
        boolean showRealTimeLogs = propertiesService.showRealTimeLogs();
        if (showRealTimeLogs) {
            result = EMPTY;
        }
        if (!ignoreErrorsExplicitlySetInStep && payLoad.hasErrors()) {
            throw new Exception(payLoad.getErrors());
        }
        try {
            boolean jsonProvidedForStep = false;
            String stepName = this.getClass().getSimpleName().toLowerCase();
            //Try to get a Payload from Json else doStep()
            List<PropertyPair> properties = propertiesService.getListOfPropertyPairsFromProperty(PayloadStep.JSON);
            if (properties.size() > 0) {
                for (PropertyPair property : properties) {
                    String payloadStepName = property.getRight().toLowerCase() + PropertyConstants.STEP.toLowerCase();
                    if (stepName.equals(payloadStepName)) {
                        ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
                        payloadStep = context.getBean(PayloadStep.class);
                        payloadStep.doWork(this);
                        payloadRunSteps.add(payloadStepName);
                        jsonProvidedForStep = true;
                        break;
                    }
                }
            }

            if (!jsonProvidedForStep && !payloadRunSteps.contains(stepName)) {
                doStep();
            }
        } catch (Exception e) {
            error += e.getMessage();
        }
        if (!error.equals(EMPTY)) {
            if (!ignoreErrorsExplicitlySetInStep) {
                payLoad.addError(this.getClass().getName(), error);
            } else {
                payLoad.addResult(this.getClass().getName(), error);
                error = EMPTY;
            }
        }

        ignoreErrorsExplicitlySetInStep = false;
        payLoad.addResult(this.getClass().getName(), result);

        if (showRealTimeLogs) {
            if (!EMPTY.equals(result) || !EMPTY.equals(error)) {
                String stepDisplayText = this.getClass().getSimpleName();
                if (callingStepName != null) {
                    stepDisplayText += " [called from " + callingStepName + " ]";
                }
                System.out.println(getStepDisplayText(stepDisplayText));
                if (!EMPTY.equals(error)) {
                    System.out.println(error);
                }
                System.out.println(result);
            }
        }
    }

    public abstract void doStep() throws Exception;

    public void doWork(PropertiesService propertiesService, LogService logService, PayLoad payLoad) throws Exception {
        this.propertiesService = propertiesService;
        this.logService = logService;
        this.payLoad = payLoad;
        doWork();
    }

    @Deprecated
    public void doWork(Step step) throws Exception {
        this.propertiesService = step.propertiesService;
        this.logService = step.logService;
        this.payLoad = step.payLoad;
        doWork();
    }

    public boolean containsError() {
        return this.error.equals(EMPTY) ? false : true;
    }

    public void setIgnoreErrorsExplicitlySetInStep(boolean ignoreErrorsExplicitlySetInStep) {
        this.ignoreErrorsExplicitlySetInStep = ignoreErrorsExplicitlySetInStep;
    }

    public static String getStepDisplayText(String stepName) {
        return PropertyConstants.SPACER +
                "\nSTEP: " + stepName + "\n" +
                PropertyConstants.SPACER;
    }

    protected String getProperty(String property) {
        if (identifier == null) {
            return propertiesService.getProperty(property);
        } else {
            return propertiesService.getProperty(property + PropertyConstants.DOT + identifier);
        }
    }

    protected List<PropertyPair> getListOfPropertyPairsFromProperty(String property) {
        if (identifier == null) {
            return propertiesService.getListOfPropertyPairsFromProperty(property);
        } else {
            return propertiesService.getListOfPropertyPairsFromProperty(property + PropertyConstants.DOT + identifier);
        }
    }

    protected List<String> getPropertyList(String property) {
        if (identifier == null) {
            return propertiesService.getPropertyList(property);
        } else {
            return propertiesService.getPropertyList(property + PropertyConstants.DOT + identifier);
        }
    }

    protected boolean isOn(String property) {
        if (identifier == null) {
            return propertiesService.isOn(property);
        } else {
            return propertiesService.isOn(property + PropertyConstants.DOT + identifier);
        }
    }
}
