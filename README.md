Automate
========

### Automate is a framework to automate tasks.

####Overview:

This framework creates a predictable way to automate tasks.
Tasks (Steps in the framework) can be anything. If a computer can do it then it can become a Step.
Steps are chained together in a ```.plan``` which will execute
any number of steps.  A step is any code that extends ```org.tinytelly.steps.Step```.  A step is configured by setting the step
properties in a .properties file.

####Provided Sample Details:

There are two steps in ```org.tinytelly.steps.samples```, ```PremierLeagueTableFinderStep``` and ```PremierLeagueChampionsLeagueStep```.
They are called in the ```sample.plan``` file and will be executed one after the other.  The properties to configure those two steps are set in
```sample.properties```. The outcome of executing the sample.plan is that it will get the current Premier League table and determine the teams that are going to the Champions League.

####Usage:

Note all --plan and --properties shown below will have their absolute path appended to them like this: [pathto]/plans/ & [pathto]/properties/

**Run a plan**:

  ```Automate --[pathto]/plan=sample.plan --properties=[pathto]/sample.properties```

**Run a plan with an override**: The override is used once and then discarded. So if you had the same property in a
.properties file it uses the override on the first call and then use the .properties version for subsequent calls :

  ```Automate --plan=[pathto]/sample.plan --properties=[pathto]/sample.properties --override=premier.league.table.name=Liga```

**Call a plan from any other application**:
Include the automate.jar in your application and call it the same way as above.
The return value is a ```org.tinytelly.model.Payload``` where you can ask for a payload by calling ```payload.getLoad(key)``` or ```payload.getMostRecentLoad()```
You can also get the log by calling ```payload.getResults()```.

####Properties:

**Standard use**:

  In the file ```sample.properties```
  is the property ```premier.league.table.name=Premier League```
  which is accessed in a step like this ```getProperty("premier.league.table.name")```

**Using an Identifier**: you can link a step in a plan file to a property by using an identifier.
This makes it easier to deal with plans that contain lots of properties.

  In the file ```sample.plan```
  you could have a step called ```premierLeagueTableFinder england``` where england is the identifier.
  you would then link that in the properties file like this ```premier.league.table.name.england```
  and calling ```getProperty("premier.league.table.name")``` will pick up that property.

**Stacked Properties**: you can stack properties by appending a number to the end of a property. Automate will use the number to
find the next property to use.

  In the file ```sample.plan```
  you could have a step called called twice like this (on separate lines)  ```premierLeagueTableFinder premierLeagueTableFinder```
  you would then provide properties for the first step like this ```premier.league.table.name.1``` and the second step like this ```premier.league.table.name.2```.

**Property lists**:

  If you want to provide properties that are turned into a ```List<String>```
  you would set the properties like this ```premier.league.table.name=Premier League | Liga```
  which is accessed in a step like this ```getPropertyList("premier.league.table.name")```

**Property Pair**:

  If you want to provide a Pair which contains a left and right property
  you would set the properties like this ```premier.league.league.and.season=Premier League ~ 2014 | Liga ~ 2014```
  which is accessed in a step like this ```getListOfPropertyPairsFromProperty("premier.league.league.and.season")```
  which returns a ```List<PropertyPair>``` which is accessed like this ```propertyPair.getLeft() and .getRight()```

####Payload:
The ```payload``` is passed from step to step and can be used to pass the result of one step to another.
It call also be used to pass a payload to an external caller.  You may call automate from an external application (like Spring MVC)
and then use that payload to display the results on a web page.  To do this include the Automate.jar (product of the included maven build)
and call ```doWork(String... args)``` on ```Automate``` which returns a Payload.  The payload can contain any object you wish.
To add a new type to be used as a payload you need to the type to ```preparePayload``` in ```PayloadStep```.

**Manually populate a payload for a step**:
You can manually generate a payload and populate a payload for a step using ``` payloadToJson and payload```
The example plan file ```sample_to_generate_a_manual_payload.plan``` which calls a step and then outputs the payload as json to the property ```payload.to.json.step.location```

The example plan file ```sample_to_use_a_manual_payload.plan``` will use the property ```payload.json``` to populate the payload accessed in ```PremierLeagueChampionsLeagueStep``` via ```payLoad.getMostRecentLoad()```
This enables you to manually set up a step with predefined data rather then the result of a previous step (which in the sample case would have been ```PremierLeagueTableFinderStep```

####Logging and Error handling?

A step has a method ```log()```. Anything that is logged will be printed out as part of the run of the plan.
A step has an error log.  If error is populated during a step the plan will stop and the error is printed out.
Any unhandled exception will be handed as an error by the framework.

####What to do now?

Write your own Steps (to do anything), configure them via .properties and run them as a .plan.



