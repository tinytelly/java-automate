package org.tinytelly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinytelly.util.PropertyConstants;

@Service
public class LogService {
    private static StringBuilder log = new StringBuilder();

    @Autowired
    private PropertiesService propertiesService;

    public void log(String msg) {
        String logActive = propertiesService.getProperty(PropertyConstants.PROPERTY_LOG_ACTIVE);
        if (logActive != null && logActive.trim().toLowerCase().equals(PropertyConstants.YES)) {
            log.append("\n" + msg);
        }
    }

    public String getLogAndResetLog() {
        String result = log.toString();
        log = new StringBuilder();//Reset the log.
        return result;
    }

}
