package org.tinytelly.steps;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tinytelly.model.PropertyPair;
import org.tinytelly.service.GsonService;
import org.tinytelly.util.PlanConstants;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

@Component
public class PayloadStep extends Step {
    public static final String JSON = "payload.json";
    @Autowired
    private GsonService gsonService;

    @Override
    public void doStep() throws Exception {
        List<PropertyPair> propertyPairs = propertiesService.getListOfPropertyPairsFromProperty(JSON);
        for (PropertyPair propertyPair : propertyPairs) {
            String jsonFile = propertyPair.getLeft();
            String destination = propertyPair.getRight();
            String json = FileUtils.readFileToString(new File(jsonFile));
            preparePayload(destination, json);
        }
    }

    private void preparePayload(String destination, String json) {
        Type type = null;
        if (destination.equals(PlanConstants.PLAN_WRITE_TO_FILES)) {
            type = new TypeToken<List<String>>() {
            }.getType();
        } else {
            this.error = "This step is not supported for Manual Payload :" + destination;
        }
        if (type != null) {
            this.payLoad.append(destination, gsonService.covertFromJson(json, type));
            this.result = "Payload preparation successful for : " + destination;
        }
    }
}
