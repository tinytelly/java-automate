Automate
========

Automate is a framework to automate tasks.

Overview:
This framework creates a predictable way to automate tasks.  Tasks can be chained together into a .plan which will execute
any number of steps.  A step is any code that extends org.tinytelly.steps.Step.  A step is configured by setting the step
properties in a .properties file.

Sample Details:
There are two steps in org.tinytelly.steps.common.  The are CreateFilesStep and WriteToFilesStep.  They are called in the
sample.plan file and will be executed one after the other.  The properties to configure those two steps are set in
sample.properties. The outcome of executing the sample.plan is that a file is created and some text is written to the file.

Usage:
org.tinytelly.Automate --plan=/plans/sample.plan --properties=/properties/sample.properties

What to do now?
write your own Steps (to do anything), configure them via .properties and run them as a .plan.



