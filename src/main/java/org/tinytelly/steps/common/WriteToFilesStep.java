package org.tinytelly.steps.common;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.tinytelly.model.PropertyPair;
import org.tinytelly.steps.Step;
import org.tinytelly.util.PlanConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This Step will do the following :
 * <ul>
 * <li>It allows to write to a given list of files. Care must be taken to set {@value WriteToFilesStep#APPEND} or the file will be over written.</li>
 * </ul>
 * <b>To use this step just add the following to a .plan file</b>
 * <ul>
 * <li>writeToFiles</li>
 * </ul>
 * <b>To configure this step add to the .properties file the following.</b>
 * <ul>
 * <li>{@value WriteToFilesStep#WRITE_CONTENTS}</li>
 * <li>{@value WriteToFilesStep#APPEND}</li>
 * </ul>
 */
@Component
public class WriteToFilesStep extends Step {
    /**
     * <p>
     * Specify the list of files to write to and the contents to write.
     * </p>
     * <p>
     * <b>Key: </b><i>[full path of file 1] ~ [contents to write in file 1] | [full path of file 2] ~ [contents to write in file 2]</i>
     * </p>
     * <p>
     * <b>Example: </b>
     * writeToFiles.write.contents=/var/tmp/asdf.text ~ add this
     * </p>
     */
    public static final String WRITE_CONTENTS = "writeToFiles.write.contents";
    /**
     * <p>
     * y to append to the file n to overwrite the file.
     * </p>
     * <p>
     * <b>Example (append to the existing file): </b>
     * writeToFiles.append=y
     * </p>
     * <p>
     * <b>Example (overwrite to the existing file : Note omitting this value will overwrite the file): </b>
     * writeToFiles.append=n
     * </p>
     */
    public static final String APPEND = "writeToFiles.append";

    @Override
    public void doStep() throws Exception {
        List<PropertyPair> propertyPairs = getListOfPropertyPairsFromProperty(WRITE_CONTENTS);
        boolean append = isOn(APPEND);

        for (PropertyPair propertyPair : propertyPairs) {
            String filePath = propertyPair.getLeft();
            String fileContents = propertyPair.getRight();
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    FileUtils.writeStringToFile(file, fileContents, append);
                } catch (IOException e) {
                    this.payLoad.addError(PlanConstants.PLAN_WRITE_TO_FILES, "Could not write to this file : " + filePath + " : due to this error " + e.getLocalizedMessage());
                    break;
                }
                log("File Written to : " + filePath);
                log("File was appended to : " + append);
                log("Contents added to the file : " + fileContents);
                logService.log(result);
                payLoad.append(PlanConstants.PLAN_WRITE_TO_FILES, fileContents);
            } else {
                this.payLoad.addError(PlanConstants.PLAN_WRITE_TO_FILES, "The file : " + filePath + " does not exist to write to.");
            }
        }
    }
}
