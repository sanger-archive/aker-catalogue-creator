package uk.ac.sanger.aker.catalogue.model;

import java.util.*;

/**
 * A process in the catalogue.
 * A process is a specified task that can be requested from a LIMS.
 * Processes include options for modules, which are specified by {@link ModulePair}s.
 * Processes are part of products.
 * @author dr6
 */
public class AkerProcess implements HasName, HasUuid {
    private String name;
    private String uuid;
    private int tat = 1;
    private String processClass = "sequencing";
    private List<ModulePair> modulePairs = new ArrayList<>();

    public AkerProcess() {
        this("");
    }

    public AkerProcess(String name) {
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

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid is null");
    }

    public int getTat() {
        return this.tat;
    }

    public void setTat(int tat) {
        this.tat = tat;
    }

    public String getProcessClass() {
        return this.processClass;
    }

    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    public List<ModulePair> getModulePairs() {
        return this.modulePairs;
    }

    public void setModulePairs(List<ModulePair> modulePairs) {
        this.modulePairs = modulePairs;
    }

    @Override
    public String toString() {
        return getName();
    }
}
