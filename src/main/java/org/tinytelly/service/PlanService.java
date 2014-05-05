package org.tinytelly.service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.tinytelly.model.PayLoad;
import org.tinytelly.model.Plan;
import org.tinytelly.steps.Step;
import org.tinytelly.util.PlanConstants;
import org.tinytelly.util.PropertyConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanService {
    @Autowired
    private LogService logService;

    @Autowired
    private PropertiesService propertiesService;

    private String stepIdentifier;

    public PayLoad execute(Plan plan) {
        PayLoad payLoad = new PayLoad();

        //Set up the plan
        if (!plan.isValid()) {
            logService.log("The plan file in not valid and contains no steps....investigate that");
            return payLoad;
        }

        plan.displayHeader();

        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

        boolean showRealTimeLogs = propertiesService.showRealTimeLogs();

        for (String step : plan.getSteps()) {
            step = parseStepFromPlan(step);

            try {
                try {
                    String stepInTestMode = propertiesService.getProperty(step + PlanConstants.PLAN_TEST_MODE);
                    if (stepInTestMode == null || !stepInTestMode.equals(PropertyConstants.TRUE)) {
                        Step worker = (Step) context.getBean(step + PropertyConstants.STEP);
                        worker.propertiesService = propertiesService;
                        worker.logService = logService;
                        worker.identifier = stepIdentifier;

                        if (payLoad.isLoaded()) {
                            worker.payLoad = payLoad;//Set the payload from the previous step if it has one
                        }
                        worker.doWork();
                        if (worker.payLoad.hasErrors()) {
                            logService.log("Exiting due to this error." + worker.payLoad.getErrors());
                            break;
                        }
                        if (worker.hasPayLoad()) {
                            payLoad = worker.payLoad;
                        }

                        String log = worker.logService.getLogAndResetLog();
                        String key = getStepName(worker.getClass().getSimpleName());
                        if (showRealTimeLogs && !PropertyConstants.EMPTY.equals(log)) {
                            System.out.println(key + " : " + log);
                        }
                        worker.payLoad.addResult(key, log);
                    } else {
                        System.out.println(step + " is in test mode so doWork is skipped");
                    }
                } catch (NoSuchBeanDefinitionException nsbe) {
                    payLoad.addResult("Missing Step", "Step " + step + " is not supported");
                }
            } catch (Exception e) {
                payLoad.addError(step, ExceptionUtils.getStackTrace(e));
            }
        }
        showRealTimeLogs = propertiesService.showRealTimeLogs();
        if (!showRealTimeLogs) {
            System.out.println((payLoad.getResults()));
        }

        if (!payLoad.hasErrors()) {
            System.out.println(getPlanDisplayText(plan.getName() + PlanConstants.PLAN_SUCCESSFUL_RUN));
        } else {
            System.out.println(payLoad.getErrors());
            System.out.println(getPlanDisplayText(plan.getName() + PlanConstants.PLAN_NOT_SUCCESSFULLY_RUN));
        }
        return payLoad;
    }

    public Plan createPlanFromFile(String planFileName) {
        Plan plan = new Plan(planFileName, null);
        List<String> steps = new ArrayList<String>();
        if (planFileName != null) {
            List<String> lines = null;
            try {
                lines = FileUtils.readLines(new File(planFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String line : lines) {
                if (line != null && !line.equals(PropertyConstants.EMPTY) && !line.trim().startsWith("#")) {
                    steps.add(line.trim());
                }
            }
        }
        if (steps.size() > 0) {
            plan.setSteps(steps);
        }

        return plan;
    }

    public static String getStepName(String className) {
        if (className == null) {
            return null;
        }
        return className.substring(0, className.indexOf(PropertyConstants.STEP));
    }

    private String getPlanDisplayText(String plan) {
        return PropertyConstants.NEW_LINE +
                PropertyConstants.SPACER +
                PropertyConstants.NEW_LINE +
                "Plan: " +
                plan +
                PropertyConstants.NEW_LINE +
                PropertyConstants.SPACER;
    }

    public String parseStepFromPlan(String stepFromPlan) {
        stepIdentifier = null;
        String step = stepFromPlan;
        if (CharMatcher.WHITESPACE.matchesAnyOf(stepFromPlan)) {
            String[] results = Iterables.toArray(Splitter
                    .on(CharMatcher.WHITESPACE)
                    .omitEmptyStrings()
                    .trimResults()
                    .split(stepFromPlan), String.class);

            if (results.length == 2) {
                step = results[0];
                stepIdentifier = results[1];
            }
        }
        return step;
    }
}
