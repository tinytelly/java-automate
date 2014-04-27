Automate
========

### Automate is a framework to automate tasks.

####Overview:

This framework creates a predictable way to automate tasks.  Tasks can be chained together in a .plan which will execute
any number of steps.  A step is any code that extends ```org.tinytelly.steps.Step```.  A step is configured by setting the step
properties in a .properties file.

####Provided Sample Details:

There are two steps in ```org.tinytelly.steps.samples```, ```PremierLeagueTableFinderStep``` and ```PremierLeagueChampionsLeagueStep```.
They are called in the ```sample.plan``` file and will be executed one after the other.  The properties to configure those two steps are set in
```sample.properties```. The outcome of executing the sample.plan is that it will get the current Premier League table and determine the teams that are going to the Champions League.

####Usage:

Note all --plan and --properties shown below will have their absolute path appended to them like this: [pathto]/plans/ & [pathto]/properties/

**Run a plan**:

  ```Automate --plan=/plans/sample.plan --properties=/properties/sample.properties```

**Run a plan with an override**: The override is used once and then discarded. So if you had the same property in a
.properties file it uses the override on the first call and then use the .properties version for subsequent calls :

  ```Automate --plan=/plans/sample.plan --properties=/properties/sample.properties --override=premier.league.table.name=Liga```

####Properties:

**Standard use**:

  In the file ```sample.properties```
  is the property ```premier.league.table.name=Premier League```
  which is accessed in a step like this ```getProperty("premier.league.table.name")```

**Using an Identifier**: you can link a step in a plan file to a property by using an identifier.
This makes it easier to deal with plans that contain lots of properties.

  In the file ```sample.plan```
  is you could have a step called ```premierLeagueTableFinder england``` where england is the identifier.
  you would then link that in the properties file like this ```premier.league.table.name.england```
  and calling ```getProperty("premier.league.table.name")``` will pick up that property.

####What to do now?

Write your own Steps (to do anything), configure them via .properties and run them as a .plan.



