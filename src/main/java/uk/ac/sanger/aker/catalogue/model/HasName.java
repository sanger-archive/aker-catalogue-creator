package uk.ac.sanger.aker.catalogue.model;

/**
 * Something with a name, that can be altered.
 * @author dr6
 */
public interface HasName {
    /**
     * Gets the name for this item.
     * @return the name for this item
     */
    String getName();

    /**
     * Sets the name for this item.
     * @param name the new name for this item
     * @exception NullPointerException if {@code name} is null, and this model does not support null for a name
     */
    void setName(String name);
}
