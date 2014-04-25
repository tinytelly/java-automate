package org.tinytelly.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
public class GsonService {
    @Autowired
    private LogService logService;

    public String convertToJson(Object payLoad) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        String json = gson.toJson(payLoad);
        logService.log("--------------------------------------------");
        logService.log(json);
        logService.log("--------------------------------------------");
        return json;
    }

    public Object covertFromJson(String json, Type type) {
        return new Gson().fromJson(json, type);
    }
}
