package uk.ac.sanger.aker.catalogue.model;

/**
 * Something with a UUID that can be set.
 * @author dr6
 */
public interface HasUuid {
    /**
     * Gets the UUID for this item, as a string; or null if it doesn't have one.
     * @return the UUID string, or null
     */
    String getUuid();

    /**
     * Sets the UUID for this item, as a string.
     * @param uuid the UUID string
     * @exception NullPointerException if the uuid is null, and this model does not allow null for a UUID
     */
    void setUuid(String uuid);
}
