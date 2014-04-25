package org.tinytelly.service;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.tinytelly.model.PropertyPair;
import org.tinytelly.util.PropertyConstants;
import org.tinytelly.util.XProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PropertiesService {
    private static List<XProperties> properties = new ArrayList<XProperties>();
    private static HashSet<String> overrideProperties;
    public static String SEPARATE_ON = "|";
    public static String SPLIT_ON = "=";
    public static String LINK_ON = "~";
    private XProperties stackedProperties = new XProperties();
    private final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

    public void load(List<String> propertyFileNames) {
        for (String propertyFileName : propertyFileNames) {
            try {
                XProperties property = new XProperties();
                properties.add(property);
                property.load(new FileInputStream(propertyFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        performPostLoadOfPropertiesOperations(true);
        extractStackedProperties();
    }

    public void performPostLoadOfPropertiesOperations(boolean reverse) {
        //We reverse the properties as we want to give the order the property overrides
        // by importance in last in first out.  So for cbis.prop, client.props we want client to be used over cbis
        if (reverse) {
            Collections.reverse(properties);
        }

        makeCommonPropertiesAvailableToAllProperties();
    }

    /**
     * A Stacked Property would look like this:
     * create.directory.1=asdf
     * create.directory.2=asdf
     * <p/>
     * Here we are saying that there are 2 times that create directory will be used in a plan.
     * The first time it will pick up create.directory.1 the second time it will pick up create.directory.2
     */
    public void extractStackedProperties() {
        for (XProperties propertyFile : properties) {
            for (Map.Entry<Object, Object> e : propertyFile.entrySet()) {
                String key = e.getKey().toString();
                Matcher matcher = lastIntPattern.matcher(key);
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt == 1) {
                        String keyWithoutNumber = key.substring(0, key.lastIndexOf("."));
                        overrideProperty(keyWithoutNumber, e.getValue().toString());//Override the X.1 property as X
                    } else {
                        stackedProperties.put(e.getKey(), e.getValue());
                    }
                }
            }
        }
    }

    public void setOverrideProperties(HashSet<String> overrideProperties) {
        if (this.overrideProperties == null) {
            this.overrideProperties = new HashSet<String>();
        }

        addToOverrideProperties(overrideProperties);

        /*for(String property : this.overrideProperties){
            System.out.println(property);
        }*/
    }

    public void resetOverrideProperties() {
        this.overrideProperties = new HashSet<String>();
    }

    public void resetAllProperties() {
        this.overrideProperties = new HashSet<String>();
        this.properties = new ArrayList<XProperties>();
    }

    public Integer getPropertyNumber(String property) {
        return Integer.valueOf(getProperty(property));
    }

    public String getProperty(String property) {
        //This allows us to override a property with one passed in via the command line
        if (this.overrideProperties != null && this.overrideProperties.size() > 0) {
            String currentOverride = null;
            String propertyFoundInOverride = null;
            String propertyKeyFoundInOverride = null;
            for (String override : overrideProperties) {
                if (override != null) {
                    String tempProp = override.substring(0, override.indexOf(SPLIT_ON));
                    int count = StringUtils.countMatches(override, SPLIT_ON);
                    if (property.equals(tempProp)) {
                        if (count == 1) {
                            Iterable<java.lang.String> prop = Splitter.on(SPLIT_ON).trimResults().omitEmptyStrings().split(override);
                            for (String p : prop) {
                                if (!p.equalsIgnoreCase(property)) {
                                    currentOverride = override;
                                    propertyFoundInOverride = p;
                                } else {
                                    propertyKeyFoundInOverride = p;
                                }
                            }
                        } else {
                            List<String> props = new ArrayList<String>(2);
                            props.add(override.substring(0, override.indexOf(SPLIT_ON)));
                            props.add(override.substring(override.indexOf(SPLIT_ON) + SPLIT_ON.length()));
                            for (String p : props) {
                                if (!p.equalsIgnoreCase(property)) {
                                    currentOverride = override;
                                    propertyFoundInOverride = p;
                                } else {
                                    propertyKeyFoundInOverride = p;
                                }
                            }

                        }
                    }
                }
            }
            if (propertyFoundInOverride != null) {
                overrideProperties.remove(currentOverride);//We use and override then remove it so it is not picked up again.
                //Add the next override if it exists in StackedProperties
                setNextOverrideFromStackedProperties(propertyKeyFoundInOverride);
                return propertyFoundInOverride;
            }
        }
        return getPropertyFromList(property);
    }

    private void setNextOverrideFromStackedProperties(String propertyKeyFoundInOverride) {
        int nextNumber = PropertyConstants.IMPOSSIBLY_HIGH_STACKED_PROPERTY_NUMBER;
        for (Map.Entry<Object, Object> prop : stackedProperties.entrySet()) {
            String key = prop.getKey().toString();
            if (key.startsWith(propertyKeyFoundInOverride)) {
                Matcher matcher = lastIntPattern.matcher(key);
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt < nextNumber) {
                        nextNumber = lastNumberInt;
                    }
                }
            }
        }
        if (nextNumber < PropertyConstants.IMPOSSIBLY_HIGH_STACKED_PROPERTY_NUMBER) {
            String overrideKey = propertyKeyFoundInOverride + "." + nextNumber;
            String overrideValue = stackedProperties.getProperty(overrideKey);
            overrideProperty(propertyKeyFoundInOverride, overrideValue);
            stackedProperties.remove(overrideKey);//Remove it so as its been used
        }
    }


    private String getPropertyFromList(String aProperty) {
        for (XProperties property : properties) {
            String prop = property.getProperty(aProperty);
            if (prop != null) {
                return prop;
            }
        }
        return null;
    }

    public List<String> getPropertyList(String property) {
        Predicate<String> nullPredicate = new Predicate<String>() {
            @Override
            public boolean apply(String prop) {
                if (prop == null || prop.trim().equals("null")) {
                    return false;
                }
                return true;
            }
        };

        String prop = getProperty(property);
        if (prop != null) {
            Iterable<String> props = Splitter.on(SEPARATE_ON).omitEmptyStrings().trimResults().split(prop);
            return Lists.newArrayList(Iterables.filter(
                    props,
                    nullPredicate
            ));
        }
        return null;
    }

    public boolean exists(String property) {
        return getPropertyFromList(property) != null ? true : false;
    }

    public void setProperty(String propertyKey, String propertyValue) {
        XProperties xProperties = new XProperties();
        xProperties.put(propertyKey, propertyValue);
        Collections.reverse(properties);//Reverse the order before adding the new property
        properties.add(xProperties);//Add the new property
        Collections.reverse(properties);//Reverse the order back to get this property at the top of the selection order
    }

    public void setProperties(XProperties property) {
        properties.add(property);
    }

    public void overrideProperties(XProperties property) {
        properties = new ArrayList<XProperties>();
        properties.add(property);
    }

    public void overrideProperty(String propertyToOverride, String override) {
        String newOverride = constructOverride(propertyToOverride, override);
        HashSet<String> createDirectoriesOverrideProperties = new HashSet<String>();
        createDirectoriesOverrideProperties.add(newOverride);
        setOverrideProperties(createDirectoriesOverrideProperties);
    }

    private String constructOverride(String propertyToOverride, String override) {
        List<String> overrides = new ArrayList<String>(1);
        overrides.add(override);
        return constructOverride(propertyToOverride, overrides);
    }

    private String constructOverride(String propertyToOverride, List<String> overrides) {
        StringBuilder result = new StringBuilder(propertyToOverride + PropertiesService.SPLIT_ON);
        int count = 1;
        for (String override : overrides) {
            result.append(override);
            if (count < overrides.size()) {
                result.append(PropertiesService.SEPARATE_ON);
            }
            count++;
        }
        return result.toString();
    }

    public List<PropertyPair> getListOfPropertyPairsFromProperty(String property) {
        List<PropertyPair> result = new ArrayList<PropertyPair>();
        List<String> properties = getPropertyList(property);

        if (properties != null) {
            for (String prop : properties) {
                Iterable<String> p = Splitter.on(PropertiesService.LINK_ON).split(prop);
                List<String> propertyPair = Lists.newArrayList(p);
                if (propertyPair.size() > 1) {
                    String filePath = propertyPair.get(0);
                    String fileContents = propertyPair.get(1);
                    for (int i = 2; i < propertyPair.size(); i++) {
                        fileContents += PropertiesService.LINK_ON + propertyPair.get(i);
                    }
                    PropertyPair pp = new PropertyPair(filePath, fileContents);
                    result.add(pp);
                }
            }
        }
        return result;
    }

    public boolean isOn(String property) {
        String prop = this.getProperty(property);
        if (prop != null && PropertyConstants.YES.equalsIgnoreCase(prop)) {
            return true;
        } else {
            return false;
        }
    }

    private void addToOverrideProperties(HashSet<String> overrideProperties) {
        for (String overrideProperty : overrideProperties) {
            String tempKey = overrideProperty.substring(0, overrideProperty.indexOf(SPLIT_ON));
            String tempProperty = overrideProperty.substring(overrideProperty.indexOf(SPLIT_ON) + SPLIT_ON.length());

            String existingPropertyWithSameId = null;
            for (String property : this.overrideProperties) {
                if (property.startsWith(tempKey)) {
                    existingPropertyWithSameId = property;
                    break;
                }
            }
            if (existingPropertyWithSameId != null) {
                this.overrideProperties.remove(existingPropertyWithSameId);
            }


            String newOverride = constructOverride(tempKey, tempProperty);
            HashSet<String> createDirectoriesOverrideProperties = new HashSet<String>();
            createDirectoriesOverrideProperties.add(newOverride);
            this.overrideProperties.addAll(createDirectoriesOverrideProperties);
            makeCommonPropertiesAvailableToAllProperties();
        }
    }

    private void makeCommonPropertiesAvailableToAllProperties() {
        Set<String> commonProperties = new HashSet<String>();
        Pattern logEntry = Pattern.compile("\\{(.*?)\\}");

        //1) find all the properties with {X}
        for (XProperties xProperties : properties) {
            for (Map.Entry<Object, Object> e : xProperties.entrySet()) {
                Matcher matchPattern = logEntry.matcher((String) e.getValue());

                while (matchPattern.find()) {
                    commonProperties.add(matchPattern.group(1));
                }
            }

        }

        //2) find the actual X= property
        for (String propertyKey : commonProperties) {
            String propertyValue = getProperty(propertyKey);
            if (propertyValue != null) {
                //3) set X property in all the properties so its available to all
                for (XProperties xProperties : properties) {
                    xProperties.put(propertyKey, propertyValue);
                }
                String overridePerformedOnThis = null;
                for (String op : overrideProperties) {
                    if (op.contains(propertyKey)) {
                        overridePerformedOnThis = op;
                        overrideProperties.add(op.replace("{" + propertyKey + "}", propertyValue));
                        break;
                    }
                }
                if (overridePerformedOnThis != null) {
                    overrideProperties.remove(overridePerformedOnThis);
                }
            }
        }
    }

    public boolean isValuePresentInListOfProperties(List<PropertyPair> pairs, String keyToLookFor, String valueToLookFor) {
        if (valueToLookFor == null || getProperty(keyToLookFor) == null) {
            return false;
        }
        for (PropertyPair pair : pairs) {
            if (pair.getRight().equals(valueToLookFor)) {
                return true;
            }
        }
        return false;
    }

    public boolean showRealTimeLogs() {
        String showRealTimeLogs = getProperty("show.real.time.logs");
        if (showRealTimeLogs == null) {
            return true;
        }
        if (!PropertyConstants.YES.equalsIgnoreCase(showRealTimeLogs)) {
            return false;
        }
        return true;
    }

    public static String convert(Object o) {
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(((Class) o).getSimpleName()), '.').toLowerCase() + ".";
    }

}