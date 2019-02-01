package uk.ac.sanger.aker.catalogue.model;

/**
 * A pair of modules representing a path {@link #getFrom() from} one module {@link #getTo() to} another.
 * When a process is serialised, the module-pairs contain the names of the modules it goes to and from.
 * When the {@code from} module is {@code Module.START}, the module-pair lists it as null.
 * When the {@code to} module is {@code Module.END}, the module-pair lists it as null.
 * This is unambiguous, since a path cannot to <i>to</i> the start or come <i>from</i> the end.
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

    public ModulePair(ModulePair other) {
        this(other.from, other.to, other.defaultPath);
    }

    /** The start point of this path */
    public Module getFrom() {
        return this.from;
    }

    /** Sets the start point */
    public void setFrom(Module from) {
        this.from = from;
    }

    /** The end point of this path */
    public Module getTo() {
        return this.to;
    }

    /** Sets the end point */
    public void setTo(Module to) {
        this.to = to;
    }

    /** Is the path represented by this pair marked as a default path through the process? */
    public boolean isDefaultPath() {
        return this.defaultPath;
    }

    public void setDefaultPath(boolean defaultPath) {
        this.defaultPath = defaultPath;
    }

    @Override
    public String toString() {
        return String.format("ModulePair(%s, %s)", from, to);
    }
}
