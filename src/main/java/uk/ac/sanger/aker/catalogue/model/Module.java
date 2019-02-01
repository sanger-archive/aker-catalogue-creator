package uk.ac.sanger.aker.catalogue.model;

import java.util.Objects;

/**
 * A module represents an option that can be chosen by a user when they are ordering work.
 * The modules available in a process are specified by {@link ModulePair}s,
 * and all the modules used by all processes must be listed inside the {@link Catalogue#getModules Catalogue} itself.
 * @author dr6
 */
public class Module implements HasName {
    /**
     * The special START module.
     * @see ModulePair
     */
    public static final Module START = new Module("START");
    /**
     * The special END module.
     * @see ModulePair
     */
    public static final Module END = new Module("END");

    private String name;
    private Integer minValue, maxValue;

    public Module(String name) {
        setName(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name is null");
    }

    public String serialisationName() {
        if (this==START || this==END) {
            return null;
        }
        return getName();
    }

    public Integer getMinValue() {
        return this.minValue;
    }

    public Integer getMaxValue() {
        return this.maxValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * A module has a parameter if either its {@link #setMinValue} or {@link #setMaxValue} is set.
     */
    public boolean hasParameter() {
        return (this.minValue!=null || this.maxValue!=null);
    }

    /**
     * The special {@link #START} and {@link #END} modules are endpoints.
     * All other modules (real modules) are not endpoints.
     */
    public boolean isEndpoint() {
        return (this==START || this==END);
    }

    @Override
    public String toString() {
        if (this==START) {
            return "Module.START";
        }
        if (this==END) {
            return "Module.END";
        }
        return String.format("Module(\"%s\")", getName());
    }
}
