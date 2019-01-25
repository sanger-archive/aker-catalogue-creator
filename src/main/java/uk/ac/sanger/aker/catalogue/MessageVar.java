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
        int i = template.indexOf('{');
        if (i < 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template);
        do {
            int j = sb.indexOf("|", i);
            int k = sb.indexOf("}", i);
            if (j < i || j > k) {
                j = i;
            }
            int s,e;
            if (number==SINGULAR) {
                s = i + 1;
                e = j;
            } else {
                s = j + 1;
                e = k;
            }
            if (s < e) {
                sb.replace(i, k+1, sb.substring(s, e));
            } else {
                sb.delete(i, k+1);
            }
            i = sb.indexOf("{", i);
        } while (i >= 0);

        return sb.toString();
    }
}
