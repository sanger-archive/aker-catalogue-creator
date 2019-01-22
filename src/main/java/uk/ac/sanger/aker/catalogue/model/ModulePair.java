package uk.ac.sanger.aker.catalogue.model;

/**
 * @author dr6
 */
public class ModulePair {
    private Module from;
    private Module to;
    private boolean defaultPath;

    public ModulePair(Module from, Module to, boolean defaultPath) {
        this.from = from;
        this.to = to;
        this.defaultPath = defaultPath;
    }

    public Module getFrom() {
        return this.from;
    }

    public void setFrom(Module from) {
        this.from = from;
    }

    public Module getTo() {
        return this.to;
    }

    public void setTo(Module to) {
        this.to = to;
    }

    public boolean isDefaultPath() {
        return this.defaultPath;
    }

    public void setDefaultPath(boolean defaultPath) {
        this.defaultPath = defaultPath;
    }
}
