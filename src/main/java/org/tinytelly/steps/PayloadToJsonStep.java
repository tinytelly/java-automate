package org.tinytelly.steps;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tinytelly.service.GsonService;
import org.tinytelly.util.PlanConstants;

import java.io.File;
import java.io.IOException;

@Component
public class PayloadToJsonStep extends Step {
    @Autowired
    private GsonService gsonService;

    @Override
    public void doStep() throws Exception {
        if (payLoad.getMostRecentLoad() == null) {
            this.error = "There is no payload to convert to json";
        } else {
            String json = gsonService.convertToJson(payLoad.getMostRecentLoad());
            this.payLoad.append(PlanConstants.PLAN_PAYLOAD_TO_JSON, json);
            this.result = "Payload successfully converted to JSON";

            String writeToDiskLocation = propertiesService.getProperty("payload.to.json.location");
            if (writeToDiskLocation != null) {
                try {
                    FileUtils.writeStringToFile(new File(writeToDiskLocation), json);
                } catch (IOException e) {
                    e.printStackTrace();//Not a serious fail so just report it
                }
            }
        }
    }
}
