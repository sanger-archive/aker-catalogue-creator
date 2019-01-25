package uk.ac.sanger.aker.catalogue.model;

import org.testng.annotations.Test;
import uk.ac.sanger.aker.catalogue.MessageVar;

import static org.testng.Assert.assertEquals;
import static uk.ac.sanger.aker.catalogue.MessageVar.PLURAL;
import static uk.ac.sanger.aker.catalogue.MessageVar.SINGULAR;

/**
 * Tests for {@link MessageVar}
 * @author dr6
 */
@Test
public class MessageVarTest {
    public void testMessageVar() {
        String template = "There {is a|are} monkey{s} here and {it|they} want{s|} your banana{s}.";
        assertEquals(MessageVar.process(template, SINGULAR), "There is a monkey here and it wants your banana.");
        assertEquals(MessageVar.process(template, PLURAL), "There are monkeys here and they want your bananas.");
    }
}
