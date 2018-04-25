/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.pidmanager.IdGenerators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.Logger;

import de.escidoc.pidmanager.Constants;
import de.escidoc.pidmanager.exceptions.IdentifierException;

/**
 * Generation of Identifier
 * 
 * This class contains methods to compute identifier.
 * 
 * @author SWA
 * 
 */
public class Identifier {

    private static Logger log = Logger.getLogger(Identifier.class);

    private static final int MAX_RANDOM_VALUE = 1001;

    private String backendStorage = null;

    private String tableName = "serialNumber";

    private String[] columNames =
        new String[] { "timeStamp LONG", "serial LONG" };

    private Properties prop = null;

    /**
     * The Generator mode. Usually configured via properties.
     */
    private String mode = "random";

    /**
     * Identifier.
     * 
     * @param prop
     *            Properties.
     */
    public Identifier(final Properties prop) {
        this.prop = prop;
        this.backendStorage =
            this.prop.getProperty(Constants.ID_GENERATOR_STORAGE, "SQLite");
        this.mode = getIdGeneratorMode();
    }

    /**
     * Generate the ID with the pre-configured generator mode.
     * 
     * @param value
     *            Values for generator if type based on otherwise null.
     * @return id.
     * @throws Exception
     *             Thrown if generating of identifier failed.
     */
    public String generateId(final String value) throws Exception {
        return (generateId(this.mode, value));
    }

    /**
     * Generate the ID with the chosen type.
     * 
     * @param mode
     *            Mode of generator (random, serial, ..)
     * @param value
     *            Values for generator if type based on otherwise null.
     * @return id (unique).
     * @throws Exception
     */
    public String generateId(final String mode, final String value)
        throws Exception {

        if (!mode.equals(this.mode)) {
            if (!modeOverriding()) {
                throw new Exception(
                    "The selected ID Generator mode is restricted.");
            }
        }

        String id = null;
        if (mode.equals("semantic")) {
            id = semantic(id);
        }
        else if (mode.equals("serial")) {
            id = serial();
        }
        else if (mode.equals("random")) {
            id = random();
        }
        else if (mode.equals("timestamp")) {
            id = random();
        }
        else if (mode.equals("noid")) {
            NoidConnector noid = new NoidConnector(this.prop);
            id = noid.getId();
        }
        else if (mode.equals("debug")) {
            id = debug();
        }
        else {
            log.warn("IdGenerator type " + mode
                + " not supported in this method.");
            throw new Exception(mode + " not supported via this method. "
                + "If applicable call method direct.");
        }

        return (id);
    }

    /**
     * Create a semantic identifier for the persistent identifier.
     * 
     * @param id
     *            The id which is use to generate the semantic identifier.
     * @return semantic identifier
     * @throws Exception
     *             Thrown in case of internal error.
     */
    public String semantic(final String id) throws Exception {
        return (id);
    }

    /**
     * Create identifier from serial number.
     * 
     * @return identifier as serial number
     * @throws Exception
     *             Thrown in case of internal failure.
     */
    public String serial() throws Exception {
        String id = null;
        if (this.backendStorage.equals("SQLite")) {
            String dbFile = this.prop.getProperty("SQLite:DBFile");
            if (dbFile == null || dbFile.equals("")) {
                dbFile = "pidmanager.sqlite";
            }

            // serial number with SQLite support to store value
            Class.forName("org.sqlite.JDBC");
            Connection conn =
                DriverManager.getConnection("jdbc:sqlite:" + dbFile);

            try {
                // check is table exist
                if (!checkTableExists(conn, tableName)) {
                    createTable(conn);
                }
                Statement stat = conn.createStatement();

                // SELECT MAX(serial) FROM tableName - does not work :-(
                ResultSet rs =
                    stat.executeQuery("SELECT serial FROM " + this.tableName
                        + " ORDER BY serial DESC;");

                long maxSerial = 0;

                if (rs.next()) {
                    maxSerial = rs.getLong("serial");
                    System.out.println("max(serial) = " + maxSerial);
                    rs.close();
                }
                maxSerial++;

                stat.executeUpdate("INSERT INTO " + this.tableName
                    + " values ('" + System.currentTimeMillis() + "', '"
                    + maxSerial + "');");

                id = Long.toString(maxSerial);
            }
            finally {
                conn.close();
            }
        }

        if (id == null) {
            throw new IdentifierException(
                "Serial identifier generation failed.");
        }
        return (id);
    }

