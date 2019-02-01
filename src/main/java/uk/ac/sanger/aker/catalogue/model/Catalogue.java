package uk.ac.sanger.aker.catalogue.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A catalogue is the set of work offered by a LIMS.
 * The products are items that a user can order in Aker.
 * A product is made up of processes, which represent types of work Aker can order in the LIMS.
 * A process includes modules, which are options a user can choose when ordering work.
 * The modules, processes and products described in a catalogue are all listed directly inside the
 * {@code Catalogue} object, and they also have direct references to each other.
 * @author dr6
 */
public class Catalogue {
    private String pipeline = "Genomics Pipeline";
    private String url = "http://localhost:3400";
    private String limsId = "SQSC";
    private List<Module> modules = new ArrayList<>();
    private List<AkerProcess> processes = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    public String getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLimsId() {
        return this.limsId;
    }

    public void setLimsId(String limsId) {
        this.limsId = limsId;
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<AkerProcess> getProcesses() {
        return this.processes;
    }

    public void setProcesses(List<AkerProcess> processes) {
        this.processes = processes;
    }

    public List<Product> getProducts() {
        return this.products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
