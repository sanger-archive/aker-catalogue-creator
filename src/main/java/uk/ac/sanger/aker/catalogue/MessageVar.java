package uk.ac.sanger.aker.catalogue;

/**
 * Message processor.
 * Given a template like:
 * <tt>"The following item(s) (is|are) here"</tt>,
 * the {@link #process} method can produce either
 * <tt>"The following item is here"</tt>
 * or
 * <tt>"The following items are here"</tt>
 * based on its {@code number} argument.
 * @author dr6
 */
public class MessageVar {
    public static final int SINGULAR = 1, PLURAL = 2;

    /**
     * Process a message template selecting either plural or singular parts
     * based on the {@code number} argument.
     * @param template the template to use to generate the string
     * @param number indicate whether to pluralise or singularise the message
     * @return the processed string
     * @see #SINGULAR
     * @see #PLURAL
     */
    public static String process(String template, int number) {
        StringBuilder sb = new StringBuilder(template);
        int i = sb.indexOf("(");
        while (i >= 0) {
            int j = sb.indexOf("|", i);
            int k = sb.indexOf(")", i);
            if (j < i || j > k) {
                j = i;
            }
            if (number!=SINGULAR) {
                if (i+1 < j) {
                    sb.replace(i, k + 1, sb.substring(i + 1, j));
                } else {
                    sb.delete(i, k+1);
                }
            } else {
                if (j+1 < k) {
                    sb.replace(i, k+1, sb.substring(j+1, k));
                } else {
                    sb.delete(i, k+1);
                }
            }

            i = sb.indexOf("(", i);
        }
        return sb.toString();
    }
}
