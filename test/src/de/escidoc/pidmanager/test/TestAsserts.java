package de.escidoc.pidmanager.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assert Handle Schema.
 * 
 * @author SWA
 * 
 */
public class TestAsserts {

    private static final Pattern PATTERN_URI_SCHEMA =
        Pattern.compile("hdl:[0-9]+:([0-9]+/[^\\s\\w]+)");

    /**
     * Test if the handle has the right URI schema.
     * 
     * @param handle
     *            The handle which is to check.
     * @throws Exception
     *             Thrown if handle fits not to the defined schema.
     */
    public static void assertHandleSchema(final String handle) throws Exception {
        Matcher m = PATTERN_URI_SCHEMA.matcher(handle);
        if (!m.find()) {
            throw new Exception("Invalid URI schema for handle (" + handle
                + ")");
        }

    }
}
