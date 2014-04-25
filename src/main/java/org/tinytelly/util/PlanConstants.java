package org.tinytelly.util;

public class PlanConstants {
    public static final String PLAN_PAYLOAD_TO_JSON = "payloadToJson";
    public static final String PLAN_CREATE_FILES = "createFiles";
    public static final String PLAN_WRITE_TO_FILES = "writeToFiles";
    public static final String PLAN_SUCCESSFUL_RUN = " SUCCESSFULLY run.";
    public static final String PLAN_NOT_SUCCESSFULLY_RUN = " NOT" + PLAN_SUCCESSFUL_RUN;

    public static final String PLAN_TEST_MODE = ".test.mode";//Any step in the properties that is [stepName].test.mode will not execute doWork
}
