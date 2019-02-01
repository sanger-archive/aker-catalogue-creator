package uk.ac.sanger.aker.catalogue;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Resources for showing help in the application.
 * @author dr6
 */
public class Help {
    private Help() {}

    /** The icon to use for a help button. */
    public static final Icon HELP_ICON = loadIcon("images/help.png");
    /** The text that will be used when a requested text resource could not be loaded. */
    public static final String MISSING_TEXT = "The resource could not be loaded.";
    /** The help text for the module graph (html) */
    public static final String MODULE_GRAPH_HELP = loadHtml("text/module_graph_help.html");

    private static Icon loadIcon(String filename) {
        URL resource = Help.class.getClassLoader().getResource(filename);
        return (resource==null ? new ImageIcon() : new ImageIcon(resource));
    }

    private static String loadHtml(String filename) {
        InputStream is = Help.class.getClassLoader().getResourceAsStream(filename);
        if (is==null) {
            return MISSING_TEXT;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        return CatalogueApp.htmlWrap(in.lines().collect(Collectors.joining(" ")));
    }
}
