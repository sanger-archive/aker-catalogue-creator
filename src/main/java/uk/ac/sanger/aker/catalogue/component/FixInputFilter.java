package uk.ac.sanger.aker.catalogue.component;

import javax.swing.text.*;
import java.util.function.Function;

/**
 * @author dr6
 */
public class FixInputFilter extends DocumentFilter {
    private Function<? super String, String> fixer;

    public FixInputFilter(Function<? super String, String> fixer) {
        this.fixer = fixer;
    }

    public String fix(String string) {
        if (string!=null) {
            string = this.fixer.apply(string);
        }
        return string;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string!=null) {
            string = fix(string);
            if (string.isEmpty()) {
                return;
            }
        }
        fb.insertString(offset, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
        fb.replace(offset, length, fix(string), attrs);
    }
}
