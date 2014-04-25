package org.tinytelly.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tinytelly.service.PropertiesService;

@Component
public class EnvironmentUtils {
    public enum OS {WINDOWS, LINUX, UNIX, UNKNOWN}

    @Autowired
    private PropertiesService propertiesService;


    public OS getOperatingSystem() {
        String os = propertiesService.getProperty("os");
        if (os == null) {
            return OS.UNKNOWN;
        } else if (os.equalsIgnoreCase("windows")) {
            return OS.WINDOWS;
        } else if (os.equalsIgnoreCase("unix")) {
            return OS.UNIX;
        } else if (os.equalsIgnoreCase("linux")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }
}
