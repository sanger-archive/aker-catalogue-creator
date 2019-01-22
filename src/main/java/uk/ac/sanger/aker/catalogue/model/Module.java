package uk.ac.sanger.aker.catalogue.model;

import java.util.Objects;

/**
 * @author dr6
 */
public class Module implements HasName {
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

    public boolean hasParameter() {
        return (this.minValue!=null || this.maxValue!=null);
    }
}
