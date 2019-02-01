package uk.ac.sanger.aker.catalogue.model;

import java.util.*;

/**
 * A product, representing a thing that a user can order in Aker.
 * A product has some direct data fields, such as a name and a UUID, but primarily
 * it is a sequence of {@link AkerProcess processes}.
 * @author dr6
 */
public class Product implements HasName, HasUuid {
    private String name;
    private String uuid;
    private String description = "";
    private int productVersion = 1;
    private int availability = 1;
    private String bioType = "dna/rna";
    private List<AkerProcess> processes = new ArrayList<>();

    public Product() {
        this("");
    }

    public Product(String name) {
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProductVersion() {
        return this.productVersion;
    }

    public void setProductVersion(int productVersion) {
        this.productVersion = productVersion;
    }

    public int getAvailability() {
        return this.availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public String getBioType() {
        return this.bioType;
    }

    public void setBioType(String bioType) {
        this.bioType = bioType;
    }

    public List<AkerProcess> getProcesses() {
        return this.processes;
    }

    public void setProcesses(List<AkerProcess> processes) {
        this.processes = processes;
    }

    @Override
    public String toString() {
        return getName();
    }

}