    /**
     * Create identifier by random number.
     * 
     * @return identifier base on random number
     */
    public String random() {
        Random r = new Random();
        long i;

        do {
            i = r.nextLong();
        }
        while (i < (long) MAX_RANDOM_VALUE);

        return (Long.toString(i));
    }

    /**
     * Create identifier from computed checksum.
     * 
     * @param algorithm
     *            checksum method
     * @param data
     *            data set for checksum calculation
     * @return identifier
     * @throws Exception
     *             Thrown if checksum calculation fails.
     */
    public String checksum(final String algorithm, final String data)
        throws Exception {
        MessageDigest md;
        byte[] csum;

        if ((data == null) || (data.length() == 0)) {
            throw new Exception("Missing dataset.");
        }
        if ((algorithm == null) || (algorithm.length() == 0)) {
            throw new Exception("Undefined method.");
        }

        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new Exception("Algorithm not implemented: " + nsae);
        }

        StringBuffer strBuf = new StringBuffer();
        md.update(data.getBytes(), 0, data.length());
        csum = md.digest();
        for (int i = 0; i < csum.length; i++) {
            strBuf.append(toHexString(csum[i]));
        }
        return ("");
    }

    /**
     * Create Identifier on base of time.
     * 
     * @return timestamp
     * @throws Exception
     *             Thrown in case of internal error.
     */
    public String timestamp() throws Exception {
        return (Long.toString(System.currentTimeMillis()));
    }

    /**
     * Getting debug identifier. This Handle doesn't exist within PID systems.
     * 
     * @return debug identifier.
     * @throws Exception
     *             Thrown if reading properties failed.
     */
    public String debug() throws Exception {
        String id = "test-handle-123:2";

        String debugId = this.prop.getProperty(Constants.DEBUG_IDENTIFIER);
        if ((debugId != null) && (debugId.length() > 2)) {
            id = debugId;
        }

        return (id);
    }

    /**
     * Convert a byte to hexa-decimal String.
     * 
     * @param b
     *            byte
     * @return HexString
     */
    private String toHexString(final byte b) {
        int value = (b & 0x7F) + (b < 0 ? 128 : 0);

        String ret = (value < 16 ? "0" : "");
        ret += Integer.toHexString(value).toUpperCase();

        return ret;
    }

    /**
     * Check if SQLite DB contains table with name.
     * 
     * @param conn
     *            SQLite DB connection.
     * @param table
     *            Name of table.
     * @return true if table with name exists, false otherwise
     * @throws SQLException
     *             Thrown if DB operation fail.
     */
    private boolean checkTableExists(final Connection conn, final String table)
        throws SQLException {

        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs =
            meta.getTables(null, null, table, new String[] { "TABLE" });

        String checkName = table.toLowerCase();

        while (rs.next()) {
            String foundTable = rs.getString("TABLE_NAME").trim().toLowerCase();
            if (foundTable.equals(checkName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creating table for serial number.
     * 
     * @param conn
     *            SQLite DB connection.
     * @throws SQLException
     *             Thrown if DB operation fail.
     */
    private void createTable(final Connection conn) throws SQLException {

        Statement stat = conn.createStatement();
        String sqlQuery =
            "CREATE TABLE " + this.tableName + " (" + this.columNames[0] + ", "
                + this.columNames[1] + ");";
        stat.executeUpdate(sqlQuery);
        log.warn("New table created: " + sqlQuery);
    }

    /**
     * Get the selected Id generator method from the properties.
     * 
     * @return id generator method
     */
    private String getIdGeneratorMode() {
        return (this.prop.getProperty(Constants.ID_GENERATOR_MODE));
    }

    /**
     * Get from properties if ID Generator mode is allowed to be overridden.
     * 
     * @return true if overriding is allowed, false otherwise.
     */
    private Boolean modeOverriding() {
        return (Boolean.valueOf(this.prop
            .getProperty(Constants.ID_GENERATOR_MODE_OVERRIDE)));
    }
}
