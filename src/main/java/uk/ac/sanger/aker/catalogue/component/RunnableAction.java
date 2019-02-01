package uk.ac.sanger.aker.catalogue.component;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * An implementation of {@code Action} that simply calls a given {@link Runnable} when it is invoked.
 */
public class RunnableAction extends AbstractAction {
    private final Runnable runnable;

    /**
     * Constructs a new action
     * @param name the name of the action
     * @param runnable the function to call when the action is invoked
     */
    public RunnableAction(String name, Runnable runnable) {
        super(name);
        this.runnable = runnable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        runnable.run();
    }
}
