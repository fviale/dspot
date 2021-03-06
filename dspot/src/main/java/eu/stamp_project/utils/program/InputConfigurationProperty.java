package eu.stamp_project.utils.program;

import java.util.Properties;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/06/18
 */
public class InputConfigurationProperty {

    private final String name;
    private final String description;
    private final String defaultValue;
    private final String naturalLanguageDesignation;

    public InputConfigurationProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.naturalLanguageDesignation = "";
    }

    public InputConfigurationProperty(String name,
                                      String description,
                                      String defaultValue,
                                      String naturalLanguageDesignation) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.naturalLanguageDesignation = naturalLanguageDesignation;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getNaturalLanguageDesignation() {
        return naturalLanguageDesignation;
    }

    public boolean isRequired() {
        return this.defaultValue == null;
    }

    private boolean hasDefaultValue() {
        return !isRequired() && !this.defaultValue.isEmpty();
    }

    public String get(Properties properties) {
        if (!isRequired()) {
            return properties.getProperty(this.getName(), this.getDefaultValue());
        } else {
            return properties.getProperty(this.getName());
        }
    }

    /**
     * This method return in markdown format a description of the Property
     * @return
     */
    @Override
    public String toString() {
        return "\t* " + this.name + ": " + this.description + (this.hasDefaultValue() ? "(default: " + this.defaultValue + ")" : "");
    }
}
