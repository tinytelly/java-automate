package org.tinytelly.steps.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tinytelly.steps.Step;
import org.tinytelly.util.EnvironmentUtils;
import org.tinytelly.util.PlanConstants;
import org.tinytelly.util.PropertyConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This Step will do the following :
 * <ul>
 * <li>It will create a given list of files</li>
 * </ul>
 * <b>To use this step just add the following to a .plan file</b>
 * <ul>
 * <li>createFiles</li>
 * </ul>
 * <b>To configure this step add to the .properties file the following.</b>
 * <ul>
 * <li>{@value CreateFilesStep#FILES}</li>
 * <li>{@value CreateFilesStep#SKIP_IF_EXISTS}</li>
 * </ul>
 */
@Component
public class CreateFilesStep extends Step {
    /**
     * <p>
     * Specify the list of files to create.
     * </p>
     * <p>
     * <b>Example: </b>
     * createFiles.files=/var/tmp/file1.text | /var/tmp/file2.text
     * </p>
     */
    public static final String FILES = "createFiles.files";
    /**
     * <p>
     * y to skip creating the file if it already exists.
     * </p>
     * <p>
     * <b>Example: </b>
     * createFiles.skip.if.exists=y
     * </p>
     */
    public static final String SKIP_IF_EXISTS = "createFiles.skip.if.exists";

    @Autowired
    private EnvironmentUtils environmentUtils;

    @Override
    public void doStep() throws Exception {
        List<String> filesToCreate = getPropertyList(FILES);

        for (String fileToCreate : filesToCreate) {
            File file = new File(fileToCreate);
            boolean alreadyCreated = false;

            if (PropertyConstants.YES.equalsIgnoreCase(getProperty(SKIP_IF_EXISTS))) {
                alreadyCreated = file.exists();
            }

            if (!alreadyCreated) {
                try {
                    file.createNewFile();
                    makeFileWritable(file);

                    log(fileToCreate + " created");
                } catch (IOException e) {
                    new File(fileToCreate).getParentFile().mkdirs();//Try to create the path first
                    try {
                        new FileWriter(file);
                        makeFileWritable(file);

                        log(fileToCreate + " created");
                    } catch (IOException e1) {
                        this.payLoad.addError(PlanConstants.PLAN_CREATE_FILES, "File not created successfully : " + e.getLocalizedMessage());
                        break;
                    }
                }
            } else {
                log("File was already present and not recreated : " + fileToCreate);
            }
        }
    }

    private void makeFileWritable(File file) throws IOException {
        if (EnvironmentUtils.OS.LINUX.equals(environmentUtils.getOperatingSystem()) || EnvironmentUtils.OS.UNIX.equals(environmentUtils.getOperatingSystem())) {
            log("Setting the file permissions to be writable on path : " + file.getAbsolutePath());
            //Make the file executable
            //using PosixFilePermission to set file permissions 777
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            //add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            //add group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            //add others permissions
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(Paths.get(file.getAbsolutePath()), perms);
        } else {
            log("Setting environment permissions is not supported on this Operating System : " + environmentUtils.getOperatingSystem());
        }
    }
}
