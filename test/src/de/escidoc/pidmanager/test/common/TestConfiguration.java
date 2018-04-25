/**
 * 
 */
package de.escidoc.pidmanager.test.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration for Test.
 * 
 * @author SWA
 * 
 */
public class TestConfiguration {

    private static final String TEST_PATH =
        "./test/src/de/escidoc/pidmanager/test/";

    private static final String TEST_CONF_FILE =
        TEST_PATH + "./test.properties";

    private Properties prop = new Properties();

    /**
     * 
     * @throws Exception
     */
    public TestConfiguration() throws Exception {
        this(TEST_CONF_FILE);
    }

    /**
     * 
     * @throws Exception
     */
    public TestConfiguration(final String filename) throws Exception {

        File f = new File(filename);
        if (!f.exists()) {

            f = new File(TEST_PATH + filename);
            if (!f.exists()) {
                throw new Exception("File " + filename + " does not exist in "
                    + new File(".").getAbsolutePath());
            }
        }
        if (!f.canRead()) {
            throw new Exception("File " + filename + " is not readable.");
        }

        InputStream inputStream = new FileInputStream(f);
        prop.load(inputStream);

        checkConfig();
    }

    /**
     * Get properties value for key.
     * 
     * @param key
     *            The properties key.
     * @return value or null
     */
    public String get(final String key) {
        return (this.prop.getProperty(key, null));
    }

    /**
     * Get the name of the database file.
     * 
     * @return name of database file
     */
    public String getDatabase() {

        return (this.prop.getProperty("SQLiteDbFile"));
    }

    /**
     * Get the name of the table.
     * 
     * @return name of table
     */
    public String getTable() {
        return (this.prop.getProperty("SQLiteDbTable"));
    }

    /**
     * 
     * @return
     */
    public String[] getColNames() {
        return this.prop.getProperty("SQLiteDbColNames").split(",");
    }

    /**
     * 
     * @return
     */
    public String getPidManagerUsername() {
        return (this.prop.getProperty("pidManagerUsername"));
    }

    /**
     * 
     * @return
     */
    public String getPidManagerPassword() {
        return (this.prop.getProperty("pidManagerPassword"));
    }

    /**
     * Check (minimal) of configuration.
     * 
     * @throws Exception
     */
    private void checkConfig() throws Exception {
        if (getDatabase().length() < 1) {
            throw new Exception(
                "Configuration for SqLite Database file missing.");
        }
        if (getTable().length() < 1) {
            throw new Exception(
                "Configuration for SqLite Database table missing.");
        }
    }
}
