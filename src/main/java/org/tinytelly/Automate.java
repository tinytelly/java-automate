package org.tinytelly;

import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.stereotype.Component;
import org.tinytelly.model.PayLoad;
import org.tinytelly.model.Plan;
import org.tinytelly.service.PlanService;
import org.tinytelly.service.PropertiesService;
import org.tinytelly.util.PropertyConstants;
import org.tinytelly.util.XProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class Automate {
    public static List<String> propertiesFileNames = new ArrayList<String>();
    public static String planFileName;
    private XProperties properties;
    private HashSet<String> overrideProperties = new HashSet<String>();
    private Plan plan;
    private static boolean exitOnError = false;

    public static void main(String... args) {
        Automate automate = new Automate();
        PayLoad payLoad = automate.doWork(args);
        if (payLoad.hasErrors() && exitOnError) {
            System.exit(-1);//This allows the caller (maybe hudson) to determine if it failed or not
        }
    }

    public PayLoad doWork(String... args) {
        processCommandLineArguments(args);

        ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

        Automate automate = context.getBean(Automate.class);
        automate.setOverrideProperties(overrideProperties);
        return automate.doWork();
    }

    private void processCommandLineArguments(String... args) {
        PropertySource ps = new SimpleCommandLinePropertySource(args);
        if (!ps.containsProperty("plan")) {
            System.out.println("the argument --plan is required");
        } else {
            planFileName = ps.getProperty("plan").toString();
        }
        if (!ps.containsProperty("properties")) {
            System.out.println("the argument --properties is required");
        } else {
            for (String o : Splitter.on("&").trimResults().omitEmptyStrings().split((CharSequence) ps.getProperty("properties"))) {
                propertiesFileNames.add(o);
            }
        }

        //Check for override properties
        if (args != null && args.length > 0) {
            ps = new SimpleCommandLinePropertySource(args);
            if (ps.containsProperty("override")) {
                overrideProperties = new HashSet<String>();
                String override = ps.getProperty("override").toString();
                System.out.println("override properties passed in : " + override);

                for (String o : Splitter.on("&").trimResults().omitEmptyStrings().split(override)) {
                    overrideProperties.add(o);
                }
            }
        }

        //Pass in the argument exitOnError if you wish to exit with an error code. Typically only used when running
        // Automate as a standalone command line application
        if (ps.containsProperty("exitOnError")) {
            exitOnError = false;
        } else {
            exitOnError = true;
        }
    }

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private PlanService planService;

    private PayLoad doWork() {
        propertiesService.setOverrideProperties(overrideProperties);
        propertiesService.load(propertiesFileNames);
        String usage = propertiesService.getProperty(PropertyConstants.USAGE);
        if (usage != null) {
            System.out.println(usage);
        }
        this.plan = planService.createPlanFromFile(planFileName);
        return planService.execute(plan);
    }

    public void setOverrideProperties(HashSet<String> overrideProperties) {
        this.overrideProperties = overrideProperties;
    }
}
