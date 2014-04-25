Automate
========

### Automate is a framework to automate tasks.

####Overview:

This framework creates a predictable way to automate tasks.  Tasks can be chained together in a .plan which will execute
any number of steps.  A step is any code that extends ```org.tinytelly.steps.Step```.  A step is configured by setting the step
properties in a .properties file.

####Provided Sample Details:

There are two steps in ```org.tinytelly.steps.common```.  The are ```CreateFilesStep``` and ```WriteToFilesStep```.
They are called in the ```sample.plan``` file and will be executed one after the other.  The properties to configure those two steps are set in
```sample.properties```. The outcome of executing the sample.plan is that a file is created and some text is written to the file.

####Usage:

Note all --plan and --properties shown below will have their absolute path append to them like this: [pathto]/plans/ & [pathto]/properties/

**Run a plan**:

  ```Automate --plan=/plans/sample.plan --properties=/properties/sample.properties```

**Run a plan with an override** The override is used once and then discarded. So if you had the same property in a
.properties file it use the override on the first call and then use the .properties version for subsequent calls :

  ```Automate --plan=/plans/sample.plan --properties=/properties/sample.properties --override=writeToFiles.append=y```

####What to do now?

Write your own Steps (to do anything), configure them via .properties and run them as a .plan.



