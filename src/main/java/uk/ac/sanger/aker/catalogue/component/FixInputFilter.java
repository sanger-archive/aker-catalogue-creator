package uk.ac.sanger.aker.catalogue.component;

import javax.swing.text.*;
import java.util.function.Function;

/**
 * A version of {@code DocumentFilter} that passes all inserted text through a given function.
 * The typical use of this is to exclude or replace certain characters in the input.
 * @author dr6
 */
public class FixInputFilter extends DocumentFilter {
    private Function<? super String, String> fixer;

    /**
     * Constructs a new filter using the given function to alter any non-null inserted string.
     * @param fixer a function that will return the string that should be added
     */
    public FixInputFilter(Function<? super String, String> fixer) {
        this.fixer = fixer;
    }

    /**
     * This method applies the {@code fixer} function to any non-string that is provided.
     * @param string the string received by the text component
     * @return the fixed string, or null if {@code string} is null
     */
    public String fix(String string) {
        if (string!=null) {
            string = this.fixer.apply(string);
        }
        return string;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        fb.insertString(offset, fix(string), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
        fb.replace(offset, length, fix(string), attrs);
    }
}
