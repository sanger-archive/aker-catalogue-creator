package uk.ac.sanger.aker.catalogue.component;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * @author dr6
 */
public class QuickConstraints extends GridBagConstraints {
    public int leftx = 0;
    public int rightx = 1;
    public int leftAnchor = LINE_END;
    public int rightAnchor = LINE_START;

    public QuickConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty,
                            int anchor, int fill, Insets insets, int ipadx, int ipady) {
        super(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
    }

    public QuickConstraints() {
        this(0,0,1,1,0,0,
                LINE_START, NONE, new Insets(0,0,0,0), 0, 0);
    }

    public QuickConstraints(Insets insets) {
        this(0, 0, 1, 1, 0, 0,
                LINE_START, NONE, insets, 0, 0);
    }

    public QuickConstraints left() {
        gridx = leftx;
        anchor = leftAnchor;
        return this;
    }

    public QuickConstraints right() {
        gridx = rightx;
        anchor = rightAnchor;
        return this;
    }

    public QuickConstraints gridy(int gridy) {
        this.gridy = gridy;
        return this;
    }

    public QuickConstraints incy() {
        this.gridy += 1;
        return this;
    }

    public QuickConstraints leftx(int leftx) {
        this.leftx = leftx;
        return this;
    }

    public QuickConstraints rightx(int rightx) {
        this.rightx = rightx;
        return this;
    }

    public QuickConstraints leftAnchor(int leftAnchor) {
        this.leftAnchor = leftAnchor;
        return this;
    }

    public QuickConstraints rightAnchor(int rightAnchor) {
        this.rightAnchor = rightAnchor;
        return this;
    }
}
